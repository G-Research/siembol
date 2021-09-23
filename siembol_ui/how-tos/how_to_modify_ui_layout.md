# How to modify ui layout
The ui-layout files are read by the backend and used to generate the schema then sent to the UI. They are useful to add/overwrite parts of the schema. 

## Prepared layout files per service types
The default layout files can be found in the git repo [here] (../../../config/config-editor-rest).

There is one per service and one for all testcases. The files are all in JSON format. The per service files are separated into 'config_layout' and 'admin_config_layout'. 
## How to change
We're using the ngx-formly library (https://github.com/ngx-formly/ngx-formly) to generate the forms. This library allows you to merge properties into a JSON schema by using the `widget` property. This is what we use in the layout files. 

To modify a part of the schema you have to reference its json path, the result of evaluation of the path should have at most one item. The json path syntax library we are using is: https://github.com/json-path/JsonPath. 
A couple of examples of what can be modified are: adding a help link next to a form field, changing title/description of a field, change UI design. 
### Title/Description
You can overwrite the title or description of fields (can be useful for translating for example). To do this first identify whether the field is in admin config or normal config so you put it in the right key in the JSON. Then find its unique JSON path and add the following into the layout file filling in the path, the title and/or description:

        "<JSON_PATH>": {
            "widget": {
                "formlyConfig": {
                    "title": "<NEW_TITLE>",
                    "description: "<NEW_DESCRIPTION>"
                }
            }
        }

### Adding help link
You can add a help link to the right of the field. One example is in the alert-layout file:

        "$..data": {
            "widget": {
                "formlyConfig": {
                    "type": "textarea",
                    "wrappers": ["form-field", "help-link"],
                    "templateOptions": {
                        "link": "https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html"
                    },
                    "expressionProperties": {
                        "templateOptions.showHelpLink": "model.matcher_type !== 'IS_IN_SET'"
                    }
                }
            }
        }
Here we're adding a help link for the unique `data` key. The `expressionProperties` can be omitted to add a new help link (by default it will be always shown), but can be used if we only want for it to be visible when a certain condition is satisfied. 

## How to change layout file per service
It is possible to add the modified layout files in the docker image. The path of the layout can be specified in the `application.properties` config file (see [here](how-tos/how_to_test_config_in_siembol_ui.md)). 