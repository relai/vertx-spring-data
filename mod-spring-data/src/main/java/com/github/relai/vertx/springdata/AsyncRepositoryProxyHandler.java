package com.github.relai.vertx.springdata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.vertx.java.core.json.JsonObject;

/**
 * Internal class of a dynamic proxy implementation of the asynchronous repository.
 * 
 * @author relai
 */
class AsyncRepositoryProxyHandler implements InvocationHandler {

    AsyncCrudRepositoryImpl<?> impl;
    
    AsyncRepositoryProxyHandler(AsyncCrudRepositoryImpl<?> impl) {
        this.impl = impl;
    }
    
	@Override    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
       if (method.getDeclaringClass().equals(AsyncCrudRepository.class)) {          
    	   method.invoke(impl, args);
       } else {
           JsonObject command = new JsonObject();
           command.putString(RepositoryVerticle.ACTION, method.getName());
           Object[] variables = new Object[args.length - 1];
           System.arraycopy(args, 0, variables, 0, args.length - 1);               
           @SuppressWarnings("rawtypes")
		   MessageHandler onreply = (MessageHandler)args[args.length -1];              
           command.putBinary(RepositoryVerticle.ARGS, 
               SerializationHelper.toBytes(variables));
           impl.process(command, onreply);                                        
       }     
       return null;
    }
    
}
