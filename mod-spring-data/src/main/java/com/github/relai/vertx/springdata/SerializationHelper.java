package com.github.relai.vertx.springdata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.vertx.java.core.VertxException;

/** 
 * Helper functions to serialize and de-serialize an object array to and from a byte array.
 * 
 * @author relai
 */
public class SerializationHelper {

    public static byte[] toBytes(Object[] args) {
        byte[] bytes = null;
        try (ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ObjectOutputStream ops = new ObjectOutputStream(bs)) {
            ops.writeObject(args);
            bytes = bs.toByteArray();
        } catch (IOException ex) {
           throw new VertxException(ex);
        }
        return bytes;
    }

    public static Object[] fromBytes(byte[] bytes) {
        Object[] args = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis)) {
            args = (Object[]) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
        	throw new VertxException(ex);
        }
        return args;
    }
    
    
}
