#!/bin/bash

helm delete storm -n=siembol

helm delete kafka -n=siembol

helm delete siembol-zookeeper -n=siembol

helm delete siembol -n=siembol

kubectl delete --all jobs -n siembol

kubectl delete --all pvc -n siembol

