package com.github.relai.vertx.springdata.integration.shoppingList.domain;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;




public interface ShoppingItemRepository extends CrudRepository<ShoppingItem,Long> {
	
   List<ShoppingItem> findByNameIsNull();
   List<ShoppingItem> findByPriority(int priority);
   
   Page<ShoppingItem> findAll(Pageable pageable);
   Iterable<ShoppingItem> findAll(Sort sort);
}
