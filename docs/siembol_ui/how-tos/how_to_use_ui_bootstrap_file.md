# How to use the ui-bootstrap file

The `ui-bootstrap.json` file contains config about each service type. Click [here](/config/config-editor-ui/ui-bootstrap.json) to see the default file provided. Service types are defined [here](/docs/services/how-tos/how_to_set_up_service_in_config_editor_rest.md).

## Schema paths
This config file provides paths/keys of various variables in the schema of the service type provided by the backend. The possible keys are: 
- `release.version` - release version key
- `release.config_array` - release config_array key
- `release.extras` - list of extra release keys
- `perConfigSchemaPath` - path to configs
- `name` - config name key
- `version` - config version key
- `author` - config author key
- `description` - config description key

## Labels function
It also provides a key to define a javascript function that will return the labels for the service. This is done through the `labelsFunc` key. 

## Testing
You can also enable and disable testing through these keys:
- `testing.perConfigTestEnabled` - whether a single config can be tested
- `testing.releaseTestEnabled` - whether the entire release can be tested
- `testing.testCaseEnabled` - whether test cases can be created

## Checkboxes
Checkboxes for quick filtering can be added per service in the `checkboxes` key. Filters can be added based on different fields such as the author or the labels of the config. Below is example of the config of a checkbox. It defined one checkbox group called `Severity` with two checkboxes: `high` and `low`. The pattern defined for each checkbox is matched against the given field. The given field can be:
- a `string` - the pattern is matched against it
- a `list` -  the pattern has to match one of the items
```
"checkboxes": {
    "severity": {
        "high": {
            "field": "labels",
            "pattern": "^severity:low$"
        },
        "low": {
            "field": "labels",
            "pattern": "^severity:high$"
        }
    }
}
```

## Override default properties for service type
The `override` key can be used to override any of the above properties for a specific service `name` (all the above is per service `type`). So if you have two services of the same type you can apply different config to one using this key. 

**_Example:_** Overriding the `testing.releaseTestEnabled` property for the `myalerts` service:
```
"override": {
  "myalerts": {
    "testing": {
        "releaseTestEnabled": false
    }
  }
}
```




