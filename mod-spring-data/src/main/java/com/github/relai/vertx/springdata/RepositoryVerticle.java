package com.github.relai.vertx.springdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import static io.netty.handler.codec.http.HttpResponseStatus.CONFLICT;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_ACCEPTABLE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactoryInformation;
import org.vertx.java.core.VertxException;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;


/**
 * Internal class of a worker verticle to query the database using Spring Data. It supports standard 
 * CRUD operations and custom methods through the Spring <code>Repository</code> interfaces.
 *
 * <p>This class is for internal use by the mod only. Clients should access data 
 * via the <code>AsyncCrudRepository</code> created by the {@link AsyncRepositoryBuilder}.
 * 
 * @author relai
 * 
 */
public class RepositoryVerticle extends Verticle {   
    public final static String APP_CONTEXT = "applicationContext";
    public final static String REPOSITORY  = "repository";     
    
    final static String ACTION    = "action";
    final static String FINDALL   = "findAll";
    final static String FINDONE   = "findOne";
    final static String SAVE      = "save";
    final static String DELETE    = "delete";
    final static String DELETEALL = "deleteAll";
    final static String EXISTS    = "exists";
    final static String COUNT     = "count";
    
    final static String ID = "id";
    final static String ENTITY = "entity";
    final static String ARGS = "args";
    
    private ConfigurableApplicationContext springContext;  
	private Class<? extends Repository> defaultRepositoryType;
    private Class<?> defaultEntityType;
    private Class<? extends Serializable> defaultIdType;
    
    @SuppressWarnings({ "rawtypes", "unchecked"})
	@Override 
	public void start() {
        
        try {
            // Start the Spring application
            JsonObject config = getContainer().config();                                    
            String configClassName = config.getString(APP_CONTEXT);
            Class configClass = Class.forName(configClassName);         
            SpringApplication app = new SpringApplication(configClass, JacksonAutoWire.class);
            springContext = app.run();
                      
            // Get the default repository type if the context contains only
            // a single repository factory
            Map<String, RepositoryFactoryInformation> fm = 
                springContext.getBeansOfType(RepositoryFactoryInformation.class);   
            if (fm.size() == 1) {
                fm.values().forEach((RepositoryFactoryInformation rfi) -> { 
                    defaultRepositoryType = (Class<? extends Repository>) 
                        rfi.getRepositoryInformation().getRepositoryInterface();
                    defaultIdType = rfi.getRepositoryInformation().getIdType();
                    defaultEntityType = rfi.getRepositoryInformation().getDomainType();
                });
            }            
                                        
            vertx.eventBus().registerHandler(getEventBusAddress(), this::handle); 
       
        } catch (SecurityException | ClassNotFoundException ex) {
            Logger.getLogger(RepositoryVerticle.class.getName()).log(Level.SEVERE, null, ex);
            throw new VertxException(ex);
        }
    }
    
    @Override 
    public void stop() {
        super.stop();
        if (springContext != null) {
            springContext.close();
        }
    }
    
   
    private void handle(Message<JsonObject> message) {
        Object result = null;
        try {
            JsonObject command = message.body();   
            byte[] args = command.getBinary(ARGS);                        
            if (args != null) {            	 
                 result = invoke(command);  
            } else {            	
                String action = command.getString(ACTION, "");
                switch (action) {
	                case FINDALL:
	                    result = findAll(command);
	                    break;
	                case FINDONE:
	                    result = findOne(command);
	                    break;
	                case EXISTS:
	                	result = exists(command);
	                	break;
	                case DELETE:
	                    delete(command);
	                    result = Void.TYPE;
	                    break;
	                case DELETEALL:
	                    deleteAll(command);
	                    result = Void.TYPE;
	                    break;
	                case COUNT:
	                    result = count(command);
	                    break;
	                case SAVE:
	                    result = save(command);
	                    break;
	                default:
	                	throw new IllegalClientRequest("Unknown action: " + action);
                }                        
            }  
            if (Void.TYPE.equals(result)) {
            	message.reply();
            } else {
            	message.reply(result);
            }
        } catch (EmptyResultDataAccessException ex){
            fail(message, NOT_FOUND, ex);
        } catch (IllegalClientRequest ex) {
            fail(message, NOT_ACCEPTABLE, ex);
        } catch (OptimisticLockingFailureException ex) {
            fail(message, CONFLICT, ex);
        } catch (InvocationTargetException | IllegalAccessException | RuntimeException ex) {
            fail(message, INTERNAL_SERVER_ERROR, ex);
        } 
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected JsonArray findAll(JsonObject command) {
        CrudRepository repository = getCrudRepository(command);
        Iterable entities;
        JsonArray idArray = command.getArray(ID);
        if (idArray == null) {
        	entities = repository.findAll();
        } else {
        	Iterable ids = fromJsonArray(idArray, getEntityIdType(command));
        	entities = repository.findAll(ids);
        }
        return toJsonArray(entities);
    }
    
    @SuppressWarnings({ "unchecked" })
	protected JsonObject findOne(JsonObject command) {
        Serializable id = getId(command);
        Object entity = getCrudRepository(command).findOne(id);
        if (entity == null) {
            throw new EmptyResultDataAccessException("Cannot locate the record with id " + id.toString(), 1);
        }
        return toJsonObject(entity);
    }
    
    @SuppressWarnings({ "unchecked" })
    protected Boolean exists(JsonObject command) {
        Serializable id = getId(command);
        return getCrudRepository(command).exists(id);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void delete(JsonObject command) {
        CrudRepository repository = getCrudRepository(command);
        
        if (command.getField(ID) != null) {
        	repository.delete(getId(command));
        	return;
        } 
        
        JsonElement entity = command.getElement(ENTITY);
        Class entityType = getEntityIdType(command);
        if (entity.isObject()) {
            Object obj = fromJsonObject((JsonObject)entity, entityType);
        	repository.delete(obj);
        } else {
            List<Object> objs = fromJsonArray((JsonArray)entity, entityType);
        	repository.delete(objs);
        }
    }
    
    private void deleteAll(JsonObject command) {
        getCrudRepository(command).deleteAll();
    }
    
    private Long count(JsonObject command) {
        return getCrudRepository(command).count();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private JsonElement save(JsonObject command) {
        JsonElement entity = command.getElement(ENTITY);
        CrudRepository repository = getCrudRepository(command);
        Class entityType = getEntityType(command);
        JsonElement ret;
        if (entity.isObject()) {
            Object obj = fromJsonObject((JsonObject)entity, entityType);
        	Object result = repository.save(obj);
        	ret = toJsonObject(result);
        } else {
            List<Object> objs = fromJsonArray((JsonArray)entity, entityType);
        	Iterable results = repository.save(objs);
        	ret = toJsonArray(results);
        }
        return ret;
    }
	    
    private Class<? extends Repository> getRepositoryType(JsonObject command) {
        String repoName = command.getString(REPOSITORY);
        Class<?> result = null;      
        if (repoName == null || 
            defaultRepositoryType != null && 
            defaultRepositoryType.getCanonicalName().equals(repoName) ) {
            result = defaultRepositoryType;
        } else {
            try {
                result = Class.forName(repoName);
            } catch (ClassNotFoundException ex) {
                throw new VertxException(ex);
            }
        }        
        if (Repository.class.isAssignableFrom(result) == false) {
             throw new VertxException("The repository interface does not extend Repository");
        }               
        return (Class<? extends Repository>) result;
    }
    
    private Class<? extends Serializable> getEntityIdType(JsonObject command) {
        Class<? extends Serializable> result;
        Class<? extends Repository> repoType = getRepositoryType(command);     
        if (repoType.equals(defaultRepositoryType)) {
            result = defaultIdType;
        } else {
            RepositoryMetadata repositoryMetaData = 
                new DefaultRepositoryMetadata(repoType);
            result = repositoryMetaData.getIdType();
        }        
        return result;
    }
    
    private Class<?> getEntityType(JsonObject command) {
        Class<?> result;
        Class<? extends Repository> repoType = getRepositoryType(command);       
        if (repoType.equals(defaultRepositoryType)) {
            result = defaultEntityType;
        } else {
            RepositoryMetadata repositoryMetaData = 
                new DefaultRepositoryMetadata(repoType);
            result = repositoryMetaData.getDomainType();
        }        
        return result;
    }
        
    private Repository getRepository(JsonObject command) {                 
        Class<? extends Repository> type = getRepositoryType(command);
    	Repository repository = springContext.getBean(type);        
   	    return repository;
    }
    
    private CrudRepository getCrudRepository(JsonObject command) {
        Repository r = getRepository(command);        
        if (r instanceof CrudRepository == false) {
             throw new VertxException("The repository interface does not extend CrudRepository");
        }                
    	return (CrudRepository) r;
    }
    
    @SuppressWarnings("rawtypes")
	private JsonElement invoke(JsonObject command) 
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String action = command.getString(ACTION, "");
        Object[] args = SerializationHelper.fromBytes(command.getBinary(ARGS));
        Method method = null;
        for (Method m : getRepositoryType(command).getMethods()) {
            if (m.getName().equals(action) && m.getParameterCount() == args.length) {
          		Class[] argTypes = m.getParameterTypes();
            	boolean matched = true;            	
            	for (int index = 0; matched && (index < args.length); index++) {
            		// Ignore primitive type match check          
            	   	matched = argTypes[index].isPrimitive() || 
            	   			argTypes[index].isInstance(args[index]);
            	}
                if (matched) {
                	method = m;
                	break;
                }
            }
        }
        
        if (method == null) 
            throw new IllegalArgumentException("Cannot find the signature matching action " + action);
        
        Object repository = getRepository(command);
        Object result = method.invoke(repository, args);
        JsonElement msg;
        if (result == null) {
        	msg = new JsonObject();
        } else if (result instanceof Iterable)  {
            msg = toJsonArray((Iterable) result);
        } else if (getEntityIdType(command).isAssignableFrom(result.getClass())){
            msg = toJsonObject(result); 
        } else {
            throw new IllegalArgumentException("The return type must be the entity object, a collection of the entity object or null");
        }
        return msg;
    }
         
    private JsonObject toJsonObject(Object entity) {
        ObjectMapper mapper = springContext.getBean(ObjectMapper.class);
        Map map = mapper.convertValue(entity, HashMap.class);
        return new JsonObject(map);
    }
    
    private JsonArray toJsonArray(@SuppressWarnings("rawtypes") Iterable entities) {
        JsonArray data = new JsonArray();
        for (Object entity : entities) {
            data.add(toJsonObject(entity));
        }      
        return data;
    }
    
    private Object fromJsonObject(JsonObject entity, Class entityType) {
        ObjectMapper mapper = springContext.getBean(ObjectMapper.class);
        Object t = mapper.convertValue(entity.toMap(), entityType);
        return t;
    }
    
    private List<Object> fromJsonArray(JsonArray objects, Class entityType) {
    	List<Object> list = new ArrayList<>(objects.size());
    	for(Object obj: objects) {
    		if (obj instanceof JsonObject) {
    			list.add(fromJsonObject((JsonObject)obj, entityType));
    		} else {
    			//throw new VertxException("Incorrect entity array.");
    			list.add(obj);
    		}
    	}
    	return list;    	
    }
    
    private Serializable getId(JsonObject command) {
        return command.getValue(ID);
    }
    
    private void fail(Message<?> message, HttpResponseStatus status, Throwable ex) {
        String explanation = ex.getMessage();
        if (explanation == null || explanation.isEmpty()) {
            explanation = "A runtime exception " + ex.getClass().getSimpleName() + " occurred.";
        }        
        message.fail(status.code(), explanation);
    }
    
    static String getEventBusAddress(){
        return RepositoryVerticle.class.getCanonicalName();
    }
}
