package com.github.relai.vertx.springdata;


import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.eventbus.Message;



/**
 * A convenience interface for AsyncResultHandler.  
 * 
 *  The supported return data types from the event bus include:
 *  <ul>
 *    <li> JsonObject, to represent an entity object
 *    <li> JsonArray,  to represent a collection of entity objects
 *    <li> void
 *    <li> long: to represent all number
 *    <li> boolean
 *    <li> String
 *  </ul>
 * 
 * @author relai
 */
public interface MessageHandler<M> extends AsyncResultHandler<Message<M>>{
    
}
