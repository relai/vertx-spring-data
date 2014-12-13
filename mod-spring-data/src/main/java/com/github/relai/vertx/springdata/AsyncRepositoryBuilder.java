package com.github.relai.vertx.springdata;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import org.springframework.data.repository.Repository;
import org.vertx.java.core.eventbus.EventBus;

/**
 * The builder class to create runtime asynchronous repository at the client side. 
 * 
 * <p>A simple example: 
 *   <pre> {@code
 *    AsyncRepositoryBuilder<Long> builder = new AsyncRepositoryBuilder<>(eventBus);
 *    AsyncCrudRepository<Long> client = builder.build(); 
 *   } </pre>
 * 
 * <p>A more complicated example showing how to set optional parameters: 
 *     <pre>{@code 
 *     AsyncRepositoryBuilder<Long> builder = new AsyncRepositoryBuilder<>(eventBus);
 *     AsyncTaskRepository<Long> client = 
 *        builder.timeout(1000)
 *               .repositoryInterface(TaskRepository.class)
 *               .build(AsyncTaskRepository.class);
 *     }</pre>
 * 
 * @author relai
 * @param <ID> The Id type of the repository entity
 */
public class AsyncRepositoryBuilder<ID extends Serializable>  {
    
    private final EventBus bus;
    private long timeout = 5000; // default to 5000 ms
    private Class<? extends Repository> repositoryInterface;
    
    /**
     * Constructs an instance with a vertx event bus
     * 
     * @param bus the event bus
     */
    public  AsyncRepositoryBuilder(EventBus bus) {
        this.bus = bus;
    }

    /**
     * Optionally sets the timeout for asynchronous invocation
     * 
     * @param milliseconds the timeout period. The default is 5000 ms.
     * @return this
     */
    public AsyncRepositoryBuilder<ID> timeout(long milliseconds) {
        timeout = milliseconds;
        return this;
    }
    
    /**
     * Optionally sets the {@code Repository} interface. This is not necessary if the 
     * Spring {@code ApplicationContext} used by the {@link RepositoryVerticle} contains
     * a single Repository object. 
     * 
     * @param repositoryInterface  the repository interface type
     * @return this
     */
    public AsyncRepositoryBuilder<ID> repositoryInterface(Class<? extends Repository> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
        return this;
    }
    
    /**
     * Builds a {@link AsyncCrudRepository} instance  
     * 
     * @return an instance of the asynchronous repository
     **/
    public AsyncCrudRepository<ID> build() {
         return new AsyncCrudRepositoryImpl(bus, repositoryInterface, timeout);
    }
    
    /**
     * Builds a specific AsyncRepository runtime Interface
     * 
     * @param <R> the type of the asynchronous repository to create
     * @param type the class of the asynchronous repository
     * @return an instance of the asynchronous repository
     */
    public <R> R build(Class<R> type){
       AsyncCrudRepositoryImpl impl = new AsyncCrudRepositoryImpl(bus, repositoryInterface, timeout);
       @SuppressWarnings("unchecked")
		R proxy = (R) Proxy.newProxyInstance(this.getClass().getClassLoader(),
            new Class[]{type}, new AsyncRepositoryProxyHandler(impl));
        return proxy;      
    }
    
}
