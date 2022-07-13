#!/bin/bash

echo "************************************************************"
echo "****************** Installing dependencies *****************"
JMX_DIR=jmx   
JMX_AGENT_NAME="agent.jar"
NAMESPACE="siembol"

file_url="https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.17.0/jmx_prometheus_javaagent-0.17.0.jar"
mkdir $JMX_DIR
wget -O "$JMX_DIR/$JMX_AGENT_NAME" $file_url
kubectl -n $NAMESPACE create cm storm-metrics-reporter --from-file=metrics_reporter_agent.jar=$JMX_DIR/$JMX_AGENT_NAME    

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

helm dependency update deployment/helm-k8s/storm/
helm install storm deployment/helm-k8s/storm/ -n=$NAMESPACE

helm install kafka bitnami/kafka -n=$NAMESPACE \
    --set zookeeper.enabled=false \
    --set externalZookeeper.servers={siembol-zookeeper-0.siembol-zookeeper-headless.siembol.svc} \
    --set provisioning.enabled=true \
    --set "provisioning.topics[0].name=siembol.alerts" \
    --set "provisioning.topics[1].name=siembol.response.heartbeat"

echo "************************************************************"
echo "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods -n siembol
echo "************************************************************"
