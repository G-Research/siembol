# How to use the ui-bootstrap file

The `ui-bootstrap.json` config file contains metadata about each service type. 

## Schema paths
This config file provides paths/keys of various variables in the schema of the service type provided by the backend. The possible keys are: 
- release.version: release version key
- release.config_array: release config_array key
- release.extras: list of extra release keys
- perConfigSchemaPath: path to configs
- name: config name key
- version: config version key
- author: config author key
- description: config description key

## Labels function
It also provides the a key to define a javascript function that will return the labels for the service. This is done through the `labelsFunc` key. 

## Testing
You can also enable and disable testing through these keys:
- testing.perConfigTestEnabled: whether a single config can be tested
- testing.releaseTestEnabled: whether the entire release can be tested
- testing.testCaseEnabled: whether test cases can be created

## Checkboxes

## Overwrite


