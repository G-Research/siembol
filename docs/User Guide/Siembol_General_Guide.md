# Siembol General User Guide
## Overview
This document gives advice on how to use some of the common UI elements that are used in multiple Siembol components. 

## Siembol Home Page
The Siembol homepage provides access to the editors for each Siembol component. 
 [//]: # (TODO add image of home page)

 Each component has it's own block with 3 buttons
 1. Editor: this button takes you to the config editor for that component. 
 2. Store repo: this takes you to the git repo for the config store 
 3. Release repo: this takes you to the git repo for the released config for that component

## Editor Page
### Filtering and Searching

There is a search bar at the top of the Editor page to allow you to filter through stored configs by name or tag.

[//]: # (TODO add image of search bar/search results)

There are also checkboxes for commonly used filters. These allow you to select any combination of:

* rules you've edited
* undeployed rules
* rules which have an undeployed upgrade

[//]: # (TODO add image of filtering checkboxes)

### Config block
Each config consists of a UI block containing:

1. The version number
2. The last author
3. Config name
4. Config description
5. Config tags

[//]: # (TODO add annotated image of a config block)

Hovering over the right side of the box allows you to see 3 further options:

1. Modify the config - this opens the config in the create config UI (discussed in the guide for each component) with the config details pre-populated
2. View the config's raw json
3. Move the config to the deployment section

[//]: # (TODO add annotaed image of config block ft hidden options)

### Change history 
The change history for an individual config can be seen by hovering over its version number.

[//]: # (TODO add image of individual config change history)

The change history for the deployment config can be seen by hovering over the time icon in the top-right corner of the deployment section.

[//]: # (TODO add image of deployment change history)


### Creating a new config
To the right-hand side of the filter checkboxes in the Editor UI there is a blue cross. Clicking this button allows you to create a new config and changes the view to the config editor mode.

[//]: # (TODO add image of new parser button)

### Deploying a config

[//]: # (TODO move this section to the general guide)

Once a config is in the store it can be be deployed from the Editor UI. 

### Deploying a config for the first time
If a config only exists in the store it can be added to the deployment section by clicking the deployment arrow on the right-hand side of the config block. 

Once a config is in the deployment section it can be committed to the deployment repo by clicking the deploy button at the top of the deployment section.

[//]: # (TODO add image of the deploy button)

Rules are stored in individual config files in the store. When the deploy button is pressed, all rules in the deployment section are combined together to create one deployment config. Therefore unless you want to un-deploy them, all configs need to remain in the deployment section.

### Upgrading a config which is already deployed
If you make changes to a config which is already deployed and commit them to the store, then an upgrade button will appear in the config block in the deployment section.

To deploy your changes:

1. click the upgrade button
2. click the deploy button at the top of the deployment section

[//]: # (TODO add image of the upgrade button)