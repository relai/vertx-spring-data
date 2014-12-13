package com.github.relai.vertx.springdata.example.rest.integration;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.vertx.java.core.http.HttpClient;
import org.vertx.testtools.TestVerticle;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 *
 * @author relai
 */
public class ContactRestIntTest extends TestVerticle {
    
    @Override
    public void start() {
        initialize();

        getContainer().deployModule(System.getProperty("vertx.modulename"), result -> {
            assertTrue(result.succeeded());
            assertNotNull("deploymentID should not be null", result.result());                                  
            startTests();
        });
    }
    
    @Test
    public void getAll() {
        createHttpClient().get("/contacts", resp -> {
            assertEquals(200, resp.statusCode());
            resp.bodyHandler( data -> {
                getContainer().logger().info("get all: " + data.toString());
                testComplete();
            });
        }).end();
    }
    
    private HttpClient createHttpClient() {
        return getVertx().createHttpClient()
                         .setHost("localhost")
                         .setPort(8080);
    }
    
}
