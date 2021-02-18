# Look! Docs!
# Quickstart Guide

## Local Install

### Kind

First, install kind:
* https://kind.sigs.k8s.io/docs/user/quick-start/#installation

Next, create the cluster:
```bash
kind create cluster --config kind_cluster.yaml --name siembol
```

Generate a token
1. Go to https://github.com/settings/tokens
2. Click Generate Token
3. Add a note
4. Select "repo -   Full control of private repositories" scope
5. Hit "Generate token"

Generate a secret for your git API Token:

```bash
kubectl create secret generic config-editor-rest-secrets --from-file=git
```

Install nginx-ingress in the cluster:

```bash
kubectl create -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/kind/deploy.yaml
```

Install Siembol in the cluster:
```bash
helm repo add gresearch	https://g-research.github.io/charts
helm upgrade --install siembol -f values.yaml
```

Siembol should now be available at: 
* http://localhost/

Take a look around, add some rules, and give us some feedback!

### Cleaning up

```bash
kind delete cluster --name siembol-test
```

## Cloud Install

### GCE

### Amazon