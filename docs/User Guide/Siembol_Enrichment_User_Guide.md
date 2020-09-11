# Siembol Enrichment User Guide
## Overview
Siembol Enrichment is an enrichment engine used to add useful data to events to assist in detection and investigations. As with the other components, enrichment rules can be created in the Siembol UI. Each rule is JSON and can be seen in the UI or in the enrichment store repo. 

The data that is used to enrich events is stored in JSON files in HDFS in the following format: 
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

. When creating a  rule you can specify the table to use, the column to join on, and the column to add to the event.

## Accessing the Enrichement Editor
The enrichments editor can be accessed from the home page of the Siembol UI.

[//]: # (Add photo of enrichment editor link on home page)

## Enrichment Editor UI
Similar to other components, the enrichment editor UI is split into two sections: the left hand side is the store while the right hand side is the deployment section. A deployed rule will show up in both sections

For a more detailed insight into this UI, read the [Editor Page](Siembol_General_Guide.md#editor-page) section of the general guide.

## Creating / Editing a rule
### Enrichment rule editor - tabs
There are four tabs:
 - Rule Description: allows you to provide a brief text description of what the rule does
 - Source Type: allows you to specify the source types to apply the enrichment on
 - Matchers: allows you to create matchers that allow you to define which events to enrich
 - Table Mappings: allows you to configure the enrichment to perform

 [//]: # (Add photo of tabs)

 #### Rule Description
 This tab simply allows you to enter a string description providing some context around the rule - eg what it does or which events it affects. This field is optional but recommeded.

[//]: # (Add photo of rule description tab)

 #### Source Type
 This tab allows you to specify the source type of events to apply the enrichment to. Essentially this is a literal string matcher for the source_type field of an event. 

 ``` 
 Tip: if you want to match multiple source types select * in the source type tab and then add a matcher on the source_type field in the matchers tab to match only the source types you want to match. 
 ```

#### Matchers
Matchers allow you to further filter the events that the enrichment will be applied to. 

There are two types of matchers, as described in the [General Guide](Siembol_General_Guide.md#matchers).

To add a new matcher, click the "Add to Matchers" button. 

[//]: # (Add picture of add to matchers button)

#### Table Mapping
The table mapping tab is where you configure the enrichment you want to perform.

The "Table Name" field should be the name of thetable which contains the data you want to enrich the event with. 

The "Joining Key" field should be the string used to join the event with the table (the key json field). This field supports substitution eg `${field_name}` or `http://${host_field_name}/${path_field_name}`. This is used to filter the key field of the table. 

[//]: # (Add picture of table name and joining key fields)

To add data from the table to the event click the "Add to Enriching Fields" button. You will then have two fields to fill:
- Table field name: the column in the enrichment table that you want to add
- Event field name: the name you want the field to have in event.

You can add as many enriching fields as you want. 

```
Note: you can only enrich from one table per rule. If you want to enrich the same event from multiple table, you need to create multiple rules.
```

[//]: # (Add picture of enriching field section)