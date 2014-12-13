# Contact Management REST Web Service

This is a sample REST web service application for Vertx Spring Data.

## Overview

###  MongoDB

While Spring Data is often used with JPA, Spring Data itself provides a fairly 
back-end-agnostic approach to data access. In this sample, MongoDB is used 
as the data store almost transparently.

To run the application, make sure MongoDB is locally running first.

### Auditing 


Spring Data MongoDB provides auditing features. In this sample, we use
* CreateDate
* LastModifiedDate


### Joda Time

Joda Time is fully support.  As a example, the lastModified is a Joda time.


## Step by Step

### Step 0: dependencies

Add dependency to `mod-spring-data` in mod.json:
  "includes": "com.github.relai.vertx.springdata~mod-spring-data~1.0-SNAPSHOT",

In pom, mark the dependency as provided:

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

### Step 2: create asynchronous repository interface - optional

Not needed. The stock AsyncCrudRepository is used.


### Step 3: instantiate asynchronous repository:

        AsyncRepositoryBuilder<String> builder
            = new AsyncRepositoryBuilder<>(vertx.eventBus());            
        AsyncPagingAndSortingRepository<String> repository = 
            builder.build(AsyncPagingAndSortingRepository.class);
        RestHelper<String> helper = new RestHelper<>(repository, String.class);		

### Step 4: use the asynchronous repository

        RestHelper<String> helper = new RestHelper<>(repository, String.class);                        
        getVertx().createHttpServer()
	    	.requestHandler(helper.createRouteMatcher("/contacts"))
	    	.listen(8080, "localhost");

### Step 5: deploy the worker verticle

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

### Update contacts

### Read contacts

