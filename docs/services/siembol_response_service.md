# Siembol Response Service

- [1. Overview](#overview)
- [2. Siembol Response Rule](#siembol-response-rule)
  * [2.1 Evaluation](#evaluation)
  * [2.2 Response Rule](#response-rule)
  * [2.3 Provided evaluators](#provided-evaluators)
      * [2.3.1 Fixed result](#fixed-result)
      * [2.3.2 Matching](#matching)
      * [2.3.3 Json path assignment](#json-path-assignment)
      * [2.3.4 Markdown table formatter](#markdown-table-formatter)
      * [2.3.5 Array markdown table formatter](#array-markdown-table-formatter)
      * [2.3.6 Array reducer](#array-reducer)
      * [2.3.7 Alert throttling](#alert-throttling)
      * [2.3.8 Sleep](#sleep)
      * [2.3.9 Kafka writer](#kafka-writer)
      * [2.3.10 Time exclusion](#time-exclusion)
- [3. Plugins](#plugins)
  * [3.1 Plugin architecture](#plugin-architecture)
  * [3.2 Evaluators - GR open source plans](#evaluators-\-\-gr-open-source-plans)
- [4. Application Properties](#application-properties)
  * [4.1 Authentication](#authentication)
    * [4.1.1 Oauth2 Authentication](#oauth2-authentication)

## Overview
Siembol response is a service for defining a response to an alert. It brings a functionality: 
- To integrate siembol with other systems such as jira, ldap, elk, the hive, cortex etc.
- Enriching the alert with possibility of filtering it in order to speed up the incident response process and to reduce false positives
- Supporting alert throttling to reduce noise from alerts that are usually triggered in a bulk of alerts
- The pluggable interface allows to  easily develop and integrate a plugin with custom evaluators if needed

## Siembol Response Rule 
### Evaluation
The rules are ordered and evaluated similarly to firewall table - first match will stop further evaluation. Each rule can return 
- `match` - The alert was matched by the rule and the evaluation of the alert has been finished
- `no_match` - The alert was not matched by the rule and the next rule in the list will be evaluated
- `filtered` - - The alert was filtered by the rule and the evaluation of the alert has been finished

A rule contains a list of evaluators which are evaluated during the rule evaluation. An evaluator can return the same values as a rule - `match`, `no_match`, `filtered`. A rule returns `match` if all its evaluators return `match`, see
![response evaluation diagram](images/response_evaluation.svg)

### Response Rule 
- `rule_name` - Rule name that uniquely identifies the rule
- `rule_author` - The author of the rule - the user who last modified the rule
- `rule_version` - The version of the rule
- `rule_description` - This field contains a single text input that allows you set a description for the alert. This should be a short, helpful comment that allows anyone to identify the purpose of this rule
- `evaulators` - The list of evaluators for the rule evaluation. Each evaluator contains:
    - `evaluator_type` - The type of the response evaluator
    - `evaluator_attributes` - The attributes of the evaluator

### Provided evaluators
#### Fixed result
Fixed evaluator always returns evaluation result from its attributes.
- `evaluator_type` - The type equals to `fixed_result`
- `evaluator_attributes`
    -`evaluation_result` - The evaluation result returned by the evaluator

#### Matching
Matching evaluator evaluates its matchers and returns evaluation result from its attributes.
- `evaluator_type` - The type equals to `matching`
- `evaluator_attributes`
    - `evaluation_result` - The evaluation result returned by the evaluator after matching from `match`, `filtered`, `filtered_when_no_match`
    - `matchers` - You can add as many matchers as you want.
        - `is_enabled` - The matcher is enabled
        - `description` - The description of the matcher
        - `matcher_type` - Type of matcher, either `REGEX_MATCH` or `IS_IN_SET`
        - `is_negated`- The matcher is negated
        - `field` - The name of the field on which the matcher will be evaluated

There are two types of matchers:
- `REGEX_MATCH` - A regex_match allows you use a regex statement to match a specified field. There are two string inputs:
    - `data` - The regex statement in [Java data time formatter syntax](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) except allowing to use underscores in the names of captured groups Named capture groups in the regex are added as fields in the event. They are available from the next matcher onwards and are included in the output event

- `IS_IN_SET` - It compares the value of a field to a set of strings defined in `data`. If the value is in the set then the matcher returns true. 
    - `data` - A list of strings to compare the value to. New line delimited. Does not support regex - each line must be a literal match however, field substitution is supported in this field

#### Json path assignment
Json path assignment evaluator allows you to assign values from json path evaluation of a current alert into a field from its attributes.
- `evaluator_type` - The type equals to `json_path_assignment`
- `evaluator_attributes`
    - `assignment_type` - The type of the assignment based on json path evaluation define the return value. The values are from `match_always`, `no_match_when_empty`, `error_match_when_empty`
    - `field_name` - The name of the field in which the non-empty result of the json path evaluation will be stored
    - `json_path`- Json path for evaluation using syntax from [https://github.com/json-path/JsonPath](https://github.com/json-path/JsonPath)

#### Markdown table formatter
Markdown table formatter evaluator formats json object into markdown table that can be used in the description of tickets in tracking systems such as Jira or the Hive.
- `evaluator_type` - The type equals to `markdown_table_formatter`
- `evaluator_attributes`
    - `field_name` - The name of the field in which the computed markdown table will be stored
    - `table_name` - The name of the table
    - `fields_column_name` - The name of the column of the generated table with key names
    - `values_column_name` - The name of the column of the generated table with object values
    - `field_filter` - The field filter used for defining the alert fields in the table, where you specify the lists of patterns for `including_fields`, `excluding_fields`

#### Array markdown table formatter
Array Markdown table formatter evaluator formats json array into markdown table that can be used in the description of tickets in tracking systems such as Jira or the Hive.
- `evaluator_type` - The type equals to `array_markdown_table_formatter`
- `evaluator_attributes`
    - `field_name` - The name of the field in which the computed markdown table will be stored
    - `table_name` - The name of the table
    - `array_field` - The array field of the alert that will be formatted in the table
    - `field_filter` - The field filter used for defining the alert fields in the table, where you specify the lists of patterns for `including_fields`, `excluding_fields`

#### Array reducer
Array reducer evaluator allows to reduce a json array to a field. Json arrays are usually generated by evaluators for searching databases, performing API calls etc. 
- `evaluator_type` - The type equals to `array_reducer`
- `evaluator_attributes`
    - `array_reducer_type`- The type of the array reducer from `first_field`, `concatenate_fields`
    - `array_field` - The field name of the array that will be used for reducing
    - `prefix_name` - The prefix for creating field names where the 
    - `field_name_delimiter`- The delimiter that is used for generating field name from the prefix and an array field name
    - `field_filter` - The field filter used for defining fields for computation, where you specify the lists of patterns for `including_fields`, `excluding_fields`

#### Alert throttling
Alert throttling evaluator allows to filter similar alerts in defined time windows. This can help to reduce noise from alerts that are usually triggered in a bulk of alerts
- `evaluator_type` - The type equals to `alert_throttling`
- `evaluator_attributes`
    - `suppressing_key`- The key for suppressing alerts in specified time window
    - `time_unit_type`- The type of time unit from `minutes`, `hours`, `seconds`
    - `suppression_time` - The time for alert to be suppressed in the time units

#### Sleep
Sleep evaluator is postponing the further evaluation of the rule by time provided in its attributes.
- `evaluator_type` - The type equals to `sleep`
- `evaluator_attributes`
    - `time_unit_type`- The type of time unit from `seconds`, `milli_seconds`
    - `sleeping_time` - The time of sleeping in the time units

#### Kafka writer
Kafka writer evaluator produces a message with alert to a kafka topic.
- `evaluator_type` - The type equals to `kafka_writer`
- `evaluator_attributes`
  - `topic_name`- The name of the kafka topic

#### Time exclusion
Time exclusion evaluator interprets the timestamp in provided time zone and evaluates patterns for excluding alerts.
- `evaluator_type` - The type equals to `time_exclusion`
- `evaluator_attributes`
  - `timestamp_field` -  The name of the milliseconds epoch timestamp field  which will be used for evaluating the exclusion
  - `time_zone` - Timezone which will be used for interpreting the timestamp
  - `months_of_year_pattern` - Months of year pattern, where months are numbers from 1 to 12
  - `days_of_week_pattern` - Days of week pattern, where days are numbers from 1 to 7
  - `hours_of_day_pattern` - Hours of day pattern, where hours are numbers from 0 to 23
  - `result_if_not_excluded` - Evaluation result from `no_match` or `match` that the evaluator returns if the alert is not excluded

## Plugins
Siembol response plugins allows to extend the functionality of siembol response by integrating custom evaluators if needed. A response plugin can contain one or more evaluators and siembol response can load one or more plugins if needed.

### Plugin architecture
A Siembol response plugin is a shaded jar file that includes all its dependencies see [how to write response plugin](how-tos/how_to_write_response_plugin.md). The plugins can be copied in a directory where they will be loaded by the [springboot properties launcher](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html) The plugin is also integrated into siembol UI and its evaluators can be used in the similar way as the ones provided directly by siembol response.

###  Evaluators - GR open source plans

Evaluators implemented internally at GR that are planned to become open source:

- Elk search 
    - calling an Elastic Search query using LUCENE or json query syntax
- Elk store 
    - storing an alert in Elastic Search index
- The Hive alert
    - creating an alert in [The hive Security Incident Response Platform](https://thehive-project.org/)
- The Hive case
    - creating a case in [The hive Security Incident Response Platform](https://thehive-project.org/)
- Ldap search
    - searching ldap queries and evaluating a user group memberships
- Cortex analysis
    - launching an analysis in [Cortex analysis and active response engine](https://github.com/TheHive-Project/CortexDocs)
- Jira search
    - searching Jira issues using Jira Query Language
- Jira create issue
    - create an issue in a Jira project
- Papermill notebook
    - launching a Jupyter Notebook using [papermill service](https://papermill.readthedocs.io/en/latest/) 

## Application Properties

- `siembol-response.zookeper-attributes.zk-url` - Zookeeper servers url. Multiple servers are separated by comma
- `siembol-response.zookeper-attributes.zk-path` - A path to Zookeeper node for synchronisation of the siembol response rules generated by siembol UI

- `siembol-response.input-topic` - An input Kafka topic for reading alerts
- `siembol-response.error-topic` - An output Kafka topic for publishing error messages 

- `siembol-response.stream-config` - Kafka streams properties from [kafka streams configuration](https://kafka.apache.org/documentation/#streamsconfigs). We suggest to define at least the following properties:
    - `application.id` - An identifier for the siembol response stream processing application
    - `bootstrap.servers` - A list of host/port pairs to use for establishing the initial connection to the Kafka cluster
    - `security.protocol` - Protocol used to communicate with Kafka brokers
    - `num.stream.threads` - The number of threads for siembol response stream rule evaluation
    - `auto.offset.reset` - It defines what to do when there is no initial offset in Kafka for reading alerts 

### Authentication
- `siembol-response-auth.type` - Type of authentication from `disabled`, `oauth2`
#### Oauth2 Authentication
- `siembol-response-auth.oauth2.excluded-url-patterns` - The url patterns of siembol response which are excluded for oauth2 authorisation and allows unauthenticated access. We suggest to include `/info,/health,/health/liveness,/health/readiness,/metrics,/v3/api-docs/**,/swagger-ui/**,/swagger-ui.html

- `siembol-response-auth.oauth2.audience` - The audience registered in the identity provider

- `siembol-response-auth.oauth2.tokenUrl` - The url to the token endpoint of the identity provider

- `siembol-response-auth.oauth2.authorizationUrl` - The url to the authorisation endpoint of the identity provider

- `siembol-response-auth.oauth2.issuerUrl` - The url to the JWT issuer - the identity provider

- `siembol-response-auth.oauth2.jwkSetUrl` - The url to the set of public keys of the issuer

- `siembol-response-auth.oauth2.jwsAlgorithm` - We suggest to use `RS256` 

- `siembol-response-auth.oauth2.jwtType` - We suggest to use `at+jwt`

- `siembol-response-auth.oauth2.scopes` - We suggest to include `openid`

- swagger ui properties if needed 
    - `springdoc.pathsToMatch`- We suggest to include `/health,/info,/metrics,/api/**,/user`
    - `springdoc.show-actuator` - - We suggest to set `true`
    - `springdoc.swagger-ui.oauth.clientId` - The client name for swagger ui that is registered in the identity provider
    - `springdoc.swagger-ui.oauth.appName` - The audience `aud` registered in the identity provider
    - `springdoc.swagger-ui.oauth.clientSecret` - The Secret in PKCE flow is not confidential and could be provided in the properties
    - `springdoc.swagger-ui.oauth.usePkceWithAuthorizationCodeGrant` - This value must be `TRUE`


