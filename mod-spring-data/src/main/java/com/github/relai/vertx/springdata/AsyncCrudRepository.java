package com.github.relai.vertx.springdata;

import java.io.Serializable;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * The asynchronous <code>Repository</code> interface, companion to Spring <code>CrudRepository</code>. 
 * This interface mirrors <code>CrudRepository</code>, offering event-driven CRUD 
 * operations through the vertx event bus.  
 *
 * <p>If a custom method is added to the repository interface and is needed at 
 * the client side, this interface should be extended by adding the 
 * corresponding asynchronous method. 
 *
 * <p>At runtime, {@link AsyncRepositoryBuilder} is responsible for creating a 
 * dynamic instance that implements the interface.   
 * 
 * @author relai
 * @param <ID> the entity Id type
 */

public interface AsyncCrudRepository<ID extends Serializable> {
 
    
    /**
	 * Saves an entity.
	 * 
	 * @param entity   the entity to save
	 * @param onreply  the response event handler. The payload is the updated entity.
	 */
     void save(JsonObject entity, MessageHandler<JsonObject> onreply);

	/**
	 * Saves all given entities.
	 * 
	 * @param entities the entities to save
	 * @param onreply  the response event handler
	 */
	 void  save(JsonArray entities, MessageHandler<JsonArray> onreply);

	/**
	 * Retrieves an entity by its id.
	 * 
	 * @param id must not be {@literal null}.
     * @param onreply  the response event handler. If the entity cannot be found,
     *                 the reply fails with error code NOT_FOUND (404).
     * @throws IllegalArgumentException if {@code id} is {@literal null}
	 */
	 void findOne(ID id, MessageHandler<JsonObject> onreply);

	/**
	 * Returns whether an entity with the given id exists. 
	 * 
	 * @param id must not be {@literal null}.
	 * @param onreply  the response event handler. 
	 * @throws IllegalArgumentException if {@code id} is {@literal null}
	 */
	void exists(ID id, MessageHandler<Boolean> onreply);

	/**
	 * Returns all instances of the type.
	 * 
	 * @param onreply  the response event handler.
	 */
	void findAll(MessageHandler<JsonArray> onreply);

	/**
	 * Returns all instances of the type with the given IDs.
	 * 
	 * @param ids the id's of the entities
	 * @param onreply  the response event handler.
	 */
	void findAll(Iterable<ID> ids, MessageHandler<JsonArray> onreply);

	/**
	 * Returns the number of entities available.
	 * 
	 * @param onreply  the response event handler.
	 */
	void count(MessageHandler<Long> onreply);

	/**
	 * Deletes the entity with the given id.
	 * 
	 * @param id must not be {@literal null}.
     * @param onreply  the response event handler.
	 * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
	 */
	void delete(ID id, MessageHandler<Void> onreply);

	/**
	 * Deletes a given entity.
	 * 
	 * @param entity   the entity to delete
     * @param onreply  the response event handler.
	 * @throws IllegalArgumentException in case the given entity is (@literal null}.
	 */
	void delete(JsonObject entity, MessageHandler<Void> onreply);

	/**
	 * Deletes the given entities.
	 * 
	 * @param entities the entities to delete
     * @param onreply  the response event handler.
	 * @throws IllegalArgumentException in case the given {@link Iterable} is (@literal null}.
	 */
	void delete(JsonArray entities, MessageHandler<Void> onreply);

	/**
	 * Deletes all entities managed by the repository.
     * 
     * @param onreply  the response event handler.
	 */
	void deleteAll(MessageHandler<Void> onreply);
}
