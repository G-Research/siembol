# How to deploy configurations in siembol ui
<img src="../screenshots/deployment_upgrade.png" alt="drawing" width='400px'/>

## Editing Deployment
To upgrade config click on the `Upgrade to x` button below the config, it appears whenever a config in release is not up to date with the store. You can also click on `View Diff` to see the changes made.

To remove a config from deployment simply click on the bin icon that appears when hovering over it. 

To change the order of the configs in deployment simply drag the deployment config to its desired location. 

```
Note: you cannot downgrade a config only upgrade.
```

## Create Pull Request with new deployment in siembol ui
<img src="../screenshots/submit_dialog_alert.png" alt="drawing" width='600px'/>

Once configs have been added/deleted/upgraded/reorded use the `Deploy` button above. This will open up a dialog, for some services it will automatically run a validation in the background, for others (e.g: alert, correlationalert) additional metadata can be specified in the dialog before clicking the `Validate` button (see in screenshot above). Once validated the changes to the release are displayed in the dialog.  

For some services it will be possible to run tests on the all the configs in the service by clicking the `Test` button in the dialog. 

To create a pull request in the release repo of the service being edited click on the `Deploy` button in the dialog.

If the user trying to deploy does not have the latest version of the release, an error dialog will be shown and the latest release will be loaded from the backend. The user will have to redo its deployment changes to try again.

Once a pull request has been created in the repo it is not possible to create another one until it is either merged or closed. You can see when a pull request is opened in the UI, the `Deploy` button is replaced with `PR for release pending` and a `reload` icon. By clicking on the first a new tab is open to the pull request. The second should be clicked once the pull request is merged/close to reload the config from the backend and be able to deploy again.

<img src="../screenshots/pr_open.png" alt="drawing" width='500px'/>