# Vert.x Spring Data Mod

This mod allows you to access data asynchronously by Spring Data inside a 
Vert.x application, making it straightforward to use JPA inside Vert.x.

[Spring Data](http://projects.spring.io/spring-data/) provides a declarative 
and unified way to access data. The data can be inside a traditional RDBMS via 
JPA.  It can also come from a NoSQL data store.

[Spring Boot](http://projects.spring.io/spring-boot/) jumps start the usage of 
Spring by adopting a convention-over-configuration approach. It largely 
eliminates the verbose xml that is often associated with Spring. 
Spring Boot is used in this project to reduce boiler-plating. 

## Features

Spring Data offers many goodies for data access:
* Auto generated Id
* Creation date
* Last update date
* Version for optimistic locking
* Paging and sorting
* Declarative [custom finder methods](http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods). 
In this regard, it is very similar to the famed GORM. You can declare custom finder
 methods such as findByLastnameAndFirstname and findByStartDateAfter and use it.

This project further makes it easy to expose the entities in a RESTful web service 
through RestHelper and YokeRestHelper.


## Sample Applications

* Shopping List REST web service using Spring Data JPA (HSQLDB and Hibernate JPA)
* Contact REST web service using Spring Data MongoDB (MongoDB)
* Todo list web application using Spring Data JPA (HSQLDB and Hibernate JPA)

## Sample: Shopping List Rest Web Service

### Step 1: create Spring Data domain classes

You need to create
* ShoppingItem: the entity class
* ShoppingItemRepository: the Spring Data Repository class
* Config: the Spring Boot ApplicationContext config class

### Step 2: create asynchronous repository interface - optional

The asynchronous repository is the companion interface to the synchronous ShoppingItemRepository

* AsyncShoppingItemRepository 

Note that in many cases you can use the stock AsyncCrudRepository or AsyncPagingAndSortingRepository. 
You need to create your own asynchronous repository only if you want to use a custom method.


### Step 3: instantiate asynchronous repository:

    AsyncRepositoryBuilder<String> builder = new AsyncRepositoryBuilder<>(vertx.eventBus());    
	AsyncShoppingItemRepository	client = builder.build(AsyncShoppingItemRepository.class);			

### Step 4: deploy the worker verticle

    SpringDeployer deployer = new SpringDeployer(container);
    deployer.springConfigClass(Config.class)
            .deploy(result -> startTests());  

### Step 5: use the asynchronous repository

You can now consume the asynchronous repository in your application. If you want
to expose the resource as a REST web service, a convenience helper class is provided.

    YokeRestHelper<Long> rest = new YokeRestHelper<>(client, Long.class);        
    Yoke yoke = new Yoke(vertx);      
    yoke.use(new BodyParser())
        .use(rest.createRouter("/shoppinglist"))
        .listen(PORT_NUMBER);
