package com.github.relai.vertx.springdata;

import static io.netty.handler.codec.http.HttpResponseStatus.UNPROCESSABLE_ENTITY;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

import java.io.Serializable;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;

/**
 * A helper to allow {@link AsyncCrudRepository} to be exposed as a REST web service 
 * in a Yoke application. The Yoke application is assumed to have enabled BodyParser.
 * 
 * <p>Sample: 
 *    <pre> {@code
 *    YokeRestHelper<Long> rest = new YokeRestHelper<>(client, Long.class);        
 *    Yoke yoke = new Yoke(vertx);      
 *    yoke.use(new BodyParser())
 *        .use(rest.createRouter("/shoppinglist"))
 *        .listen(8080); 
 *    }</pre>
 * 
 * @author relai
 * @param <ID> the entity Id type
 */

public class YokeRestHelper<ID extends Serializable> extends RestHelper<ID>{
	
    /**
     * Constructs the <code>YokeRestHelper</code>.
     * 
     * @param client asynchronous repository 
     * @param idType the entity Id type
     */
    public YokeRestHelper(AsyncCrudRepository<ID> client, Class<ID> idType) {
        super(client, idType);
    }
	
    /**
     * Creates a Yoke router for the REST application.
     * 
     * @param pattern the pattern for the router
     * @return  A Yoke <code>Router</code>
     */
	public Router createRouter(String pattern) {
	    String idPattern = pattern + "/:" + ID_STR;		
		Router matcher = new Router();
		matcher.get(pattern, 	  this::getAll)
			   .post(pattern, 	  this::post)
			   .get(idPattern,    this::getOne)
			   .put(idPattern,    this::put)
			   .delete(idPattern, this::delete);
		return matcher;
	}

	@Override
	protected void save(HttpServerRequest request, boolean isNew) {
		if (request instanceof YokeRequest) {
			Object body = ((YokeRequest)request).body();		
			
			if (body instanceof Buffer) {
		         body = new JsonObject(((Buffer)body).toString());
		    }	       
			
		    if ((body instanceof JsonObject)) {
		    	doSave((JsonObject) body, request, isNew);
		    } else {
 	           request.response()
 	             .setStatusCode(UNPROCESSABLE_ENTITY.code())
 	             .end();
		   }		
		} else {
			request.response()
			  .setStatusCode(INTERNAL_SERVER_ERROR.code())
			  .end();		
		}
	}
}
