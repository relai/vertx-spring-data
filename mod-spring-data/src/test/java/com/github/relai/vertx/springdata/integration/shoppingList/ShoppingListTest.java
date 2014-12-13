package com.github.relai.vertx.springdata.integration.shoppingList;

import com.github.relai.vertx.springdata.AsyncRepositoryBuilder;

import org.junit.Test;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;


import com.github.relai.vertx.springdata.SpringDeployer;
import com.github.relai.vertx.springdata.YokeRestHelper;
import com.github.relai.vertx.springdata.integration.shoppingList.domain.Config;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BodyParser;

import static io.netty.handler.codec.http.HttpResponseStatus.CONFLICT;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

public class ShoppingListTest extends TestVerticle {
    
    private static final int PORT_NUMBER = 8088;

	AsyncShoppingItemRepository client;

	@Override
	public void start() {
		initialize();
        
        AsyncRepositoryBuilder<String> builder
               = new AsyncRepositoryBuilder<>(vertx.eventBus());    
		client =  builder.build(AsyncShoppingItemRepository.class);			
        
        YokeRestHelper<Long> rest = new YokeRestHelper<>(client, Long.class);        
        Yoke yoke = new Yoke(vertx);      
        yoke.use(new BodyParser())
            .use(rest.createRouter("/shoppinglist"))
            .listen(PORT_NUMBER);
        
        SpringDeployer deployer = new SpringDeployer(container);
        deployer.springConfigClass(Config.class)
                .deploy(result -> startTests());       
	}
		
    @Test
	public void restGetAll() {		
        createHttpClient().get("/shoppinglist",  resp -> {
            assertEquals(200, resp.statusCode());
            resp.bodyHandler((Buffer data) -> {
                getContainer().logger().info("rest getAll: " + data.toString());
                testComplete();
            });
        }).end();
	}
    
    @Test
	public void restGetAllByPage() {		
        createHttpClient().get("/shoppinglist?page=0&size=3&sort=name.asc.nulls_first",
            (HttpClientResponse resp) -> {
                assertEquals(200, resp.statusCode());
                resp.bodyHandler((Buffer data) -> {
                    JsonArray array = new JsonArray(data.toString());
                    JsonObject apples = (JsonObject) array.get(1);
                    assertEquals("apples", apples.getString("name"));
                    getContainer().logger().info("rest get 1st page by name: " + data.toString());
                    testComplete();
                });
            }
        ).end();
	}
    
    @Test
	public void restGetAllBySort() {		       
        createHttpClient().get("/shoppinglist?sort=name.asc", resp -> {
            assertEquals(200, resp.statusCode());
            resp.bodyHandler((Buffer data) -> {
                getContainer().logger().info("rest get by sort: " + data.toString());
                testComplete();
            });
        }).end();                        
	}
    
    @Test
	public void restGetOne() {
        createHttpClient().get("/shoppinglist/1", resp -> {
            assertEquals(200, resp.statusCode());
            resp.bodyHandler(data -> {
                JsonObject coffee= new JsonObject(data.toString());
                assertEquals("coffee", coffee.getString("name"));
                getContainer().logger().info("rest getOne: " + coffee.toString());
                testComplete();
            });
        }).end();                
	}
   
    @Test
	public void restPost() {        
        HttpClientRequest request = createHttpClient().post("/shoppinglist", resp -> {
            assertEquals(201, resp.statusCode());
            resp.bodyHandler(data -> {
                JsonObject saved = new JsonObject(data.toString());
                assertEquals("cream", saved.getString("name"));
                getContainer().logger().info("rest post: " + saved.toString());
                testComplete();
            });
        });
        
        JsonObject cream = new JsonObject()
            .putString("name", "cream")
            .putNumber("priority", 5);
        Buffer content = new Buffer(cream.toString());
        
        request.headers().add("Content-Length", String.valueOf(content.length()));        
        request.write(content).end();                
	}
	
    @Test
	public void restPut() {        
        HttpClientRequest request = createHttpClient().put("/shoppinglist/2", resp -> {
            assertEquals(200, resp.statusCode());
            resp.bodyHandler(data -> {
                JsonObject lowFat = new JsonObject(data.toString());
                assertEquals("low-fat milk", lowFat.getString("name"));
                getContainer().logger().info("rest put: " + lowFat.toString());
                testComplete();
            });
        });
        
        JsonObject milk = new JsonObject();
		milk.putNumber("id", 2)
		    .putNumber("version", 2)
		    .putString("name", "low-fat milk")
		    .putNumber("priority", 8);
        Buffer content = new Buffer(milk.toString());
        
        request.headers().add("Content-Length", String.valueOf(content.length()));        
        request.write(content).end();                
	}
    
    @Test
    public void testDelelte() {
        createHttpClient().delete("/shoppinglist/5", resp -> {
            getContainer().logger().info("rest delete: " + resp.statusCode());
            testComplete();            
        }).end();
    }
    
    @Test
	public void findByPriority() {	
		client.findByPriority(3, reply -> {
			assertTrue(reply.succeeded());
			JsonArray items = reply.result().body();
			assertTrue(items.size() > 0);
		    getContainer().logger().info("find by priority: " + items.toString());
            testComplete();
		});
	}

    @Test
	public void findByPriorityIsNull() {	
		client.findByNameIsNull(reply -> {
			assertTrue(reply.succeeded());
			JsonArray items = reply.result().body();
			assertTrue(items.size() > 0);
		    getContainer().logger().info("find by name is null: " + items.toString());
		    testComplete();
		});
	}
	

    @Test         
	public void negativeTestOptimisticLock() {		 
		JsonObject milk = new JsonObject();
		milk.putNumber("id", 2)
		    .putNumber("version", 1)
		    .putString("name", "low-fat milk")
		    .putNumber("priority", 8);
		client.save(milk, reply -> {
			assertTrue(reply.failed());
			assertTrue(reply.cause() instanceof ReplyException);
			ReplyException ex = (ReplyException) reply.cause();
			assertEquals(CONFLICT.code(), ex.failureCode());
			getContainer().logger().info("nagative test: " + ex.failureCode() + " - " + ex.getMessage());
			testComplete();
		});
		 
	}
    
    private HttpClient createHttpClient() {
        return getVertx().createHttpClient()
                         .setHost("localhost")
                         .setPort(PORT_NUMBER);
    }
}
