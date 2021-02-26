# Siembol Alert User Guide
## Overview
Siembol Alert is a detection engine used to filter matching events from an incoming data stream based on a configurable rule set. 
## Alert service
### Alert Rule
#### Rule Description
This section contains a single text input that allows you set a description for the alert. This should be a short, helpful comment that allows anyone to identify the purpose of this alert.

[//]: # (TODO add image of rule description)

#### Source Type
This section allows you to determine the type of data you want to match on. It is essentially a matcher for the "source_type" field. This field does not support regex - however, using * as an input matches all source types.

The source_type field is set during parsing and is equal to the name of the parser config which was used to parse the event.

```
Tip: if you want to match on multiple data sources, set the source type to be * and add a regex matcher (in the matcher section) to filter down to your desired source types.
```

#### Matchers
Matchers allow you to select the events you want the rule to alert on.


#### Tags
Tags are optional but recommended as they allow you to add tags to the event after matching the rule.

Each tag is a key-value pair. Both the key and the value inputs are completely freeform allowing you to tag your rules in the way which works best for your organisation.

You can use substitution in the value input to set the tag value equal to the value of a field from the event. The syntax for this is `{field_name}` eg:

[//]: # (TODO add image of substitution in tag value)

#### Rule Protection
Rule Protection allows you to prevent a noisy alert from flooding the components downstream. You can set the maximum number of times an alert can fire per hour and per day. If either limit is exceeded then any event that matches is filtered and not sent on to Siembol Response until the threshold is reset.

Rule Protection is optional.  If it is not configured for a rule, the rule will get the global defualts applied (global defaults are set during the deployment process - see below).

1. [Global Tags](#global_tags)
2. [Global Rule Protection](#global_rule_protection)

[//]: # (TODO add image of deploy options)

### Global Tags

[//]: # (TODO add image of global tags)

### Global Rule Protection
## Alert admin config
## Correlation Alert service

### Overview
The correlation alert allows you to group several detections together before raising an alert. The primary use case for this is when you have a group of detections which indvidually shouldn't be alerted on (eg high volume or detections with high false positive rate) you can group several together to get more reliable alerts.
TODO: correlation key, time window better to describe


### Correlation alert rule

#### Rule description
 This tab simply allows you to enter a string description providing some context around the rule - eg what it does or which events it affects. This field is optional but recommeded.

#### Correlation attributes
The correlation attributes tab allows you to configure which detections to correlate together. 

The "Time Unit" field allows you to configure the time unit to use, this is a fixed option with the choices:
- hours
- minutes
- seconds

This is used in conjuction with the "Time Window" field to set the time window for the correlation. The "Time Window" field accepts an integer value. The requirements within this time window vary depending on the options you select - this is discussed further below.

You can also configure how the time window is calculated using the "Time computation type". There are two values for this:
- event_time: the time window is calculated using the timestamp field in the events, the timestamps need to be inside the time window for an alert to trigger
- processing_time: the time window is calcualted using the current time (when an alert is matched), the events need to be processed by the correlationalert component within the time window

The alerts threshold allows you to configure how many detections (you can specify which detections later) need to trigger in the time window for the alert to trigger. This field accepts an integer value, if it is left empty then all detections need to to trigger before an alert is created. 

For each detection you have three fields:
- Alert name: string, name of the detection (as named in the alert component)
- Threshold: integer, the number of times the detection has to trigger in the time window
- Mandatory: checkbox, whether a detection has to trigger within a time window in order for the rule to match

If the mandatory field is checked, the detection has to trigger before an alert is created - even if the alert threshold has already been reached. 

If the threshold field is set to more than 1, then if that condition is fulfilled it only counts as 1 detection towards the total alerts threshold. 

#### Tags
This is the same as the tags section in the [alert editor](Siembol_Alert_User_Guide.md#tags)
#### Rule Protection
This is the same as the tags section in the [alert editor](Siembol_Alert_User_Guide.md#rule-protection)

## Correlation alert admin config
