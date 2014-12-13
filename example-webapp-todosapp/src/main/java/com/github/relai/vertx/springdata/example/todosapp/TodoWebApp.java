package com.github.relai.vertx.springdata.example.todosapp;

/**
 * Todo sample web application.
 * 
 * <p>This sample shows how to create a server-rendered web application using Vertx
 * Spring Data. 
 * 
 * 
 * @author relai
 */


import com.github.relai.vertx.springdata.example.todosapp.domain.Config;
import com.jetdrone.vertx.yoke.*;
import com.jetdrone.vertx.yoke.engine.HandlebarsEngine;
import com.jetdrone.vertx.yoke.middleware.*;

import org.vertx.java.platform.Verticle;

import com.github.relai.vertx.springdata.*;
import com.github.relai.vertx.springdata.example.todosapp.handlebars.IfEqualHelper;
import org.vertx.java.core.Future;



public class TodoWebApp extends Verticle {   

    @Override
    public void start(Future<Void> startedResult) {                    
        // 
        // Router middleware
        //
        Router router = new Router();
        
        //Server-based web app
        AsyncRepositoryBuilder<Long> builder
            = new AsyncRepositoryBuilder<>(vertx.eventBus());  
        AsyncTaskRepository client = builder.build(AsyncTaskRepository.class);

        TodoWebAppHandler webApp = new TodoWebAppHandler(client);
        router.get("/tasks",         webApp::getTasks)
              .get("/tasks/create",  webApp::createTask)
              .post("/tasks/create", webApp::postTask)
              .get("/tasks/:id",     webApp::getTask)
              .post("/tasks/:id",    webApp::postTask);
        
        //
        // Template engine middlware
        //
        HandlebarsEngine handlebars = new HandlebarsEngine("handlebars");
        handlebars.registerHelper("ifEqual", new IfEqualHelper());
       
        //
        // Start Yoke
        //
        Yoke yoke = new Yoke(vertx);      
        yoke.use(new BodyParser())
            .use(new Static("webroot"))
            .engine(handlebars)
            .use(router)
            .listen(8080);
        
        //
        // Deploy Srping Data mod
        //
        SpringDeployer deployer = new SpringDeployer(container);
        deployer.springConfigClass(Config.class)
                .deploy(it -> startedResult.setResult(null));
    }  
    
  
}
