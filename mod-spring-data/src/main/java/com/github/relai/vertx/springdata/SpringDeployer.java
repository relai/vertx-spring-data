package com.github.relai.vertx.springdata;

import static com.github.relai.vertx.springdata.RepositoryVerticle.APP_CONTEXT;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.VertxException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * The mod deployer for the Spring Data worker verticle. 
 * 
 * <p>A simple example: 
 *    <pre> {@code
 *    SpringDeployer deployer = new SpringDeployer(container);
 *    deployer.springConfigClass(Config.class).deploy(); 
 *    } </pre>
 * 
 * <p>A more complicated usage example: 
 *    <pre> {@code
 *    SpringDeployer deployer = new SpringDeployer(container);
 *    deployer.instances(5)
 *            .multiThreaded(true)
 *            .springConfigClass(Config.class)
 *            .deploy((result) -> {
 *                // do something
 *             }); 
 *    } </pre>
 * 
 * @author relai
 */
public class SpringDeployer {
    
    private final Container container;
    private Class springConfigClass;
    private int   instances = 1;
    private boolean multiThreaded = false;
    
    
    /**
     * Constructs the <code>SpringDeployer</code>.
     * 
     * @param container The vertx container
     */
    public SpringDeployer(Container container) {
        this.container = container;
    }
    
    /**
     * Specifies the Spring Boot Configuration class. This is required.
     * 
     * @param configClass Spring Boot application configuration class
     * @return this
     */
    public  SpringDeployer springConfigClass(Class configClass) {
        springConfigClass = configClass;
        return this;
    }
        
    /**
     * Specifies the worker verticle instance count. Default to 1.
     * 
     * @param count the number of worker verticle instances to create
     * @return this
     */
    public SpringDeployer instances(int count) {
        this.instances = count;
        return this;
    }
    
    /**
     * Specifies whether the worker verticle is multi-threaded. Default to false.
     * 
     * @param value true of false
     * @return  this
     */
    public SpringDeployer multiThreaded(boolean value) {
        this.multiThreaded = value;
        return this;
    }
    
    /**
     * Deploys the Spring Data worker verticle.
     */
    public void deploy() {
        deploy(null);
    }
    
    /**
     * Deploys the Spring worker verticle.
     * 
     * @param doneHandler the asynchronous done handler
     */
    public void deploy(AsyncResultHandler<String> doneHandler) {            
        if (springConfigClass == null) 
            throw new VertxException("Spring config class cannot be empty");
        
        JsonObject config = new JsonObject();
        config.putString(APP_CONTEXT , springConfigClass.getCanonicalName());
        container.deployWorkerVerticle(RepositoryVerticle.class.getCanonicalName(), 
                config, instances, multiThreaded, doneHandler);
    }
    
}
