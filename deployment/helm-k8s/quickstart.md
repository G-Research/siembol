Quickstart Guide
================

Local Install
----------------

### 1. Run minikube.sh

```bash
deployment/helm-k8s/quickstart_install/minikube.sh
```

### 2. Install dependencies
#### 1. Run dependencies.sh
```bash
deployment/helm-k8s/quickstart_install/dependencies.sh
```

### 3. Prepare git

#### 1. Prepare Siembol Config Repository

1. Go to https://github.com/G-Research/siembol-config
2. Fork into your own organization or personal account

#### 2. Create Git token

1. Go to https://github.com/settings/tokens
2. Click Generate new token
4. Select "repo - Full control of private repositories" scope
5. Hit "Generate token"
6. Keep this token value as you will need it for the next step.

#### 3. Run demoInstall.sh
1. This will ask for your github details related to the Siembol Config repository and the token created in previous step.
2. This will also initialise Zookeeper nodes.

```bash
deployment/helm-k8s/quickstart_install/demoInstall.sh
```

### 4. Siembol install

To install Siembol in the cluster

```bash
helm install siembol deployment/helm-k8s/ -f deployment/helm-k8s/values.yaml -n=siembol
```

This step might take a few minutes depending on the specs of your development machine.

### Check it out!

In a browser, go to:

  * https://siembol.local/home

You should now see the Siembol UI homepage.

## Cleaning up
If you're done poking about on a local instance, you can clean up with:

1. For cleaning up siembol resources and dependencies:
```bash deployment/helm-k8s/quickstart_install/cleanUp.sh
```
2. For deleting everything siembol related incl. config maps, secrets, certs and namespace:
```bash
minikube delete -p siembol
sudo rm /etc/resolver/minikube-*
```