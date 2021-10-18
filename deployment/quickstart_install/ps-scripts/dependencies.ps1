$namespace="siembol"

Write-Output "************************************************************"
Write-Output "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add gresearch https://g-research.github.io/charts
helm repo update


helm install siembol-zookeeper bitnami/zookeeper --namespace $namespace `
    --set resources.requests.memory="64Mi"

helm install kafka bitnami/kafka --namespace $namespace `
    --set zookeeper.resources.requests.memory="64Mi" `
    --set resources.requests.memory="128Mi"

helm install storm gresearch/storm --namespace $namespace `
    --set supervisor.replicaCount=1 `
    --set supervisor.resources.requests.memory="256" `
    --set nimbus.image.tag=2.3.0 `
    --set supervisor.image.tag=2.3.0 `
    --set supervisor.replicaCount=1 `
    --set nimbus.image.tag=2.3.0 `
    --set supervisor.image.tag=2.3.0 `
    --set supervisor.slots=6 `
    --set supervisor.worker.heap_memory_mb=256 `
    --set ui.childopts="-Xmx256m -ea" `
    --set persistence.logs.size="10Mi" `
    --set persistence.metrics.size="50Mi" `
    --set zookeeper.resources.requests.memory="64Mi"


Write-Output "************************************************************"
Write-Output "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods --namespace $namespace
Write-Output "************************************************************"
