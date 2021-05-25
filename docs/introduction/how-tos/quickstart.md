# Look! Docs!
# Quickstart Guide

## Local Install
### Minikube

#### 1. Run minikube.sh

```bash
deployment/minikube/minikube.sh
```

#### 2. Prepare Siembol Config Repository

##### 1. Fork Siembol Config Repository

1. Go to https://github.com/stackedsax/siembol-config
   * Ask for access if you don't have it already
1. Fork into your own organization or personal account

#### 2. Update application.properties

Edit `deployment/helm/config-editor/resources/application.properties`

Change <your_github_username> in these lines: 

```
config-editor.services.alert.config-store.git-user-name=<your_github_username>
config-editor.services.alert.config-store.store-repository-name=<your_github_username>/siembol-config
config-editor.services.alert.config-store.release-repository-name=<your_github_username>/siembol-config
```

#### 3. Prepare git

##### 1. Create Git Secret

Generate a token
1. Go to https://github.com/settings/tokens
2. Click Generate Token
4. Select "repo - Full control of private repositories" scope
5. Hit "Generate token"
6. Copy token value to a file named `git_token`

##### 2. Generate a secret for your git API Token:

This creates a Kubernetes secret for the Config Editor to interact with git.

```bash
kubectl create secret generic siembol-config-editor-rest-secrets --from-file=git_token -n siembol
```



#### 4. Helm install

Install Siembol in the cluster:

```bash
helm repo add gresearch https://g-research.github.io/charts
helm upgrade --install siembol -n siembol -f deployment/helm/config-editor/values.yaml deployment/helm/config-editor
```

### Cleaning up
If you're done poking about on a local instance, you can clean up with:

```bash
minikube delete -p siembol
sudo rm /etc/resolver/minikube-*
```

## Cloud Install

### GCE

### Amazon