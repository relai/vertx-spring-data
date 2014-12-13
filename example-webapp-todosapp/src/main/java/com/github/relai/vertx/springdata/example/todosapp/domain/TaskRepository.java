package com.github.relai.vertx.springdata.example.todosapp.domain;



import java.util.List;

import org.springframework.data.repository.CrudRepository;


/**
 * Task repository. 
 * 
 * @author relai
 */

public interface TaskRepository extends CrudRepository<Task,Long> {	
	List<Task> findByCompleted(boolean completed);
}
