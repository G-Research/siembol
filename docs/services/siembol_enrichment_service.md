# Siembol Enrichment Service

- [1. Overview](#overview)
  * [1.1 Enrichment rule](#enrichment-rule)
      * [1.1.1 Matchers](#matchers)
      * [1.1.2 Table Mapping](#table-mapping)
- [2. Admin config](#admin-config)

## Overview
Siembol Enrichment is an enrichment engine used to add useful data to events to assist in detection and investigations.  

The data that is used to enrich events is stored in JSON files in a file store in the following format: 
```
{ 
    "key" :
    {
        "column1":"value",
         "column2":"value2",
         ...
    }
}
 ``` 

 When creating a rule you can specify the table to use, the column to join on, and the column to add to the event.

### Enrichment rule 
- `rule_name` - Rule name that uniquely identifies the rule
- `rule_author` - The author of the rule, i.e., the user who last modified the rule
- `rule_version` - The version of the rule
- `rule_description` - This field contains a single text input that allows you set a description for the rule. This should be a short, helpful comment that allows anyone to identify the purpose of this rule
- `source_type` - This fields allows you to determine the type of data you want to match on. It is essentially a matcher for the `source_type` field. This field does not support regex - however, using `*` as an input matches all source types. The source_type field is set during parsing and is equal to the name of the last parser which was used to parse the log
- `matchers` - Matchers allow you to further filter the events that the enrichment will be applied to
- `table_mapping` - Mappings for enriching events

#### Matchers
Matchers allow you to further filter the events that the enrichment will be applied to. You can add as many matchers as you want.
- `is_enabled` - The matcher is enabled
- `description` - The description of the matcher
- `matcher_type` - Type of matcher, either `REGEX_MATCH` or `IS_IN_SET`
- `is_negated`- The matcher is negated
- `field` - The name of the field on which the matcher will be evaluated

There are two types of matchers:
- `REGEX_MATCH` - A regex_match allows you use a regex statement to match a specified field. There are two string inputs:
    - `data` - The regex statement in Java syntax [https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) except allowing to use underscores in the names of captured groups Named capture groups in the regex are added as fields in the event. They are available from the next matcher onwards and are included in the output event

- `IS_IN_SET` - It compares the value of a field to a set of strings defined in `data`. If the value is in the set then the matcher returns true. 
    - `data` - A list of strings to compare the value to. New line delimited. Does not support regex - each line must be a literal match however, field substitution is supported in this field
    
#### Table Mapping
The table mapping tab is where you configure the enrichment you want to perform.

- `table_name` - The name of the table which contains the data you want to enrich the event with
- `joining_key` - The string used to join the event with the table (the key json field). This field supports substitution e.g. `${field_name}` or `http://${host_field_name}/${path_field_name}`. This is used to filter the key field of the table 
- `tags`- Tags are added into the event after successful joining the table with the joining key. You can add as many tags as you want
    - `tag_name` - The name of the tag
    - `tag_value` - The value of the tag

- `enriching_fields` - Fields from the enriching table that are added after successful joining the table with the joining key. You can add as many enriching fields as you want
    - `table_field_name` - The column in the enrichment table that you want to add
    - `event_field_name` - The name you want the field to have in event after enriching
```
Note: you can only enrich from one table per rule. If you want to enrich the same event from multiple table, you need to create multiple rules.
```
## Admin config
- `topology.name`- The name of storm topology
- `kafka.spout.num.executors` - The number of executors for kafka spout
- `enriching.engine.bolt.num.executors` - The number of executors for enriching rule engine
- `memory.enriching.bolt.num.executors` - The number of executors for memory enrichments from tables
- `merging.bolt.num.executors` - The number of executors for merging enriched fields
- `kafka.writer.bolt.num.executors` - The number of executors for producing output messages
- `enriching.rules.zookeeper.attributes` - The zookeeper attributes for updating enrichment rules
    - `zk.url` - Zookeeper servers url. Multiple servers are separated by comma
    - `zk.path` - Path to a zookeeper node
- `enriching.tables.zookeeper.attributes` - The zookeeper attributes for notifying the update of enrichment tables
    - `zk.url` - Zookeeper servers url. Multiple servers are separated by comma
    - `zk.path` - Path to a zookeeper node
- `kafka.batch.writer.attributes` - Kafka batch writer attributes for producing output messages
    - `producer.properties` - Defines kafka producer properties, see [https://kafka.apache.org/0102/documentation.html#producerconfigs](https://kafka.apache.org/0102/documentation.html#producerconfigs)
- `storm.attributes` - Storm attributes for the enrichment topology
- `bootstrap.servers` - Kafka brokers servers url. Multiple servers are separated by comma
    - `first.pool.offset.strategy` - Defines how the kafka spout seeks the offset to be used in the first poll to kafka
    - `kafka.spout.properties` - Defines kafka consumer attributes for kafka spout such as `group.id`, `protocol`, see [https://kafka.apache.org/0102/documentation.html#consumerconfigs](https://kafka.apache.org/0102/documentation.html#consumerconfigs)
    - `poll.timeout.ms`- Kafka consumer parameter `poll.timeout.ms` used in kafka spout
    - `offset.commit.period.ms` - Specifies the period of time (in milliseconds) after which the spout commits to Kafka, see [https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.5/storm-moving-data/content/tuning_kafkaspout_performance.html](https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.5/storm-moving-data/content/tuning_kafkaspout_performance.html)
    - `max.uncommitted.offsets`- Defines the maximum number of polled offsets (records) that can be pending commit before another poll can take place
    - `storm.config` - Defines storm attributes for a topology, see [https://storm.apache.org/releases/current/Configuration.html](https://storm.apache.org/releases/current/Configuration.html)
- `enriching.input.topics`- The list of kafka input topics for reading messages
- `enriching.output.topic` - Output kafka topic name for correctly processed messages
- `enriching.error.topic` - Output kafka topic name for error messages
- `enriching.tables.hdfs.uri` - The url for hdfs cluster where enriching tables are stored
