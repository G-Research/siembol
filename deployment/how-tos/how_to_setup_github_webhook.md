How to set-up a GitHub webhook
================================

The synchronisation of service configurations are stored in git repositories with Zookeeper nodes is implemented in siembol in config editor rest service.

Siembol config editor rest rest endpoint for webhooks
--------------------------------------------------------

Find a hostname of siembol config editor rest and prepare URL. 

### URL parameters

- `serviceNames`: Comma-separated list of service names or `all` if the hook is for all services
- `syncType`: Type of synchronisation that should be triggered by the hook.  One of:
    - `ALL`
    - `RELEASE`
    - `ADMIN_CONFIG`

Example URL:

- [`https://config-editor/api/v1/sync/webhook?serviceNames=alert&syncType=ALL`](https://config-editor/api/v1/sync/webhook?serviceNames=alert&syncType=ALL)


## Setting a webhook in a GitHub repository

For a git repository you should recognise: 
- services which configurations are stored in a git repository
- type of configurations that are stored in the git repository

### Prepare and test URL

You can prepare a URL using the above example or by using Swagger.  

Example Swagger URL:

- [`https://config-editor-rest/swagger-ui.html`](https://config-editor-rest/swagger-ui.html)


Ensure that the prepared URL is accessible form the GitHub server.

### Set a webhook URL in GitHub

To set up a webhook:
- Go to the settings page of your repository or organization in GitHub
- Click _Webhooks_
- Click _Add Webhook_ 
- Add a _Payload URL_
- Select _'Just the push event.'_
- Click _Add webhook_

### Set the content type

Set the content type as `application/json`

### Set the GitHub secret (optional)

Setting a webhook secret allows you to ensure that POST requests sent to siembol are from GitHub. 

#### Set the secret for verification in config editor rest's application.properties file

The verification of webhook signature is computed only if is set the secret in the application properties of config editor rest. Otherwise this check is skipped.

```properties
config-editor.gitWebhookSecret=your_secret_provided_in_GitHub
```