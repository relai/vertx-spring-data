package com.github.relai.vertx.springdata;

import java.io.Serializable;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.eventbus.EventBus;

import static com.github.relai.vertx.springdata.RepositoryVerticle.*;


/**
 * Internal class that implements the {@link AsyncCrudRepository} interface.
 * 
 * @author relai
 */
class AsyncCrudRepositoryImpl<ID extends Serializable> implements AsyncCrudRepository<ID> {
    private final EventBus bus;
    private final Class<?> repositoryType;   
    private final long timeout; // default is  5000 ms

    public AsyncCrudRepositoryImpl(EventBus bus, Class<?> repositoryInterface, long timeout) {
        this.bus = bus;
        this.repositoryType = repositoryInterface;
        this.timeout = timeout;
    }
    
    @Override
    public void findAll(MessageHandler<JsonArray> onreply) {
       process(FINDALL, null, null, onreply);
    }
    
	@Override
	public void findAll(Iterable<ID> ids, MessageHandler<JsonArray> onreply) {
		JsonArray idArray = new JsonArray();
		for(ID id: ids) {
            validateId(id);
			idArray.add(id);
		}
		JsonObject command = new JsonObject();
		command.putString(ACTION, FINDALL)
		       .putArray(ID, idArray);
		process(command, onreply);
	}

    @Override
    public void findOne(ID id, MessageHandler<JsonObject> onreply) {
       validateId(id); 
       process(FINDONE, id, null, onreply);
    }

    @Override
    public void save(JsonObject entity, MessageHandler<JsonObject> onreply) {
        validateEntity(entity);
        process(SAVE, null, entity, onreply);
    }

	@Override
	public void save(JsonArray entities, MessageHandler<JsonArray> onreply) {
        validateEntity(entities);
		process(SAVE, null, entities, onreply);		
	}
	
    @Override
    public void delete(ID id,  MessageHandler<Void> onreply) {
       validateId(id); 
       process(DELETE, id, null, onreply);
    }
    
	@Override
	public void delete(JsonObject entity, MessageHandler<Void> onreply) {
        validateEntity(entity);
		process(DELETE, null, entity, onreply);			
	}

	@Override
	public void delete(JsonArray entities, MessageHandler<Void> onreply) {
        validateEntity(entities);
	    process(DELETE, null, entities, onreply);	
	}

	@Override
	public void deleteAll(MessageHandler<Void> onreply) {
		process(DELETEALL, null, null, onreply);			
	}
    

	@Override
	public void exists(ID id, MessageHandler<Boolean> onreply) {	
        validateId(id);
		process(EXISTS, id, null, onreply);
	}


	@Override
	public void count(MessageHandler<Long> onreply) {
		process(COUNT, null, null, onreply);	
	}

    
    void process(String action, ID id, JsonElement entity,  
    		 MessageHandler<?> onreply) {
                
        JsonObject command = new JsonObject();
        command.putString(ACTION, action);
        if (id != null) {
            command.putValue(ID, id);
        }        
        if (entity != null) {
            command.putElement(ENTITY, entity);
        }      
        process(command, onreply);
    }
    
    void process(JsonObject command, MessageHandler<?> onreply) {
        if (repositoryType != null) {
            command.putString(REPOSITORY, repositoryType.getCanonicalName());
        }
        String address = RepositoryVerticle.getEventBusAddress();
        bus.sendWithTimeout(address, command, timeout, onreply);
    }
    
    private void validateId(ID id) {
         if (id == null) 
           throw new IllegalArgumentException("The input Id cannot be null");            
    }
    
    private void validateEntity(JsonElement el) {
        if (el == null)
            throw new IllegalArgumentException("The entity or entities cannot be null.");
    }
}
