package com.github.relai.vertx.springdata.example.rest;

import com.github.relai.vertx.springdata.AsyncPagingAndSortingRepository;
import com.github.relai.vertx.springdata.AsyncRepositoryBuilder;
import org.vertx.java.platform.Verticle;

import com.github.relai.vertx.springdata.RestHelper;
import com.github.relai.vertx.springdata.SpringDeployer;
import com.github.relai.vertx.springdata.example.rest.domain.Config;
import org.vertx.java.core.Future;


/**
 * A sample Contact REST web service 
 **/
public class ContactRestApp extends Verticle {

	@Override
	public void start(Future<Void> startedResult) {  
        
        AsyncRepositoryBuilder<String> builder
            = new AsyncRepositoryBuilder<>(vertx.eventBus());            
        AsyncPagingAndSortingRepository<String> repository = 
            builder.build(AsyncPagingAndSortingRepository.class);
        
        RestHelper<String> helper = new RestHelper<>(repository, String.class);                        
        getVertx().createHttpServer()
	    	.requestHandler(helper.createRouteMatcher("/contacts"))
	    	.listen(8080, "localhost");
        
          
        SpringDeployer deployer = new SpringDeployer(container);
        deployer.springConfigClass(Config.class)
                .deploy(result -> startedResult.setResult(null));
	}
}
