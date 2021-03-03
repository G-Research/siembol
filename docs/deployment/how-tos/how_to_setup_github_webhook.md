# How to set-up a github webhook
The synchronisation of service configurations stored in git repositories with zookeeper nodes is implemented in siembol in config editor rest service.
## Siembol config editor rest rest endpoint for webhooks
Find a hostname of siembol config editor rest and prepare url. 
### Url parameters
- serviceNames - comma separated list of service names or ```all``` if the hook is for all services
- syncType - type of synchronisation that should be triggered by the hook
    - one from ```ALL```, ```RELEASE```, ```ADMIN_CONFIG```

```
Example of url:
https://config-editor/api/v1/sync/webhook?serviceNames=alert&syncType=ALL
```
## Setting a webhook in a github repository
For a git repository you should recognise 
- services which configurations are stored in a git repository
- type of configurations that is stored in the git repository
### Prepare and test url
You can prepare url using above example or by swagger
```
Example of swagger url:
https://config-editor-rest/swagger-ui.html
```
Ensure that the prepared url is accessible form the github server.
### Set a webhook url in github
To set up a webhook, go to the settings page of your repository or organization in github. From there, click Webhooks, then Add webhook url with push event.
### Set the content type
Set the content type as ```application/json```
### Set the github secret (optional)
Setting a webhook secret allows you to ensure that  POST requests sent to siembol are from GitHub. 
#### Set the secret for verification in config editor rest application properties 
The verification of webhook signature is computed only if  is set the secret in the application properties of config editor rest. Otherwise this check is skipped.
```
config-editor.gitWebhookSecret=your secret provided in github
```

