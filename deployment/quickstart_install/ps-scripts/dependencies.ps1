$namespace="siembol"

Write-Output "************************************************************"
Write-Output "****************** Installing dependencies *****************"
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

helm dependency update deployment/helm-k8s/storm/
helm install storm deployment/helm-k8s/storm/ --namespace $namespace

helm install kafka bitnami/kafka --namespace $namespace `
    --set zookeeper.enabled=false `
    --set externalZookeeper.servers={siembol-zookeeper-0.siembol-zookeeper-headless.siembol.svc}


Write-Output "************************************************************"
Write-Output "Checking status by running: 'kubectl get pods -n siembol'"
kubectl get pods --namespace $namespace
Write-Output "************************************************************"
