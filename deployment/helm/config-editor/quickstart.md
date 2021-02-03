# Quickstart Guide

## Local Install

### Kind

First, install kind:
* https://kind.sigs.k8s.io/docs/user/quick-start/#installation

Next, create the cluster:
```bash
kind create cluster --config kind_cluster.yaml --name siembol
```

Generate a secret for your git API Token:

```bash
kubectl create secret generic config-editor-rest-secrets --from-file=git
```

Install nginx-ingress in the cluster:

```bash
kubectl delete -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/kind/deploy.yaml
```

Install Siembol in the cluster:
```bash
helm repo add gresearch	https://g-research.github.io/charts
helm install siembol
```

Siembol should now be available at: http://localhost

### Cleaning up

```bash
kind delete cluster --name siembol-test
mv $hult

```

## Cloud Install

### GCE

### Amazon