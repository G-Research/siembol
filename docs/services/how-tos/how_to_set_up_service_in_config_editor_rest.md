# How to set-up a service in config editor rest
The service is added into siembol UI by editing application properties of config editor rest.
Before you start editing the properties you need to prepare git repositories for storing service configurations. 
## Service name and type
Service name should identify the service and should be unique in the application properties. For the sake of simplicity let us use the name `my-new-service`. The type of the service can be selected in the property:
- `config-editor.services.my-new-service.type` from `alert`, `correlationalert`, `response`, `parserconfig`, `parsingapp`, `enrichment`

### Git repositories settings

- `config-editor.services.my-new-service.config-store.git-user-name` - The git user name of the user with write access to all related repositories

- `config-editor.services.my-new-service.config-store.git-password` - The password or the token of the git user usually stored in an environment variable like `${GIT_TOKEN}`

- `config-editor.services.my-new-service.config-store.github-url` - The url of github server

- `config-editor.services.my-new-service.config-store.store-repository-name` - The repository name for storing configs. This repository should have disabled main branch protection in order to store configurations directly in the main branch

- `config-editor.services.my-new-service.config-store.store-repository-path` - The path to a directory where the config store repository will be cloned

- `config-editor.services.my-new-service.config-store.release-repository-name` - The repository name for releasing (deploying) configurations. This repository could have enabled main branch protection since all changes to the main branch are realised using pull requests

- `config-editor.services.my-new-service.config-store.release-repository-path` - The path to a directory where the release repository will be cloned

- `config-editor.services.my-new-service.config-store.admin-config-repository-name` - The repository name for releasing (deploying) admin configurations. This repository could have enabled main branch protection since all changes to the main branch are realised using pull requests. This property is applicable only to services from `alert`, `correlationalert`, `parsingapp`, `enrichment`

- `config-editor.services.my-new-service.config-store.admin-config-repository-path` - The path to a directory where the admin config repository will be cloned

```
Note: you can share one git repository in the settings above. In this case you do not need to define a path for the same repository multiple times and defining it once should be sufficient
```

- `config-editor.services.my-new-service.config-store.store-directory` - The directory in the store git repository where configs will be stored

- `config-editor.services.my-new-service.config-store.test-case-directory` - - The directory in the store git repository where test cases will be stored. This property is applicable only to services from `alert`, `parserconfig`, `enrichment`

- `config-editor.services.my-new-service.config-store.release-directory` - The directory in the release git repository where the release will be stored

- `config-editor.services.my-new-service.config-store.admin-config-directory` - The directory in the admin config git repository where the admin config will be stored.  This property is applicable only to services from `alert`, `correlationalert`, `parsingapp`, `enrichment`

```
Note: you do not need to create these directories manually during initialisation. All the directories will be created in git by config editor rest after used by siembol UI
```
### Ui Layout file name
The ui-layout files are used to modify the config schema which is sent to the siembol UI to render web forms. The layout config files are useful to add/overwrite parts of the schema in order to customise siembol UI in order to change a title, a description of the field, or for adding a help-link.
- `config-editor.services.my-new-service.ui-config-file-name` - The path to the ui layout configuration file

### Synchronisation settings
- `config-editor.services.my-new-service.synchronisation` - This field enable or disable synchronisation of the service with zookeeper and storm topology manager that is responsible for releasing storm topologies. The synchronisation is triggered by calling the webhook api, see [how_to_setup_webhook](../../deployment/how-tos/how_to_setup_github_webhook.md) 
    - `RELEASE` - synchronise service release (deployment) with the zookeeper node defined in the property `config-editor.services.my-new-service.release-zookeeper`. This type of synchronisation is applicable only to services from `alert`, `correlationalert`, `parserconfig`, `enrichment`
    - `ADMIN_CONFIG`- synchronise admin configuration with desired state for storm topology manager. This type of synchronisation is applicable only to services from `alert`, `correlationalert`, `enrichment`
    - `ALL` - it synchronizes both `RELEASE` and `ADMIN_CONFIG` if possible. This needs to be enabled to synchronise `parsingapp` service since it depends on both admin configuration and release.
```
Note: you need to enable synchronisation for all services in the property: config-editor.synchronisation. Similarly you can disable synchronisation for all services using this property.
```    
#### Release zookeeper settings for service deployment
- `config-editor.services.my-new-service.release-zookeeper.zk-path` - path to Zookeeper node for synchronisation of the deployment and related application. 
- `config-editor.services.my-new-service.release-zookeeper.zk-url` -  Zookeeper servers url. Multiple servers are separated by comma

#### Topology image for topology deployment
- `config-editor.services.alert.topology-image` - Url for downloading the topology image used during releasing the related service topology by storm topology manager
### Authorisation
Authorisation are applicable only if `oauth2` authentication is enabled in siembol ui. The access to the service is based on checking OIDC group membership.
#### Authorisation for service users
- `config-editor-auth.authorisation-groups.my-new-service` - The list of groups for accessing the service
#### Authorisation for service administrators
- `config-editor-auth.authorisation-admin-groups.my-new-service` - - The list of groups for accessing the admin configuration of the service. This property is applicable only to services from `alert`, `correlationalert`, `enrichment`, `parsingapp`
