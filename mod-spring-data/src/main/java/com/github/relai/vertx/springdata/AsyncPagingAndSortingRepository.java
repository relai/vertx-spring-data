package com.github.relai.vertx.springdata;


import java.io.Serializable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.vertx.java.core.json.JsonArray;

/**
 * The asynchronous <code>Repository</code> interface, companion to Spring {@code PagingAndSportingReportory}.
 * 
 * @author relai
 * @param <ID> The Id type of the entity
 */
public interface AsyncPagingAndSortingRepository<ID extends Serializable> extends 
    AsyncCrudRepository<ID> {    
            
    /**
	 * Returns a {@code Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
	 * 
	 * @param pageable the paging hint
	 * @param onreply the response event handler
	 */    
    void findAll(Pageable pageable, MessageHandler<JsonArray> onreply);
    
    /**
	 * Returns all entities sorted by the given options.
	 * 
	 * @param sort the sort hint
	 * @param onreply the response event handler
	 */
    void findAll(Sort sort, MessageHandler<JsonArray> onreply);    
}
