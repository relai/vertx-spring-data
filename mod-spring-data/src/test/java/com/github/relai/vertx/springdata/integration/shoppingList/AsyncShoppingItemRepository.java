package com.github.relai.vertx.springdata.integration.shoppingList;

import org.springframework.data.domain.Pageable;
import org.vertx.java.core.json.JsonArray;

import com.github.relai.vertx.springdata.AsyncCrudRepository;
import com.github.relai.vertx.springdata.MessageHandler;
import org.springframework.data.domain.Sort;


interface AsyncShoppingItemRepository extends AsyncCrudRepository<Long>{
    void findByPriority(int priority, MessageHandler<JsonArray> onreply);
    void findByNameIsNull(MessageHandler<JsonArray> onreply);
    void findAll(Pageable pageable, MessageHandler<JsonArray> onreply);
    void findAll(Sort sort, MessageHandler<JsonArray> onreply);
}
