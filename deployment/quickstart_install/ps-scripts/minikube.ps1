$namespace="siembol"

Write-Output == install prerequisites ==
choco upgrade kubernetes-helm kubernetes-cli minikube mkcert carbon
Import-Module 'Carbon'

Write-Output == create and install CA ==
mkcert -install

Write-Output == create k8s cluster ==
minikube start --profile $namespace --driver hyperv --cpus 4 --memory 8g --disk-size 40g --addons ingress
minikube profile $namespace

Write-Output == install dns host entries ==
Set-CHostsEntry -IPAddress $(minikube ip) -HostName 'siembol.local' -Description 'resolver for siembol.local'
Set-CHostsEntry -IPAddress $(minikube ip) -HostName 'rest.siembol.local' -Description 'resolver for rest.siembol.local'
Set-CHostsEntry -IPAddress $(minikube ip) -HostName 'storm.local' -Description 'resolver for storm.local'
Set-CHostsEntry -IPAddress $(minikube ip) -HostName 'topology-manager.siembol.local' -Description 'resolver for topology-manager.siembol.local'
Set-CHostsEntry -IPAddress $(minikube ip) -HostName 'enrichment.local' -Description 'resolver for enrichment.local'

Write-Output == install cert-manager ==
helm repo add jetstack https://charts.jetstack.io
helm repo update
kubectl create namespace cert-manager
helm install cert-manager jetstack/cert-manager --namespace cert-manager `
  --set installCRDs=true

Write-Output '== wait for cert-manager to be up'
kubectl wait --namespace cert-manager --for=condition=available --timeout=90s --all deployments

Write-Output == install CA in siembol namespace ==
kubectl create namespace $namespace
kubectl create -n $namespace secret tls cacerts --cert=$($env:LOCALAPPDATA)\mkcert\rootCA.pem --key=$($env:LOCALAPPDATA)\mkcert\rootCA-key.pem

$certfile = New-TemporaryFile

Write-Output @"
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: ca-issuer
  namespace: $namespace
spec:
  ca:
    secretName: cacerts
"@ | Out-file $certfile.FullName

kubectl apply -f $certfile.FullName
Remove-Item $certfile.FullName -Force
