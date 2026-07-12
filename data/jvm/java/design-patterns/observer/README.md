## JVM / Java / Design Patterns / Observer

### 1. Observer Basics

The Observer pattern lets one object (the subject) notify a list of registered listeners whenever its state changes, without the subject needing to know anything about what those listeners actually do. Listeners register and unregister themselves dynamically, decoupling the source of an event from whatever reacts to it.

~~~java
interface OrderListener { void onOrderPlaced(String orderId); }

class OrderService {
    private final List<OrderListener> listeners = new ArrayList<>();

    void addListener(OrderListener listener) { listeners.add(listener); }

    void placeOrder(String orderId) {
        listeners.forEach(l -> l.onOrderPlaced(orderId)); // notify every registered listener
    }
}
~~~


### 2. PropertyChangeListener

java.beans.PropertyChangeListener is the JDK's own built-in Observer implementation: a PropertyChangeSupport helper manages the listener list for you, and firePropertyChange() notifies everyone registered whenever a named property changes value.

~~~java
class Thermostat {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private double temperature;

    void addListener(PropertyChangeListener l) { support.addPropertyChangeListener(l); }

    void setTemperature(double temp) {
        double old = temperature;
        temperature = temp;
        support.firePropertyChange("temperature", old, temp); // notifies all listeners
    }
}
~~~


### 3. Spring's ApplicationEvent as Observer

Spring's event system is Observer at the framework level: publish an ApplicationEvent through the ApplicationContext, and every bean with a matching @EventListener method gets called automatically, fully decoupled from the publisher. Commonly used to react to things like a user registration without coupling that logic into the registration code itself.

~~~java
class OrderPlacedEvent extends ApplicationEvent {
    OrderPlacedEvent(Object source) { super(source); }
}

@Component
class OrderEventPublisher {
    @Autowired ApplicationEventPublisher publisher;
    void placeOrder() { publisher.publishEvent(new OrderPlacedEvent(this)); }
}

@Component
class EmailNotificationListener {
    @EventListener // called automatically when OrderPlacedEvent is published
    void onOrderPlaced(OrderPlacedEvent event) { /* send email */ }
}
~~~
