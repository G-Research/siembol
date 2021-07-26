#!/bin/bash

echo "************************************************************"
echo "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add gresearch https://g-research.github.io/charts
helm repo update

helm install siembol-zookeeper bitnami/zookeeper -n=siembol

helm install kafka bitnami/kafka -n=siembol

helm install storm gresearch/storm -n=siembol --set supervisor.replicaCount=1


echo "************************************************************"
echo "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods -n siembol
echo "************************************************************"