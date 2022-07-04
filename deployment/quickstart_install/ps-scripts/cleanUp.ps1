helm delete storm -n=siembol

helm delete kafka -n=siembol

helm delete siembol -n=siembol

helm delete kafdrop -n=siembol

helm delete monitoring -n=siembol

kubectl delete pod kafka-client -n siembol

kubectl delete --all jobs -n siembol

kubectl delete configmap github-details -n siembol

kubectl delete secret config-editor-rest-secrets -n siembol

kubectl delete --all pvc -n siembol
