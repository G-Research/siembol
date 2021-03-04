# Siembol Parsing Services
## Overview
Siembol provides parsing services for normalising logs into messages with one layer of key/value pairs. Clean normalised data are very important for further processing such as alerting.
### Key concepts
- `Parser` is a siembol configuration that defines how to normalise a log 
- `Parsing app` is a stream application (storm topology) that combines one or multiple parsers and reads a log from kafka topics and produces normalised log to output kafka topics
### Common fields
These common fields are included in all siembol messages after parsing:
- `original_string` - The original log before normalisation
- `timestamp` - Timestamp extracted form the log in milliseconds since the UNIX epoch
- `source_type` - Data source - a siembol parser that was used for parsing the log 
- `guid` - Unique identification of the message
## Parser config
The configuration defines how the log is normalised
- `parser_name` - Name of the parser
- `parser_version` - Version of the parser
- `parser_author` - Author of the parser
- `parser_description`- Description of the parser
### Parser Attributes
- `parser_type` - The type of the parser
    - Netflow v9 parser - parses netflow payload and produces list of normalised messages. Netflow v9 parsing is based on templates and the parser is learning templates during parsing messages.
    - Generic parser - Creates two fields
        - `original_string` - The log copied from the input
        - `timestamp` - Current epoch time of parsing in  milliseconds. This timestamp can be overwriten in further parsing
    - Syslog Parser
        - `syslog_version` -  Expected version of the syslog message - `RFC_3164`, `RFC_5424`, `RFC_3164, RFC_5424`
        - `merge_sd_elements` - Merge SD elements of syslog message into one parsed object
        - `time_formats` - Time formats used for time formatting. Syslog default time formats are used if not provided 
        - `timezone` - Time zone used in syslog default time formats
### Parser Exctractors
Extractors are used for further extracting and normalising parts of the message. 
#### Overview
An extractor reads input field and produces the set of key value pairs extacted from the field. Each extractor is called in the chain and its produced messages are merged into the parsed message after finishing the extraction. This way next extractor in the chain can use outputs of the previous extractors. If the input field of the extractor is not part of the parsed message then the extractor execution is skipped and the next extractor in the chain is called. A preprocessing function of the extractor is called before the extraction in order to normalise and clean input field. Post-processing functions are called on extractor outputs in order to normalise output messages of the extractor.
#### Common extractor attributes
- `name` - The name of the extractor
- `field` - The field on which the extractor is applied
- `pre_processing_function` - The pre-processing function applied before the extraction
    - `string_replace` - Replace the first occurrence of `string_replace_target` by `string_replace_replacement` 
    - `string_replace_all` - Replace all occurrences of `string_replace_target` by `string_replace_replacement`. You can use regular expression in `string_replace_target`
- `post_processing_functions` - The list of post-processing functions applied after the extractor
    - `convert_unix_timestamp` - Convert `timestamp_field` in unix epoch timestamp in seconds to milliseconds 
    - `format_timestamp` - Convert `timestamp_field` using `time_formats` 
        - `validation_regex` - validation regular expression for checking format of the timestamp, if no match of the pattern then next formatter from the list is tried to apply
        - `time_format` using syntax from `https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html`
        - `timezone` - Time zone used by the time formatter
    - `convert_to_string` - Convert all extracted fields as strings except fields from the list `conversion_exclusions`
- `extractor_type` - The extractor type - one from `pattern_extractor`, `key_value_extractor`, `csv_extractor`, `json_extractor` 
- flags
    - `should_overwrite_fields` - Extractor should overwrite an existing field with the same name, otherwise create new field with prefix `duplicate`
    - `should_remove_field` - Extractor should remove input field after extraction
    - `remove_quotes` - Extractor removes quotes in the extracted values
    - `skip_empty_values` - Extractor will remove empty strings after the extraction
    - `thrown_exception_on_error`- Extractor throws an exception on error (recommended for testing), otherwise it skips the further processing
#### Pattern extractor
Extracting key value pairs by matching a list of regular expressions with named-capturing groups, where names  of the groups are used for naming fields. Siembol supports syntax from `https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html` except allowing to to use underscores in the names of captured groups
- `regular_expressions` - The list of regular expressions 
- `dot_all_regex_flag` - The regular expression `.` matches any character - including a line terminator
- `should_match_pattern` - At least one pattern should match otherwise the extractor throws an exception
#### Key value Extractor
Key value extractor extracts values from the field which has the form `key1=value1 ... keyN=valueN`
 - `word_delimiter`- Word delimiter used for splitting words, by dafault ` `
 - `key_value_delimiter`- Key-value delimiter used for splitting key value pairs, by default `=`;
 - `escaped_character`- Escaped character for escaping quotes, delimiters, brackets, by default `\\`;
 - `quota_value_handling` - Handling quotes during parsing
 - `next_key_strategy` - Strategy for key value extraction where key-value delimiter is found first and then the word delimiter is searched backward
 - `escaping_handling` - Handling escaping during parsing
#### CSV extractor
- `column_names` - Specification for selecting column names, where `skipping_column_name` is a name that can be used to not include a column with this name in the parsed message
- `word_delimiter` - Word delimiter used for splitting words 
#### Json Extractor
Json extractor extracts valid json message and unfold json into flatten json key value pairs.
- `path_prefix` - The prefix added to extracted field names after json parsing
- `nested_separator` - The separator added during unfolding nested json objects
### Parser Transformations
#### Overview
All key value pairs generated by parser and extractor can be modified by a chain of transformations. This stage allows the parser to clean data by renaming fields, removing  fields or even to filter the whole message. 
#### 
#### field name string replace
Replace the first occurrence of `string_replace_target` in field names by `string_replace_replacement`

#### field name string replace all
Replace all occurrences of `string_replace_target` by `string_replace_replacement`. You can use regular expression in `string_replace_target`
#### field name string delete all
Delete all occurrences of `string_replace_target`. You can use regular expression in `string_replace_target`
#### field name change case
Change case in all field names to `case_type`
#### rename fields
Rename fields according to mapping in `field_rename_map`, where you specify pairs of `field_to_rename`, `new_name`
#### delete fields
Delete fields according to the filter in `fields_filter`, where you specify the lists of patterns for `including_fields`, `excluding_fields`
#### trim value
Trim values in the fields according to the filter in `fields_filter`, where you specify the lists of patterns for  `including_fields`, `excluding_fields`
#### lowercase value
Lowercase values in the fields according to the filter in `fields_filter`, where you specify the lists of patterns for  `including_fields`, `excluding_fields`
#### uppercase value
Uppercase values in the fields according to the filter in `fields_filter`, where you specify the lists of patterns for `including_fields`, `excluding_fields`
#### chomp value
Remove ending new line from values in the fields according to the filter in `fields_filter`,  where you specify the lists of patterns for `including_fields`, `excluding_fields`
#### filter message
Filter logs that are matching the `message_filter`, where are specified `matchers` for filtering.
## Parsing application
### Overview
Parsers are integrated in a stream application (storm topology) that combines one or multiple parsers and reads a log from input kafka topics and produces a normalised log to output kafka topics when parsing is successful or to an error topic on error.
- `parsing_app_name` - The name of the parsing application
- `parsing_app_version` - The version of the parsing application
- `parsing_app_autho` - The author of the parsing application
- `parsing_app_description`- Description of the parsing application 
- `parsing_app_settings` - Parsing application settings
    - `parsing_app_type`- The type of the parsing application - `router_parsing` or `single_parser`
    - `input_topics` - The kafka topics for reading messages for parsing
    - `error_topic`- The kafka topic for publishing error messages
    - `input_parallelism` - The number of parallel executors for reading messages from the input kafka topics
    - `parsing_parallelism` - The number of parallel executors for parsing messages
    - `output_parallelism` - The number of parallel executors for publishing parsed messages to kafka
    - `parse_metadata` - Parsing json metadata from input key records using `metadata_prefix` added to metadata field names, by default `metadata_`
- `parsing_settings` - Parsing settings depends on parsing application type
### Single Parser
The application integrates single parser only.
- `parser_name` - The name of the parser from parser configurations
- `output_topic`- The kafka topic for publishing parsed messages
### Router parsing
The application integrates multiple parsers. First, the router parser parses the input message and after its processing  the next parser is selected from the list of parsers based on pattern matching of the routing field.
- `router_parser_name` - The name of the parser that will be used for routing
- `routing_field` - The field of the message parsed by the router that will be used for selecting next parser
- `routing_message` - The field of the message parsed by the router that will be routed to the next parser as a message for the further parsing
- `merged_fields` - The fields from the message parsed by the router that will be merged to a message parsed by next the next parser
- `default_parser` - The parser that should be used if no other parsers will be selected with `parser_name` and `output_topic`
- `parsers` - The list of parsers for the further parsing
    - `routing_field_pattern` - The pattern for selecting the parser
    - `parser_properties` - The properties of the selected parser with `parser_name` and `output_topic`
## Admin Config
- `topology.name.prefix` - The prefix that will be used to create a topology name using application name, by default `parsing`
- `client.id.prefix` - The prefix that will be used to create a kafka producer client id using application name
- `group.id.prefix`- The prefix that will be used to create a kafka group id reader using application name
- `zookeeper.attributes` - Zookeeper attributes for updating parser configurations 
    - `zk.url` - Zookeeper servers url. Multiple servers are separated by coma
    - `zk.path` - Path to a zookeeper node
- `kafka.batch.writer.attributes` - Global settings for kafka batch writer used if are not overridden
    - `batch.size` - The max size of batch used for producing messages
    - `producer.properties` - Defines kafka producer properties, see `https://kafka.apache.org/0102/documentation.html#producerconfigs`
- `storm.attributes` - Global settings for storm attributes used if are not overridden
    - `bootstrap.servers` - Kafka brokers servers url. Multiple servers are separated by coma
    - `first.pool.offset.strategy` - Defines how the kafka spout seeks the offset to be used in the first poll to kafka
    - `kafka.spout.properties` - Defines kafka consumer attributes for kafka spout such as group.id, protocol, see `https://kafka.apache.org/0102/documentation.html#consumerconfigs`
    - `poll.timeout.ms`- Kafka consumer parameter `poll.timeout.ms` used in kafka spout
    - `offset.commit.period.ms` - Specifies the period of time (in milliseconds) after which the spout commits to Kafka, see `https://docs.cloudera.com/HDPDocuments/HDP3/HDP-3.1.5/storm-moving-data/content/tuning_kafkaspout_performance.html`
    - `max.uncommitted.offsets`- Defines the maximum number of polled offsets (records) that can be pending commit before another poll can take place
    - `storm.config` - Defines storm attributes for a topology, see `https://storm.apache.org/releases/current/Configuration.html`
- `overridden_applications`- List of overridden settings for individual parsing applications. The overriden application is selected by `application.name` and `kafka.batch.writer.attributes` and `storm.attributes` are used from this setting in the parsing application
    
