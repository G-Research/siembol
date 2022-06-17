# How to set up Siembol monitoring
Siembol monitoring is currently composed of one component: Siembol heartbeat.

## Siembol Heartbeat
Siembol heartbeat is a springboot application with two main components: kafka producers and a kafka consumer. The kafka producers send heartbeat messages to kafka at an interval of time.  The kafka consumer reads the messages after they have been processed by various Siembol services and calculate total latency and latency between Siembol services.  

The properties of the heartbeat are defined in its `application.properties` file.

### General properties
The general properties are:
- the interval in second the producers will send heartbeat messages, the default is 60:
```properties
siembol-monitoring.heartbeat-properties.heartbeat-interval-seconds=60
```
- any additional fields to add to the heartbeat message:
```properties
siembol-monitoring.heartbeat-properties.message.key1=value1
siembol-monitoring.heartbeat-properties.message.key2=value2
```

### Producer properties
Multiple producers can be defined in the properties, for example:

```properties
siembol-monitoring.heartbeat-properties.heartbeat-producers.producer1.output-topic=siembol.heartbeat
siembol-monitoring.heartbeat-properties.heartbeat-producers.producer1.kafka-properties.[bootstrap.servers]=kafka-0.kafka-headless.siembol.svc.cluster.local:9092
siembol-monitoring.heartbeat-properties.heartbeat-producers.producer1.kafka-properties.[security.protocol]=PLAINTEXT 

siembol-monitoring.heartbeat-properties.heartbeat-producers.producer2.output-topic=siembol.heartbeat-2
siembol-monitoring.heartbeat-properties.heartbeat-producers.producer2.kafka-properties.[bootstrap.servers]=kafka-0.kafka-headless.siembol.svc.cluster.local:9092
siembol-monitoring.heartbeat-properties.heartbeat-producers.producer2.kafka-properties.[security.protocol]=PLAINTEXT 
```

Here two producers will be sending heartbeat messages to the same kafka cluster but different kafka topics. Any additional kafka producer config parameter can be added.

### Consumer properties
The properties of the consumer can be defined as below:
```properties
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[bootstrap.servers]=kafka-0.kafka-headless.siembol.svc.cluster.local:9092
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[application.id]=siembol.heartbeat.reader
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[auto.offset.reset]=earliest
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[security.protocol]=PLAINTEXT
siembol-monitoring.heartbeat-properties.heartbeat-consumer.enabled-services=parsingapp,enrichment,response
```
Any additional kafka consumer config parameter can be added.

The `enabled-services` property is to specify between which Siembol services latency should be computed. Only put services that are running.

