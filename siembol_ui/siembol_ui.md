# Siembol UI
## Authentication
Two types of authentication are supported: disabled or oauth2. This can be configured in the `ui-config.json` file (more info [here](../services/how-tos/how_to_set_up_service_in_config_editor_rest.md)).
## Home page
![image](screenshots/home_page.png)
### Services
On the home page all services are listed alphabetically by name on the left side bar. By hovering on a service you can directly access your editing of choice depending on user roles (admin or/and config) or access the various git directories used.

<p align="center">
    <img src="screenshots/sidebar_hover.png" alt="drawing" width="400"/>
</p>

### Recently visited
Your recently visited pages are saved in your browser and can be accessed with only one click from the home page. The default number of pages shown is 5 but can be configured in the `ui-config.json` file using the "historyMaxSize" key.
 
### Explore Siembol
The 'Explore Siembol' section of the home page is for quick access to useful resources such as documentation, ticket tracking systems etc... By default there is a link to the documentation and to the issues page on the git repo. This can be customised from the `ui-config.json` config file. 

Below is the default config file provided. The two default links are in "homeLinks". To add a new one a url, an icon and a title are required like in the config below. 


    {
        "environment": "demo",
        "serviceRoot": "https://config-editor/",
        "uiBootstrapPath": "./ui-bootstrap.json",
        "authType": "disabled",
        "historyMaxSize": 5,
        "blockingTimeout": 30000,
        "homeLinks": [
            {
                "icon": "library_books",
                "title": "Documentation",
                "link": "https://github.com/G-Research/siembol/tree/master/docs"
            },
            {   
                "icon": "live_help",
                "title": "Issues",
                "link":  "https://github.com/G-Research/siembol/issues"}
            ]
    }

## Blocking timeout
The blocking timeout is a property that can be configured in `ui-config.json` (as shown in the default config above), it can be omitted in `ui-config.json`, the default value is 30 seconds. This value is used for certain operations that require blocking the entire UI (eg. deleting a config), the UI will be blocked for this maximum amount of time after which an error will occur if the operation hasn't yet finished. 

## Service configurations 
After selecting a service to edit you will be redirected to the service configuration page. An example is shown below. 

<img src="screenshots/config_manager.png" alt="drawing"/>

### Config Store
The config store is shown on the left hand side of the config manager page. These are the configs in the store repo. The configurations are ordered according to the order in deployment and with the deployed configs before the non-deployed ones. 
<img src="screenshots/config_store.png" alt="drawing"/>

### Deployment
<p align="center">
    <img src="screenshots/deployment.png" alt="drawing" width="500px"/>
</p>
The deployments are shown on the right hand side of the config manager page. These are the configs that are in the release repo. To add a new config from the store to deployment, simply drag it into the deployment column. Click the `Upgrade to version x` button below a deployed config to upgrade it to the latest changes from the store. To delete a config from deployment click on the `bin` icon that appears when hovering over it. 

### Filtering
There is a search bar and various checkboxes at the top of the config manager used to filter the configs shown. The search bar allows you to search for configs by name, author or labels. The checkbox "My Edits" filters all configs where the current user has made the latest changes, "Undeployed" filters configs not in the deployment column, "Upgradable" filters configs that are deployed but don't have the latest version from the store.

### History
By hovering over the version number of a store config its history becomes visible. It includes dates, authors and the count of lines changed. 
Similarly the deployment history is visible by hovering over the history logo. 

## Creating a service config
See [here](how-tos/how_to_test_config_in_siembol_ui.md).

## Editing a service config
To edit a config the edit button that appears when hovering over a config can be used. 

Once in edit mode the window is separated in two:
 - on the left is the json tree view of the config, useful for a quick view of the entire config
 - on the right the config editor separated into tabs

The config editor has three main tabs for all services (although some may be disabled):
 - Edit Config
 - Test Config
 - Test Cases

 <img src="screenshots/config_editor.png" alt="drawing"/>

### Config Editor
The config editor tab is separated into different tabs that are different depending on the service. The screenshot above is for a correlation alert type service.

The submit button is disabled until the form is valid. 

### Testing Configuration
This tab can be used to test a test specification against the config in the previous tab. All that is required to run a test is a valid test specification. After clicking the `Run Test` button the output of the test is shown. More details [here](how-tos/how_to_test_config_in_siembol_ui.md).

### Test Cases
Test Cases can be set up to ensure the configuration is working as expected. It consists of test specifications and expected outputs that are stored in git. More details [here](how-tos/how_to_test_config_in_siembol_ui.md).

## Admin configuration
Admin view is similar to config editing view, the window is separated in two:
 - on the left is the json tree view of the admin config, useful for a quick view of the entire config
 - on the right the admin config editor separated into tabs

 The main difference between user config and admin config is that the latter doesn't have a store repo, only a release repo. This means that when submitting an admin config a PR is raised to the release repo directly. 
