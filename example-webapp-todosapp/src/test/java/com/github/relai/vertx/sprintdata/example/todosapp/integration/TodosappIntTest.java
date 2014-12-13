package com.github.relai.vertx.sprintdata.example.todosapp.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.testtools.TestVerticle;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 *
 * @author relai
 */
public class TodosappIntTest extends TestVerticle{
    
    
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
    public void home() {
        createHttpClient().get("/tasks", resp -> {
            assertEquals(200, resp.statusCode());
            resp.bodyHandler(( Buffer data) -> {
                String page = data.toString();
                assertTrue(page.indexOf("todosapp") > 0);
                assertTrue(page.indexOf("Buy coffee") > 0);
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
