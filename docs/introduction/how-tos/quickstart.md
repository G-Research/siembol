Quickstart Guide
================

Local Install
----------------

### 1. Run minikube.sh

```bash
deployment/minikube/minikube.sh
```

### 2. Prepare Siembol Config Repository

#### 1. Fork Siembol Config Repository

1. Go to https://github.com/G-Research/siembol-config
1. Fork into your own organization or personal account

#### 2. Update application.properties

1. Clone the https://github.com/G-Research/charts repo
1. Switch to the `siembol` branch: `git checkout stackedsax/siembol`
2. Edit: `src/siembol/resources/application.properties`

Change <your_github_org_or_username> in these lines: 

```
config-editor.services.alert.config-store.git-user-name=<your_github_org_or_username>
config-editor.services.alert.config-store.store-repository-name=<your_github_org_or_username>/siembol-config
config-editor.services.alert.config-store.release-repository-name=<your_github_org_or_username>/siembol-config
```

#### 3. Update git.config

1. Still in the `siembol` repo, edit: `src/siembol/resources/git.config`
2. Replace "your_username" and "your_email" with the appropriate values


### 3. Prepare git

#### 1. Create Git Secret

Generate a token
1. Go to https://github.com/settings/tokens
2. Click Generate Token
4. Select "repo - Full control of private repositories" scope
5. Hit "Generate token"
6. Copy token value to a file named `git`

#### 2. Generate a secret for your git API Token:

This creates a Kubernetes secret for the Config Editor to interact with git.

```bash
kubectl create secret generic siembol-config-editor-rest-secrets --from-file=git -n siembol
```


### 4. Helm install

To install Siembol in the cluster

```bash
helm repo add gresearch https://g-research.github.io/charts
helm upgrade --install siembol -n siembol charts/src/siembol -f charts/src/siembol/quickstart-values.yaml
```

This step might take 3-5 minutes depending on the specs of your development machine.

### Check it out!

In a browser, go to:

  * https://ui.siembol.local/home

You should now see the Siembol UI homepage.

## Cleaning up
If you're done poking about on a local instance, you can clean up with:

```bash
minikube delete -p siembol
sudo rm /etc/resolver/minikube-*
```