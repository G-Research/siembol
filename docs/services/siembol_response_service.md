# Siembol Response Service
## Overview
Siembol response is a service for defining a response to an alert. It brings a functionality: 
- To integrate siembol with other systems such as jira, ldap, elk, the hive, cortex etc.
- Enriching the alert with possibility of filtering it in order to speed up the incident response process and to reduce false positives
- Supporting alert throttling to reduce noise from alerts that are usually triggered in a bulk of alerts
- The pluggable interface allows to  easily develop and integrate a plugin with custom evaluators if needed
## Siembol Response Rule 
### Evaluation
The rules are ordered and evaluated similarly to firewall table - first match will stop further evalation. Each rule can return 
- `match` - The alert was matched by the rule and the evaluation of the alert has been finished
- `no_match` - The alert was not matched by the rule and the next rule in the list will be evaluated
- `filtered` - - The alert was filtered by the rule and the evaluation of the alert has been finished

A rule contains a list of evaluators which are evaluated during the rule evaluation. An evaluator can return the same values as a rule - `match`, `no_match`, `filtered`. A rule returns `match` if all its evaluators return `match`.
![parser_flow](images/response_evaluation.svg)
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
        - `matcher_type` - Type of matcher, either `REGEX_MATCH` or `IS_IN_SET`
        - `is_negated`- The matcher is negated
        - `field` - The name of the field on which the matcher will be evaluated

There are two types of matchers:
- `REGEX_MATCH` - A regex_match allows you use a regex statement to match a specified field. There are two string inputs:
    - `data` - The regex statement in Java syntax [https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) except allowing to to use underscores in the names of captured groups Named capture groups in the regex are added as fields in the event. They are available from the next matcher onwards and are included in the output event

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
## Plugins
### Plugin architecture
### Evaluators implemented internally at GR that we are planning to open source
#### Elk search
#### Elk store
#### The hive alert
#### The hive case
#### Ldap search
#### Cortex analysis
#### Jira search
#### Jira create issue
#### Papermill notebook
## Application Properties