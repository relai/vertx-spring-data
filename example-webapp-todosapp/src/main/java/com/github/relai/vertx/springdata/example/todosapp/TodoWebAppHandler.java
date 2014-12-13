package com.github.relai.vertx.springdata.example.todosapp;

import static io.netty.handler.codec.http.HttpResponseStatus.CONFLICT;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_ACCEPTABLE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.github.relai.vertx.springdata.MessageHandler;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;

/**
 * Todosapp web application handlers.
 * 
 * @author relai
 */
class TodoWebAppHandler {
    
    // form attributes
    private final static String ID = "id";
    private final static String NAME = "name";
    private final static String DESCRIPTION = "description";
    private final static String PRIORITY = "priority";
    private final static String COMPLETED = "completed";
    private final static String VERSION = "version";
    
    private final static String ACTION = "action";
    private final static String FILTER = "filter";
    
    AsyncTaskRepository client;
    
    TodoWebAppHandler(AsyncTaskRepository client) {
        this.client = client;
    }
    
    void getTasks(YokeRequest request) {
        String filter = request.getParameter(FILTER, "all");
        request.put(FILTER, filter);
        
        MessageHandler<JsonArray> onreply = (reply) -> 
    		onDBResult(reply, request, ()->	{
    			request.put("todos", reply.result().body().toArray());
     	        request.response().render("task-table.hbs");
    		});
               
        if ("all".equals(filter)) {
            client.findAll(onreply);
        } else {
            Boolean completed = "done".equals(filter);           
            client.findByCompleted(completed, onreply);
        }
    }
    
    void getTask(YokeRequest request) {
        client.findOne(id(request),  
        	(reply) -> onDBResult(reply, request, ()->	{
	 			request.put("todo", reply.result().body().toMap());
	            request.put("isInserting", false);
	            request.response().render("task-form.hbs");
	 		}));
    }
     
    
    void createTask(YokeRequest request) {
        request.put("isInserting", true);
        request.response().render("task-form.hbs");
    }
    
    @SuppressWarnings("unchecked")
	void postTask(YokeRequest request) {
        Long id = id(request);
                   
        @SuppressWarnings("rawtypes")
		MessageHandler onreply = (reply) -> 
        	onDBResult((AsyncResult<?>) reply, request,
        			() -> request.response().redirect("/tasks"));
        	
        String action = request.getFormParameter(ACTION, "");
        switch(action) {
            case "save":
                JsonObject entity = getForm(request);               
                client.save(entity, onreply);
                break;
            case "delete":
                client.delete(id, onreply);
                break;
            default:
                request.response().setStatusCode(500).end();
        }      
    }
    
    
    private JsonObject getForm(YokeRequest request) {
        JsonObject entity = new JsonObject();
        entity.putString(NAME, request.getFormParameter(NAME));
        entity.putString(DESCRIPTION, request.getFormParameter(DESCRIPTION));
        entity.putString(PRIORITY, request.getFormParameter(PRIORITY));
        entity.putBoolean(COMPLETED, "on".equals(request.getFormParameter(COMPLETED)));
        
        Long id = id(request);
        if (id != null) {
            entity.putNumber(ID, id);
        }
        
        String version = request.getFormParameter(VERSION);
        if (version != null && version.isEmpty() == false) {
            entity.putNumber(VERSION, Integer.valueOf(version));
        }

        return entity;
    }

    private Long id(YokeRequest request) {
    	String strId = request.getParameter(ID, "").trim();
    	
        return strId.isEmpty()? null : Long.valueOf(strId);
    }
            
    private void onDBResult(AsyncResult<?> result, 
			YokeRequest request, Runnable work) {
		if (result.succeeded()) {
			work.run();
		} else {
			Throwable ex = result.cause();
			onError(request, ex);
		}
	}
    
    private void onError(YokeRequest request, Throwable ex) {
        String message = null;
        if (ex instanceof ReplyException) {
            ReplyException rex = (ReplyException) ex;
            if (rex.failureCode() == CONFLICT.code()) {
               message = "The record was modified or deleted by someone else.";
            } else if (rex.failureCode() == NOT_FOUND.code()) {
               message = "The record cannot be found.";
            } else if (rex.failureCode() == NOT_ACCEPTABLE.code()) {
               message = rex.getMessage();
            } 
        } 
        message = (message == null) ? "Something went wrong." : message;
        request.put("message", message);
        request.put("exception", ex);
        request.put("url", request.uri());     
        request.response().render("error.hbs");
    }
}
