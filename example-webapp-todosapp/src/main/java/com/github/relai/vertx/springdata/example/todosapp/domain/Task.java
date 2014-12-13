package com.github.relai.vertx.springdata.example.todosapp.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * The persistent class for the TASKS database table.
 * 
 */

@Entity
@Table(name = "Tasks")
@NamedQuery(name = "Task.findAll", query = "SELECT t FROM Task t")
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue Long id;    
    @Version int version;
    @NotNull @Size(min=1) String name;
    String priority;
    String description;
    boolean completed;
     
    public Task() {  }

    public Long getId() {return id;}
    
    public int getVersion() {return version;}
    public void putVersion(int version) {this.version = version;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = name;}

    public String getPriority() {return priority;}
    public void setPriority(String priority) {this.priority = priority;}
    
    public boolean getCompleted() {return completed;}
    public void setCompleted(boolean completed) {this.completed = completed;}
}
