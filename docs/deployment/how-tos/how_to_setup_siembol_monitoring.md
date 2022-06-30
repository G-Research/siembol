# How to set up Siembol monitoring
Siembol monitoring is currently composed of one component: Siembol heartbeat.

## Siembol Heartbeat
Siembol heartbeat is a springboot application with two main components: kafka producers and a kafka consumer. The kafka producers send heartbeat messages to kafka at an interval of time.  The kafka consumer reads the messages after they have been processed by various Siembol services and calculate total latency and latency between Siembol services.  

The properties of the heartbeat are defined in its `application.properties` file.

### General properties
- the interval in seconds the producers will send heartbeat messages, the default is 60 seconds
```properties
siembol-monitoring.heartbeat-properties.heartbeat-interval-seconds=60
```
- any optional additional fields to add to the heartbeat message
```properties
siembol-monitoring.heartbeat-properties.message.key1=value1
siembol-monitoring.heartbeat-properties.message.key2=value2
```

### Producer properties
One or multiple producers can be defined in the properties to monitor data from different kafka clusters, for example

```properties
siembol-monitoring.heartbeat-properties.heartbeat-producers.local-kafka-cluster.output-topic=siembol.heartbeat
siembol-monitoring.heartbeat-properties.heartbeat-producers.local-kafka-cluster.kafka-properties.[bootstrap.servers]=kafka-0.kafka-headless.siembol.svc.cluster.local:9092
siembol-monitoring.heartbeat-properties.heartbeat-producers.local-kafka-cluster.kafka-properties.[security.protocol]=PLAINTEXT
```
Any arbitrary additional kafka producer properties can be added (https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html).

### Consumer properties
```properties
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[bootstrap.servers]=kafka-0.kafka-headless.siembol.svc.cluster.local:9092
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[application.id]=siembol.heartbeat.reader
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[auto.offset.reset]=earliest
siembol-monitoring.heartbeat-properties.heartbeat-consumer.kafka-properties.[security.protocol]=PLAINTEXT
siembol-monitoring.heartbeat-properties.heartbeat-consumer.enabled-services=parsingapp,enrichment,response
```
Any arbitrary additional kafka streams properties can be added (https://kafka.apache.org/10/documentation/streams/developer-guide/config-streams.html).
The `enabled-services` property is to specify between which Siembol services latency should be computed.


