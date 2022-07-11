$namespace="siembol"

Write-Output "************************************************************"
Write-Output "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add gresearch https://g-research.github.io/charts
helm repo update


helm install storm gresearch/storm --namespace $namespace `
    --set supervisor.replicaCount=1 `
    --set supervisor.resources.requests.memory=1024 `
    --set supervisor.resources.limits.memory=1024 `
    --set supervisor.resources.requests.cpu=500m `
    --set supervisor.childopts="-Xmx1g" `
    --set supervisor.slots=3 `
    --set zookeeper.fullnameOverride="siembol-zookeeper" `
    --set provisioning.enabled=true `
    --set "provisioning.topics[0].name=siembol.alerts" `
    --set "provisioning.topics[1].name=siembol.response.heartbeat"


helm install kafka bitnami/kafka --namespace $namespace `
    --set zookeeper.enabled=false `
    --set externalZookeeper.servers={siembol-zookeeper-0.siembol-zookeeper-headless.siembol.svc}


Write-Output "************************************************************"
Write-Output "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods --namespace $namespace
Write-Output "************************************************************"
