# How to tune the performance of storm topologies
The Siembol services comprise of one or more topologies running in Storm. These services will in most cases require some performance tuning. Whilst this document will cover the common use-cases for performance tuning it will not be exhaustive. We highly recommend the reader familiarises themselves with the following excellent performance tuning guides about Storm and Kafka:

https://storm.apache.org/releases/2.2.0/Concepts.html

https://storm.apache.org/releases/current/Performance.html

https://www.confluent.io/blog/how-choose-number-topics-partitions-kafka-cluster/

All settings mentioned below to tune parallelism are configurable in the [siembol admin UI](../../siembol_ui/siembol_ui.md).

### Parallelism Concepts
As volumes of data increase, so will the parallelism requirements of storm topologies that make up Siembol services. A significant amount of trial and error will be required to optimise performance, but some high level rules exist that can help guide. 

The default configuration for a storm topology will most likely be one worker, one spout, one _processor_ and one writer. This is true for nearly all topologies running in Siembol. The Storm concepts link above gives a good overview of a topology, comprising of one or more workers which execute tasks (logical threads of execution).

As a first principle one should begin looking at increasing tasks (spouts, processors and writers). It is useful to use the Storm UI and view capacity values for bolts (spouts, processors, writers), where a value approaching one indicates that tuples are having to wait before entering bolts, indicating back pressure is occurring. A helpful visualization of these values also exists in the topology visualization section of the Storm UI. 

If you increase spout, processor and writers together, along with Kafka partitions you should see linear scalability of performance up until a point where the number of tasks in a single worker no longer yields linear improvement. At this point you should record the optimal maximum number of spouts, processors and writers of a worker in your environment. These should be the target size of workers, and you can scale workers horizontally across storm supervisor nodes. 

### Storm Topology Configuration
It is generally optimal to set the Kafka spout parallelism equal to the number of partitions used in your kafka topic. Increasing parallelism greater than the number of partitions will lead to idle spouts that do not process events. As partitions and spouts increase, so will the requirement for processing and writing tasks. Below is an example of how to increase spouts, processing and writing tasks using the [Siembol UI](../../siembol_ui/siembol_ui.md):

![](images/executors-alerts.jpg)

The fields to increase tasks in the alerting storm topology are: 

```
Kafka.spout.num.executors
Alerts.engine.bolt.num.executors
Kafka.writer.bolt.num.executors
```

When the limit of a topologies horizontal scalability (i.e. the limit of a single worker's processing power through increasing tasks) is met the next parallelism configuration option is the number of workers to run across one or more storm supervisor hosts. This can be specified in the admin configuration of a siembol service: 

![](images/topology-workers.jpg)

Increase the number of workers by changing `topology.workers`. 

The configuration of `topology.max.spout.pending` allows for the trade off latency and throughput. If this value is set reasonably low (i.e. 500) you can expect low latency but slightly reduced throughput. If this is increased (i.e. 5000) you could expect slightly greater throughput, but lower latency.

Siembol topologies implement a configurable batch writer to further improve throughput. The internal batch size of topologies can be set through the UI, by navigating to the `Admin Config -> Kafka Batch Writer Attributes`

![](images/kafka-batch-writer.jpg)

The internal kafka writer has a tick frequency of one second, which mean's if it has not filled it's batch it will flush and write to Kafka.