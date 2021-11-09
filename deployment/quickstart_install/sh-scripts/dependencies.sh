#!/bin/bash

echo "************************************************************"
echo "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add gresearch https://g-research.github.io/charts
helm repo update

helm install kafka bitnami/kafka -n=siembol
helm install storm gresearch/storm -n=siembol \
    --set supervisor.replicaCount=1  \
    --set supervisor.image.tag=2.3.0 \
    --set supervisor.childopts="-Xmx1g" \
    --set supervisor.slots=3 \
    --set nimbus.image.tag=2.3.0 \
    --set ui.image.tag=2.3.0 \
    --set zookeeper.fullnameOverride="siembol-zookeeper"


echo "************************************************************"
echo "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods -n siembol
echo "************************************************************"
