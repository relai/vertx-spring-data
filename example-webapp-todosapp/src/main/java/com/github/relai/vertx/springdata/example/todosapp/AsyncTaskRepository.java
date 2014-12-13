package com.github.relai.vertx.springdata.example.todosapp;

import org.vertx.java.core.json.JsonArray;

import com.github.relai.vertx.springdata.AsyncCrudRepository;
import com.github.relai.vertx.springdata.MessageHandler;

/**
 * Asynchronous Task repository. A custom finder API is added.
 * 
 * @author relai
 */
 public interface AsyncTaskRepository extends AsyncCrudRepository<Long> {
    void findByCompleted(Boolean completed, MessageHandler<JsonArray> onreply);
}
