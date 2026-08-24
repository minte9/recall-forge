## Architecture / Architecture / Architectural Styles / Event-Driven Architecture

### 1. Pub/Sub vs Point-to-Point

In point-to-point messaging, one producer sends to one consumer via a queue -- the message is removed once processed. In pub/sub, a producer publishes an event and any number of subscribers receive their own copy. Pub/sub decouples the producer from knowing who's listening, which is what makes event-driven architecture composable.

~~~java
// Pub/sub: producer doesn't know who's listening
eventBus.publish(new OrderPlaced(orderId));
~~~

### 2. Message Brokers and Decoupling

A broker (Kafka, RabbitMQ) sits between producer and consumers so they never call each other directly -- the producer doesn't know if zero, one, or ten services are listening, and consumers can be added later without touching the producer. That decoupling is the payoff; the broker becomes a new critical dependency to operate.

~~~java
// Producer has no reference to any consumer
broker.publish("order.placed", orderEvent);

// Consumer, registered independently, elsewhere entirely
broker.subscribe("order.placed", event -> inventory.reserve(event));
~~~

### 3. Eventual Consistency Trade-offs

Once services communicate through events instead of direct calls, there's a window where different services see different, temporarily inconsistent state -- the order service knows an order was placed before the inventory service has processed the event and reserved stock. Designs must tolerate that window, not assume immediate consistency.

~~~java
orderService.place(order);       // order marked PLACED immediately
inventoryService.reserve(order); // handled async, seconds later
// A read here can see PLACED with stock not yet reserved
~~~
