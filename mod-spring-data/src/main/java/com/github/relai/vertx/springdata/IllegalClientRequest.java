package com.github.relai.vertx.springdata;

/**
 * Thrown to indicate an illegal request is made by the client.
 * 
 * @author relai
 */
@SuppressWarnings("serial")
public class IllegalClientRequest extends RuntimeException {
    
    /**
     * Constructs a <code>IllegalClientRequest</code> with the specified error message
     * 
     * @param message the detailed error message
     */
    public IllegalClientRequest(String message) {
        super(message);
    }

    /**
     * Constructs a <code>IllegalClientRequest</code> with the specified error message
     * and the cause
     * 
     * @param message the detailed error message
     * @param cause  the exception cause
     */
     public IllegalClientRequest(String message, Throwable cause) {
        super(message, cause);
    }
  
}
