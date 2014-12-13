package com.github.relai.vertx.springdata.integration.shoppingList.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "SHOPPING_LIST")
public class ShoppingItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue Long id;
    @Version int version;
    int priority;
    String name;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public int  getVersion() {return version;}
    public void setVersion(int version) {this.version = version;}

    public int  getPriority() {return priority;}
    public void setPriority(int priority) {this.priority = priority;}

    public String getName() {return name;}
    public void   setName(String name) {this.name = name;}
    
    
}