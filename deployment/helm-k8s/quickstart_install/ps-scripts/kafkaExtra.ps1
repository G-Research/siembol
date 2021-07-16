Write-Output "************************************************************"
Write-Output "****************** Installing dependencies *****************"
git clone https://github.com/obsidiandynamics/kafdrop && cd kafdrop

helm upgrade -i kafdrop chart --set image.tag=3.27.0 `
    --set kafka.brokerConnect=kafka-0.kafka-headless.siembol.svc.cluster.local:9092 `
    --set server.servlet.contextPath="/" `
    --set jvm.opts="-Xms32M -Xmx64M" --namespace=siembol

kubectl run kafka-client --restart='Never' --image docker.io/bitnami/kafka:2.8.0-debian-10-r43 --namespace siembol --command -- sleep infinity

Write-Output "************************************************************"
