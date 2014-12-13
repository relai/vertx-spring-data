package com.github.relai.vertx.springdata;


import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.domain.Sort.Order;
import static org.springframework.data.domain.Sort.NullHandling;
import org.springframework.util.ReflectionUtils;
import org.vertx.java.core.json.JsonArray;

/**
 * A helper to allow {@link AsyncCrudRepository} to be exposed as a REST web service. 
 * 
 * <p>Example: 
 *   <pre> {@code 
 *     RestHelper<String> helper = new RestHelper<>(repository, String.class);                      
 *     vertx.createHttpServer()
 *	    	.requestHandler(helper.createRouteMatcher("/contacts"))
 *	    	.listen(8080, "localhost"); 
 *    }</pre>
 * 
 * Optional sorting and paging hints are supported in the {@link #getAll GET} request. For
 * example: 
 * <ul>
 *    <li> <code> /tasks </code>
 *    <li> <code> /tasks?page=2</code>
 *    <li> <code> /tasks?sort=name</code>
 *    <li> <code> /tasks?sort=name.asc.nulls_first,priority.desc</code> 
 *    <li> <code> {@literal /tasks?page=2&size=15&sort=name.asc.nulls_first} </code> 
 * </ul>
 * For details, see {@link #getPageable getPageable} and {@link #getSort getSort} methods.
 * 
 * @author relai
 * @param <ID> the Id type of the entity
 */

public class RestHelper<ID extends Serializable> {
	final static String ID_STR = "id";
    final static String SORT = "sort";
    final static String SIZE = "size";
    final static String PAGE = "page";

    final static int DEFAULT_SIZE = 25;
    final static int DEFAULT_PAGE = 0;
    
	final AsyncCrudRepository<ID> client;
	final Class<ID> idType;
   

    /**
     * Constructs the <code>RestHelper</code>.
     * 
     * @param client asynchronous repository instance
     * @param idType The id type of the entity
     */
    public  RestHelper(AsyncCrudRepository<ID> client, Class<ID> idType) {
        this.client = client;
        this.idType = idType;        
    }
    
    /**
     * Creates a route matcher with the specified pattern.
     * 
     * @param pattern the route matcher pattern. For example, <code> "/contacts" </code>
     * @return  A route matcher
     */
	public Handler<HttpServerRequest> createRouteMatcher(String pattern) {
	    String idPattern = pattern + "/:" + ID_STR;		
		RouteMatcher matcher = new RouteMatcher();
		matcher.get(pattern, 	  this::getAll)
			   .post(pattern, 	  this::post)
			   .get(idPattern,    this::getOne)
			   .put(idPattern,    this::put)
			   .delete(idPattern, this::delete);
		return matcher;
	}
	
    /**
     * Gets all entities. The method supports optional paging and sort hints in 
     * the HTTP request. For details, see {@link #getPageable getPageable} and {@link #getSort getSort} methods.
     * 
     * @param request HTTP request
     */
	public void getAll(HttpServerRequest request) {
        try {
            Sort sort = getSort(request);
            Pageable pageable = getPageable(request, sort);

            if (pageable != null) {
                getAllWithPageable(request, pageable);
            } else if (sort != null) {
                getAllWithSort(request, sort);
            } else {   
               client.findAll(result -> onDatabaseResult(result, request));
            }
        } catch (RuntimeException ex) {
            handleException(request, ex); 
        }
	}
    
    protected void getAllWithPageable(HttpServerRequest request, Pageable pageable){
         MessageHandler<JsonArray> onreply = result -> 
             onDatabaseResult(result, request);
         
        if (client instanceof AsyncPagingAndSortingRepository) {
            ((AsyncPagingAndSortingRepository)client).findAll(pageable, 
                onreply );
        } else {
            Method m = ReflectionUtils.findMethod(client.getClass(), 
                "findAll", Pageable.class, MessageHandler.class);
            m.setAccessible(true);
            ReflectionUtils.invokeMethod(m, client, pageable, onreply);
        }
    }

      protected void getAllWithSort(HttpServerRequest request, Sort sort){
         MessageHandler<JsonArray> onreply = result -> 
             onDatabaseResult(result, request);
         
        if (client instanceof AsyncPagingAndSortingRepository) {
            ((AsyncPagingAndSortingRepository)client).findAll(sort, 
                onreply );
        } else {
            Method m = ReflectionUtils.findMethod(client.getClass(), 
                "findAll", Sort.class, MessageHandler.class);
            m.setAccessible(true);
            ReflectionUtils.invokeMethod(m, client, sort, onreply);
        }
    }
      
    /**
     * Creates an entity. 
     * 
     * @param request HTTP request
     */  
	public void post(HttpServerRequest request) {
		save(request, true);
	}

    /**
     * Gets one entity by Id.
     * @param request HTTP request
     */
	public void getOne(HttpServerRequest request) {
		client.findOne(id(request), result -> onDatabaseResult(result, request));
	}

    /**
     * Updates an existing entity.
     * 
     * @param request HTTP request
     */
	public void put(HttpServerRequest request) {
		save(request, false);
	}

    /**
     * Deletes an entity by Id.
     * 
     * @param request HTTP request
     */
	public void delete(HttpServerRequest request) {
        try {
            client.delete(id(request), result -> onDatabaseResult(result, request)); 
        } catch (RuntimeException ex) {
            handleException(request, ex);
        }
	}

    /**
     *  Saves the entity. 
     * 
     * @param request HTTP request
     * @param isNew  whether this entity is new (creation).
     */
    protected void save(HttpServerRequest request, boolean isNew) {        
        request.bodyHandler((Buffer body) -> {
            try {               
                String data = body.toString();
                JsonObject entity = new JsonObject(data);    
                doSave(entity, request, isNew);
            } catch (RuntimeException ex) {
                Exception error = new IllegalArgumentException("The request body is not JSON", ex);
                handleException(request, error);
            }
        });
	}

    /**
     * Saves the entity. This can be either a creation or update operation.
     * 
     * @param entity the entity
     * @param request HTTP request
     * @param isNew whether this is a new entity for creation or an existing entity for update
     */
	protected void doSave(JsonObject entity, HttpServerRequest request, boolean isNew) {
		client.save(entity, (result) -> {
			if (isNew && result.succeeded()) {
				request.response().setStatusCode(CREATED.code());
			}
			onDatabaseResult(result, request);
		});
	}

    /**
     * Processes the asynchronous database result.
     * 
     * @param result  the asynchronous event handler
     * @param request  HTTP request
     */
	protected void onDatabaseResult(AsyncResult<? extends Message<?>> result, 
			HttpServerRequest request) {
		if (result.succeeded()) {
			Object data = result.result().body();
			if (data == null) {
				request.response()
				  .setStatusCode(NO_CONTENT.code())
				  .end();
			} else if (data instanceof JsonElement){
                JsonElement el = (JsonElement) data;
				String payload = el.isArray() ? 
	    			el.asArray().encode() : el.asObject().encode();
	    		request.response()
	    		   .putHeader("content-type", "application/json")
	    	       .end(payload);
	    	} else {
	    		request.response()
	    		   .setStatusCode(INTERNAL_SERVER_ERROR.code())
	    		   .end("Uncognized response from vertx");
	    	}
		} else {
            handleException(request, result.cause());
		}
	}

    /**
     * Retrieves the ID from the HTTP request parameter.
     * 
     * @param request HTTP request
     * @return this
     */
	@SuppressWarnings("unchecked")
	protected ID id(HttpServerRequest request) {
		String s = request.params().get(ID_STR);
		
		if (s == null || s.isEmpty()) {
    		throw new IllegalArgumentException();
    	}
    	
    	ID result = null;
    	if (idType.isAssignableFrom(String.class)){
    		result = (ID) s;
    	} else if (idType.equals(Long.class)) {
    		result =  (ID) Long.valueOf(s);
    	} else {
    		try {
				Method m = idType.getMethod("valueOf", new Class[]{String.class});
				result = (ID) m.invoke(null, s);				
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException 
					| IllegalArgumentException	| InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
    	}    	
    	return result;    	
	}
    
    private void handleException(HttpServerRequest request, Throwable ex) {
        int errorCode = INTERNAL_SERVER_ERROR.code();
        
        if (ex instanceof ReplyException) {
            errorCode = ((ReplyException) ex).failureCode(); 
        } else if (ex instanceof IllegalArgumentException) {
            errorCode = BAD_REQUEST.code();
        }
        
        if (errorCode < 0 || errorCode > 600) {
            errorCode = INTERNAL_SERVER_ERROR.code();    
        }
        
        String explanation = ex.getMessage();
        if (explanation == null) {
            explanation = "An internal server error encountered.";
        }
		request.response().setStatusCode(errorCode)
		       .end(explanation);
    }
    
    
    /**
     * Retrieves the sort specification of the query. The sort format in the 
     * request is expected as follows in the query parameter:
     *  <pre>
     *  sort=property[.direction[.nullHandling]][,property[.direction[.nullHandling]]]
     * </pre>
     * 
     * <p>The valid values of directions are: asc, desc. 
     * <p> The valid values of nullHanding are: native, nulls_first, nulls_last.
     * 
     * <p> Examples of query parameters:
     * <ul>
     *   <li>sort=priority,name,created
     *   <li>sort=priority.desc,name,created.asc
     *   <li>sort=priority.desc.nulls_last,name,created.asc
     * </ul>
     * 
     * <p> Override this method to define your customized sort format.
     * 
     * @param request HTTP request
     * @return  The <code>Sort</code> specification
     */
    protected Sort getSort(HttpServerRequest request) {
        String sortStr = request.params().get(SORT);
        if (sortStr == null || sortStr.isEmpty()) {
           return null;                         
        }     
        
        String[] properties = sortStr.split(",");
        List<Order> orders = new ArrayList(properties.length);
        for (String prop : properties) {                                
            String[] details = prop.split("\\.");                
            String propName = details[0];                
            Direction direction = (details.length > 1) ? 
                Direction.fromStringOrNull(details[1]) : Sort.DEFAULT_DIRECTION;              
            NullHandling nullHandling = (details.length > 2) ? 
                NullHandling.valueOf(details[2].toUpperCase(Locale.US)) : null;
                                
            Order order = new Order(direction, propName, nullHandling);
            orders.add(order);
        }      
        return new Sort(orders);
    }
    
    /**
     * Retrieves the paging specification from the request. The paging hint is
     * expected as optional query parameters as <pre>
     *  {@literal page=pageNumber&size=pageSize} </pre>
     * 
     * <p> The page number starts at 0 and is defaulted to 0. The page size is defaulted
     * to 25. Paging is enabled when either page or size hint is present in the
     * request. 
     * 
     * <p> Override this method to define your own paging format.
     * 
     * @param request HTTP request
     * @param sort the <code>Sort</code> specification
     * @return  The <code>Pageable</code> specification
     */
    protected Pageable getPageable(HttpServerRequest request, Sort sort) {
        Pageable result = null;
        String pageStr = request.params().get(PAGE);
        String sizeStr = request.params().get(SIZE);      
        if (pageStr != null || sizeStr != null)  {
            int page = (pageStr != null) ? Integer.valueOf(pageStr) : DEFAULT_PAGE;
            int size = (sizeStr != null) ? Integer.valueOf(sizeStr) : DEFAULT_SIZE;            
            result = new PageRequest(page, size, sort);
        }        
        return result;
    }
}
