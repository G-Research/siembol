#!/bin/bash

echo "************************************************************"
echo "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add gresearch https://g-research.github.io/charts
helm repo update

helm install storm gresearch/storm -n=siembol \
    --set supervisor.replicaCount=1  \
    --set supervisor.image.tag=2.3.0 \
    --set supervisor.childopts="-Xmx1g" \
    --set supervisor.slots=3 \
    --set nimbus.image.tag=2.3.0 \
    --set ui.image.tag=2.3.0 \
    --set zookeeper.fullnameOverride="siembol-zookeeper"

helm install kafka bitnami/kafka -n=siembol \
    --set zookeeper.enabled=false \
    --set externalZookeeper.servers={siembol-zookeeper-0.siembol-zookeeper-headless.siembol.svc}

read -p "Do you want to deploy oauth2-proxy? (y/n)" choice
echo $choice
if [ $choice = 'y' ]; then
    echo "installing oauth2-proxy"
    helm install oauth2-proxy bitnami/oauth2-proxy -n=siembol -f deployment/helm-k8s/oauth-values.yaml
fi

echo "************************************************************"
echo "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods -n siembol
echo "************************************************************"
