# Messaging Utilities
The utility offers one class, `Event`, an interface `MessageQueue`, and a default implementation of `MessageQueue` using RabbitMq using the class `RabbitMqQueue`.

Installation is done via `mvn clean install`.

The classes are used like follows. To create a queue one uses the constructor of `RabbitMqQueue` with the argument the hostname of host where RabbitMq is running on. The default is `"localhost"`.

```
MessageQueue q = new RabbitMqQueue();
MessageQueue q = new RabbitMqQueue("<hostname>");
````

Then one publishes an event using

```
q.publish(new Event("<topic>", new Object[] { ... }));
```

To install a handler for a given topic, one can use

```
q.addHandler("<topic>", this::method);
```

where `this::method` is a method with one argument of class `Event`, handling the event.

# Running the tests
Currently, the tests are disabled. To run the tests, the comments have to be removed from the `// @Test` annotation. Furthermore, the tests require a running RabbitMq server that has to have their ports mapped to corresponding localhost ports, as described in the `docker-compose.yml` file:

```version: '3'
services:
  rabbitMq:
    image: rabbitmq:3-management
    container_name: rabbitMq_container
    ports:
     - "5672:5672"
     - "15672:15672"
```
 
The RabbitMq server is started by executing `docker-compose up`.

Then `mvn clean test` runs the tests.
