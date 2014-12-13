package com.github.relai.vertx.springdata.example.rest.domain;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


@Configuration
@EnableAutoConfiguration
@EnableMongoAuditing
public class Config {
	
}
