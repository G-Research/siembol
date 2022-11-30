# Siembol Alerting Services

- [1. Overview](#overview)
- [2. Alert service](#alert-service)
  * [2.1 Common rule fields](#common-rule-fields)
  * [2.2 Alert rule](#alert-rule)
      * [2.2.1 Matchers](#matchers)
  * [2.3 Global Tags and Rule Protection](#global-tags-and-rule-protection)
- [3. Correlation Rule](#correlation-rule)
  * [3.1 Overview](#overview-1)
  * [3.2 Correlation alert rule](#correlation-alert-rule)
- [4. Admin config](#admin-config)
  * [4.1 Common admin config fields](#common-admin-config-fields)
  * [4.2 Alert admin config](#alert-admin-config)
  * [4.3 Correlation alert admin config](#correlation-alert-admin-config)

## Overview
Siembol alert is a detection engine used to filter matching events from an incoming data stream based on a configurable rule set. The correlation alert allows you to group several detections together before raising an alert.
## Alert service
### Common rule fields 
The fields that are common to alert and correlation alert.
- `rule_name` - Rule name that uniquely identifies the rule
- `rule_author` - The author of the rule - the user who last modified the rule
- `rule_version` - The version of the rule
- `rule_description` - This field contains a single text input that allows you set a description for the alert. This should be a short, helpful comment that allows anyone to identify the purpose of this alert.
- `tags` - Tags are optional but recommended as they allow you to add tags to the event after matching the rule. Each tag is a key-value pair. Both the key and the value inputs are completely free form allowing you to tag your rules in the way which works best for your organisation. You can use substitution in the value input to set the tag value equal to the value of a field from the event. The syntax for this is `${field_name}`
  - `tag_name` - The name of the tag
  - `tag_value` - The value of the tag. 

```
Note: if you want to correlate an alert in correlation engine that use the tag with name "correlation_key". This alert will be silent if you do not set the tag with name "correlation_alert_visible"
```
- `rule_protection` - Rule Protection allows you to prevent a noisy alert from flooding the components downstream. You can set the maximum number of times an alert can fire per hour and per day. If either limit is exceeded then any event that matches is sent to error instead of output topic until the threshold is reset. Rule Protection is optional.  If it is not configured for a rule, the rule will get the global defaults applied.
  - `max_per_hour` - Maximum alerts allowed per hour
  - `max_per_day` - Maximum alerts allowed per day

### Alert rule
- `source_type` - This fields allows you to determine the type of data you want to match on. It is essentially a matcher for the "source_type" field. This field does not support regex - however, using `*` as an input matches all source types. The `source_type` field is set during parsing and is equal to the name of the last parser which was used to parse the log.

```
Tip: if you want to match on multiple data sources, set the source type to be * and add a regex matcher (in the matcher section) to filter down to your desired source types.
```

#### Matchers
Matchers allow you to select the events you want the rule to alert on.
- `is_enabled` - The matcher is enabled
- `description` - The description of the matcher
- `matcher_type` - Type of matcher, either `REGEX_MATCH`, `IS_IN_SET`, `CONTAINS`, `NUMERIC_COMPARE`, `COMPOSITE_AND` or `COMPOSITE_OR`
- `is_negated`- The matcher is negated
    private Boolean negated = false;
- `field` - The name of the field on which the matcher will be evaluated

There are four types of matchers:
- `REGEX_MATCH` - A regex_match allows you use a regex statement to match a specified field. There are two string inputs:
    - `data`: the regex statement in Java using syntax from [https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) except allowing to use underscores in the names of captured groups Named capture groups in the regex are added as fields in the event. They are available from the next matcher onwards and are included in the output event.
- `IS_IN_SET` - An "is_in_set" matcher compares the value of a field to a set of strings defined in `data`. If the value is in the set then the matcher returns true. 
    - `data` - A list of strings to compare the value to. New line delimited. Does not support regex - each line must be a literal match however, field substitution is supported in this field.
    - `case_insensitive`- Use case-insensitive string compare
- `CONTAINS` - A "contains" matcher searches for substring defined in `data`. If the pattern is found the matcher returns true.
    - `data` - A pattern to search. Field substitution is supported in this field.
    - `case_insensitive` - Use case-insensitive string compare
    - `starts_with`- The field value starts with the pattern
    - `ends_with` - The field value ends with the pattern
- `NUMERIC_COMPARE` - A matcher compares field numeric value with the expression using various comparing types
    - `compare_type` - The type of comparing numbers, either `equal`, `lesser_equal`, `lesser`, `greater` or `greater_equal`
    - `expression` - A field numeric value will be compared with the expression. The expression can be a numeric constant or a string that contains a variable.
- `COMPOSITE_AND` - Used to combine matchers from the list `matchers` with AND logic operation
- `COMPOSITE_OR` - Used to combine matchers from the list `matchers` with OR logic operation

`Note : A composite matcher is recursive in alerting engine, however the level of recursion is limited to 3 in Siembol UI for simplicity`

### Global Tags and Rule Protection
Global tags and global rule protection are defined in the deployment of the rules. These are added to the alert after matching unless are overridden by individual rule settings. The global tag with the name `detection_source` is used to identify the detection engine that triggers the alert.
## Correlation Rule
### Overview
The correlation alert allows you to group several detections together before raising an alert. The primary use case for this is when you have a group of detections which individually shouldn't be alerted on (e.g. high volume or detections with high false positive rate) you can group several together to get more reliable alerts.
### Correlation alert rule
`correlation_attributes` field allows you to configure which detections to correlate together. 
  - `time_unit` - A field that allows you to configure the time unit to use, this is a fixed option with the choices:
    - `hours`
    - `minutes`
    - `seconds`  
  - `time_window` - A field to set the time window in the selected time unit for the correlation
  - `time_computation_type` - You can configure how the time window is calculated 
    - `event_time` - The time window is calculated using the `timestamp` field in the events, the `timestamp` field is usually computed during parsing from the log  
    - `processing_time` - The time window is calculated using the current time (when an alert is evaluated), the events need to be processed by the correlation alert component within the time window
  - `max_time_lag_in_sec` - The event with timestamp older than the current time minus the lag (in seconds) will be discarded
  - `alerts_threshold` - The alert's threshold allows you to configure how many detections (you can specify which detections later) need to trigger in the time window for the alert to trigger. This field accepts an integer value, if it is left empty then all detections need to trigger before an alert is created
    - `alerts` - The list of alerts for correlation
       - `alert` - The alert name used for correlation
       - `threshold` - The number of times the alert has to trigger in the time window
       - `mandatory` - The alert must pass the threshold for the rule to match 
  - `fields_to_send` - The list of fields of correlated alerts that will be included in the triggered alert after matching
## Admin config
### Common admin config fields
- `alerts.topology.name` - The name of storm topology
- `alerts.input.topics` - The list of kafka input topics for reading messages
- `kafka.error.topic` - The kafka error topic for error messages
- `alerts.output.topic` - The kafka output topic for publishing alerts
- `alerts.correlation.output.topic` - The kafka topic for alerts used for correlation by correlation rules
- `kafka.producer.properties` - Defines kafka producer properties, see [https://kafka.apache.org/0102/documentation.html#producerconfigs](https://kafka.apache.org/0102/documentation.html#producerconfigs)
- `zookeeper.attributes` - The zookeeper attributes for updating the rules
  - `zk.url` - Zookeeper servers url. Multiple servers are separated by comma
  - `zk.path` - Path to a zookeeper node or multiple nodes delimited by new line. Alerting rules from multiple zookeeper nodes can be loaded in order to save storm resources
- `storm.attributes` - Storm attributes for the enrichment topology
  - `bootstrap.servers` - Kafka brokers servers url. Multiple servers are separated by comma
  - `first.pool.offset.strategy` - Defines how the kafka spout seeks the offset to be used in the first poll to kafka
  - `kafka.spout.properties` - Defines kafka consumer attributes for kafka spout such as `group.id`, `protocol`, see [https://kafka.apache.org/0102/documentation.html#consumerconfigs](https://kafka.apache.org/0102/documentation.html#consumerconfigs)
  - `poll.timeout.ms`- Kafka consumer parameter `poll.timeout.ms` used in kafka spout
  - `offset.commit.period.ms` - Specifies the period of time (in milliseconds) after which the spout commits to Kafka, see [https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.5/storm-moving-data/content/tuning_kafkaspout_performance.html](https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.5/storm-moving-data/content/tuning_kafkaspout_performance.html)
  - `max.uncommitted.offsets`- Defines the maximum number of polled offsets (records) that can be pending commit before another poll can take place
  - `storm.config` - Defines storm attributes for a topology, see [https://storm.apache.org/releases/current/Configuration.html](https://storm.apache.org/releases/current/Configuration.html)
- `kafka.spout.num.executors` - The number of executors for reading from kafka input topic
- `alerts.engine.bolt.num.executors` - The number of executors for evaluating alerting rules
- `kafka.writer.bolt.num.executors` - The number of executors for producing alerts to output topic
### Alert admin config
- `alerts.engine` - This field should be set to `siembol_alerts`
### Correlation alert admin config
- `alerts.engine` - This field should be set to `siembol_correlation_alerts`
- `alerts.engine.clean.interval.sec` - The period in seconds for regular cleaning a rule correlation data that are not needed for the further rule evaluation
