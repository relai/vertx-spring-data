package com.github.relai.vertx.springdata.example.rest.domain;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactRepository extends MongoRepository<Contact,String> {
}
