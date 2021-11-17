$namespace="siembol"

Write-Output "************************************************************"
Write-Output "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add gresearch https://g-research.github.io/charts
helm repo update


helm install storm gresearch/storm --namespace $namespace `
    --set supervisor.replicaCount=1 `
    --set supervisor.image.tag=2.3.0 `
    --set supervisor.resources.requests.memory=1024 `
    --set supervisor.resources.limits.memory=1024 `
    --set supervisor.resources.requests.cpu=500m `
    --set supervisor.childopts="-Xmx1g" `
    --set nimbus.image.tag=2.3.0 `
    --set supervisor.slots=3 `
    --set ui.image.tag=2.3.0 `
    --set zookeeper.fullnameOverride="siembol-zookeeper"


helm install kafka bitnami/kafka --namespace $namespace `
    --set zookeeper.enabled=false `
    --set externalZookeeper.servers={siembol-zookeeper-0.siembol-zookeeper-headless.siembol.svc}

$choice = Read-Host -Prompt "Do you want to deploy oauth2-proxy? (y/n)" 
Write-Output $choice
if ($choice -eq 'y') {
    Write-Output "installing oauth2-proxy"
    helm install oauth2-proxy bitnami/oauth2-proxy --namespace $namespace -f deployment/helm-k8s/oauth-values.yaml
}

Write-Output "************************************************************"
Write-Output "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods --namespace $namespace
Write-Output "************************************************************"
