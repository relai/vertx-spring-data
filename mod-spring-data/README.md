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
through [RestHelper](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/main/java/com/github/relai/vertx/springdata/RestHelper.java)
 and [YokeRestHelper](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/main/java/com/github/relai/vertx/springdata/YokeRestHelper.java).



## Sample: Shopping List

The integration test, [Shopping List]((https://github.com/relai/vertx-spring-data/tree/master/mod-spring-data/src/test), 
also serves a good example of how to use the mod.

### Step 1: create Spring Data [domain classes](https://github.com/relai/vertx-spring-data/tree/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/domain)

We start with creating the usual for Spring Data:
* [ShoppingItem](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/domain/ShoppingItem.java): the entity class
* [ShoppingItemRepository](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/domain/ShoppingItemRepository.java): the Spring Data Repository class
* [Config](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/domain/Config.java): the Spring Boot ApplicationContext config class

### Step 2: create asynchronous repository interface

To invoke Spring Data, we use an asynchronous repository. The mod comes with two stock asynchronous repositories:

* [AsyncCrudRepository](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/main/java/com/github/relai/vertx/springdata/AsyncCrudRepository.java) 
* [AsyncPagingAndSortingRepository](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/main/java/com/github/relai/vertx/springdata/AsyncPagingAndSortingRepository.java)

In many cases you can just use one of the above interfaces directly. However, if 
you add custom methods to the Repository interface, you need to define your custom asynchronous repository. 

Since we add custom methods to our repository interface, we define the asynchronous repository accordingly:

* [AsyncShoppingItemRepository](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/AsyncShoppingItemRepository.java) 


### Step 3: [instantiate](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/ShoppingListTest.java) asynchronous repository:

    AsyncRepositoryBuilder<String> builder = new AsyncRepositoryBuilder<>(vertx.eventBus());    
	AsyncShoppingItemRepository	client = builder.build(AsyncShoppingItemRepository.class);			
  

### Step 4: [use](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/ShoppingListTest.java) the asynchronous repository

You can now consume the asynchronous repository in your application. 

If you want to expose the resource as a REST web service, a convenience helper class is provided.

    YokeRestHelper<Long> rest = new YokeRestHelper<>(client, Long.class);        
    Yoke yoke = new Yoke(vertx);      
    yoke.use(new BodyParser())
        .use(rest.createRouter("/shoppinglist"))
        .listen(PORT_NUMBER);

### Step 5: [deploy](https://github.com/relai/vertx-spring-data/blob/master/mod-spring-data/src/test/java/com/github/relai/vertx/springdata/integration/shoppingList/ShoppingListTest.java) the worker verticle

    SpringDeployer deployer = new SpringDeployer(container);
    deployer.springConfigClass(Config.class)
            .deploy(result -> startTests());

## More Sample applications

Full-blown sample applications are also provided:
* [Contact REST web service](https://github.com/relai/vertx-spring-data/tree/master/example-rest-on-mongodb) using Spring Data MongoDB (MongoDB)
* [Todosapp](https://github.com/relai/vertx-spring-data/tree/master/example-webapp-todosapp): a web application using Spring Data JPA (HSQLDB and Hibernate JPA)