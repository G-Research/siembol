$namespace="siembol"

Write-Output "************************************************************"
Write-Output "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add gresearch https://g-research.github.io/charts
helm repo update


helm install siembol-zookeeper bitnami/zookeeper --namespace $namespace

helm install kafka bitnami/kafka --namespace $namespace

helm install storm gresearch/storm --namespace $namespace `
    --set supervisor.replicaCount=1 `
    --set supervisor.image.tag=2.3.0 `
    --set supervisor.resources.requests.memory=1024 `
    --set supervisor.resources.limits.memory=2048 `
    --set supervisor.resources.requests.cpu=500m `
    --set supervisor.childopts="-Xmx2g" `
    --set nimbus.image.tag=2.3.0 `
    --set supervisor.slots=5 `
    --set ui.image.tag=2.3.0


Write-Output "************************************************************"
Write-Output "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods --namespace $namespace
Write-Output "************************************************************"
