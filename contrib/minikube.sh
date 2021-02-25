#!/bin/sh

profile=siembol

echo == install prerequisites ==
brew install helm kubernetes-cli minikube mkcert nss

echo == create and install CA ==
mkcert -install

echo == create k8s cluster ==
minikube start --profile $profile --driver hyperkit --container-runtime containerd --cni calico --cpus 8 --memory 16g --disk-size 40g --addons ingress,ingress-dns
minikube profile $profile

echo == add DNS resolver ==
sudo tee /etc/resolver/minikube-$(minikube profile) >/dev/null << EOF
domain $(minikube profile).local
nameserver $(minikube ip)
search_order 1
timeout 5
EOF

echo == install cert-manager ==
helm repo add jetstack https://charts.jetstack.io
kubectl create namespace cert-manager
helm install cert-manager jetstack/cert-manager --namespace cert-manager --version v1.1.0 --set installCRDs=true


printf '== wait for cert-manager to be up'
for deployment in cert-manager cert-manager-cainjector cert-manager-webhook
do
  while ! [ "$(kubectl get deployment $deployment --namespace cert-manager -o jsonpath='{.status.readyReplicas}')" == "1" ]; do printf .; sleep 1; done
done
echo ' =='


echo == install CA in sandbox namespace ==
kubectl create namespace sandbox
kubectl create secret tls ca-key-pair --namespace sandbox --cert=$HOME/Library/ApplicationSupport/mkcert/rootCA.pem --key=$HOME/Library/ApplicationSupport/mkcert/rootCA-key.pem
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: ca-issuer
  namespace: sandbox
spec:
  ca:
    secretName: ca-key-pair
EOF


echo == create demo deployment and service ==
kubectl create deployment demo --namespace sandbox --image=gcr.io/google-samples/hello-app:1.0
kubectl expose deployment demo --namespace sandbox --port=8080
printf '== wait for demo deployment to be up'
while ! [ "$(kubectl get deployment demo --namespace sandbox -o jsonpath='{.status.readyReplicas}')" == "1" ]; do printf .; sleep 1; done
echo ' =='


echo == create demo ingress ==
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    cert-manager.io/issuer: ca-issuer
  name: ingress
  namespace: sandbox
spec:
  rules:
  - host: demo.$(minikube profile).local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: demo
            port:
              number: 8080
  tls:
  - hosts:
    - demo.$(minikube profile).local
    secretName: ingress-cert
EOF


printf '== wait for certificate to be created'
while ! kubectl get secret ingress-cert --namespace sandbox >/dev/null 2>/dev/null; do printf .; sleep 1; done
echo ' =='


printf '== wait for ingress controller to be reloaded'
while [[ "$(kubectl get event --namespace kube-system --field-selector reason==RELOAD -o jsonpath='{.items[-1].lastTimestamp}' 2>/dev/null)" < "$(date -u -v-2S +%Y-%m-%dT%H:%M:%SZ)" ]]; do printf .; sleep 1; done
echo ' =='


echo == validate that the demo works ==
curl https://demo.$(minikube profile).local/
echo == done ==
echo "=> You can try it in a browser too: https://demo.$(minikube profile).local/"