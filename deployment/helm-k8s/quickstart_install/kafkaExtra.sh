#!/bin/bash

echo "************************************************************"
echo "****************** Installing dependencies *****************"
git clone https://github.com/obsidiandynamics/kafdrop && cd kafdrop

helm upgrade -i kafdrop chart --set image.tag=3.27.0 \
    --set kafka.brokerConnect=kafka.siembol.svc.cluster.local:9092 \
    --set server.servlet.contextPath="/" \
    --set jvm.opts="-Xms32M -Xmx64M" --namespace=siembol

kubectl run kafka-client --restart='Never' --image docker.io/bitnami/kafka:2.8.0-debian-10-r43 --namespace siembol --command -- sleep infinity

NODE_PORT=$(kubectl get --namespace siembol -o jsonpath="{.spec.ports[0].nodePort}" services kafdrop)
NODE_IP=$(kubectl get nodes --namespace siembol -o jsonpath="{.items[0].status.addresses[0].address}"

echo "Kafka UI url: http://$NODE_IP:$NODE_PORT"

echo "************************************************************"
