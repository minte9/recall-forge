## Architecture / Architecture / Scalability & Performance / Database Scaling

### 1. Vertical vs Horizontal Scaling

Vertical scaling means a bigger machine -- more CPU, RAM, faster disk -- for the same single database; simple, but it has a ceiling and that machine is still a single point of failure. Horizontal scaling spreads data or load across multiple machines, which has no hard ceiling but requires the application to handle data being split up.

~~~java
// Vertical: one instance, more resources
// db.instanceType = "db.r5.4xlarge"

// Horizontal: same workload spread across many instances
// shard1.instanceType == shard2.instanceType == "db.r5.large"
~~~

### 2. Read Replicas

A read replica is a copy of the primary database that serves read-only queries, taking load off the primary so it can focus on writes. Replication is asynchronous by default, so a replica can lag slightly behind the primary -- reading your own just-written data from a replica can return stale results.

~~~java
primaryDb.save(order);                     // writes go to the primary
Order order = replicaDb.findById(id);      // reads may be a bit stale
~~~

### 3. Sharding Strategies

Sharding splits a dataset across multiple databases by some key -- customer ID, region -- so each shard only holds a fraction of the data and load. Choosing the shard key matters more than the mechanism: a bad key, like a sequential ID, concentrates writes on one shard, while a well-distributed key spreads them evenly.

~~~java
// Sequential ID: concentrates writes on the "hot" last shard
int shard = orderId % shardCount;

// Hashed customer ID: spreads writes evenly
int shard = hash(customerId) % shardCount;
~~~
