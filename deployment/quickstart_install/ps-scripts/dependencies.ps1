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
    --set supervisor.childopts="-Xmx1g" `
    --set nimbus.image.tag=2.3.0 `
    --set supervisor.slots=6 `
    --set ui.image.tag=2.3.0 `
    --set zookeeper.enabled=false `
    --set zookeeper.servers=["zookeeper-0.zookeeper-headless.siembol.svc"]


Write-Output "************************************************************"
Write-Output "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods --namespace $namespace
Write-Output "************************************************************"
