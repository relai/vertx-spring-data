# Contact Management REST Web Service

This is a sample REST web service application for Vertx Spring Data on MongoDB.

## Overview

###  MongoDB

While Spring Data is often used with JPA, Spring Data itself provides a fairly 
back-end-agnostic approach to data access. In this sample, MongoDB is used 
as the data store almost transparently.


### Auditing 


Spring Data MongoDB provides handy auditing features. In this sample, we use
* CreateDate
* LastModifiedDate


### Joda Time

Joda Time is fully support.  As a example, `lastModified` field is a Joda time.


## Step by Step

### Step 0: dependencies

Add dependency to `mod-spring-data` in [mod.json](https://github.com/relai/vertx-spring-data/blob/master/example-rest-on-mongodb/src/main/resources/mod.json):

  "includes": "com.github.relai.vertx.springdata~mod-spring-data~0.5"

In [`pom`](https://github.com/relai/vertx-spring-data/blob/master/example-rest-on-mongodb/pom.xml), mark the dependency as provided:

    <dependency>
       <groupId>com.github.relai.vertx.springdata</groupId>
       <artifactId>mod-spring-data</artifactId>
       <version>1.0-SNAPSHOT</version>
       <scope>provided</scope>
    </dependency>  


To add the project-specific Spring Boot dependencies:

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>${spring.boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>  
        </dependencies>
  </dependencyManagement>

The dependencies are:
* group: org.springframework.boot, name: spring-boot-starter-data-mongodb
* group: joda-time, name: joda-time
* group: com.fasterxml.jackson.datatype, name: jackson-datatype-joda

### Step 1: Spring Data domain classes
* Contact: the entity class
* ContactRepository: the repository class
* Config: Spring Boot application context

### Step 2: create asynchronous repository interface

Not needed. The stock [AsyncCrudRepository](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/main/java/com/github/relai/vertx/springdata/AsyncCrudRepository.java) is used.


### Step 3: [instantiate](https://github.com/relai/vertx-spring-data/blob/master/example-rest-on-mongodb/src/main/java/com/github/relai/vertx/springdata/example/rest/ContactRestApp.java) asynchronous repository:

        AsyncRepositoryBuilder<String> builder
            = new AsyncRepositoryBuilder<>(vertx.eventBus());            
        AsyncPagingAndSortingRepository<String> repository = 
            builder.build(AsyncPagingAndSortingRepository.class);
        RestHelper<String> helper = new RestHelper<>(repository, String.class);		

### Step 4: [use](https://github.com/relai/vertx-spring-data/blob/master/example-rest-on-mongodb/src/main/java/com/github/relai/vertx/springdata/example/rest/ContactRestApp.java) the asynchronous repository

        RestHelper<String> helper = new RestHelper<>(repository, String.class);                        
        getVertx().createHttpServer()
	    	.requestHandler(helper.createRouteMatcher("/contacts"))
	    	.listen(8080, "localhost");

### Step 5: [deploy](https://github.com/relai/vertx-spring-data/blob/master/example-rest-on-mongodb/src/main/java/com/github/relai/vertx/springdata/example/rest/ContactRestApp.java) the worker verticle

        SpringDeployer deployer = new SpringDeployer(container);
        deployer.springConfigClass(Config.class)
                .deploy(result -> startedResult.setResult(null));

## How to run the application

1. The project is coded using Java 8.0. Make sure JDK 8 is used.
2. Start the mongodb at the default port 27017
3. Execute Maven command: `mvn vertx:runMod`

If you open the project inside NetBeans, you can directly run or debug the project.

The REST service end-point can be accessed at localhost:8080/contacts

### Create contacts

POST /contacts

{"firstName":"June",
"lastName":"Lee"}

Response:

{"firstName":"June",
"lastName":"Lee",
"created":"2014-12-13T05:16:28.199+0000",
"id":"548bcbac7bcb8d95dad48cfd",
"lastModified":"2014-12-13T05:16:28.199Z",
"version":0}

### Update contacts

PUT /contacts/548bcbac7bcb8d95dad48cfd

{"firstName":"June B","lastName":"Lee", "id":"548bcbac7bcb8d95dad48cfd", "version":0}

Response:

{"firstName":"June B","lastName":"Lee","id":"548bcbac7bcb8d95dad48cfd","lastModified":"2014-12-13T05:18:09.862Z","version":1}

