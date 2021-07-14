#!/bin/bash

CONFIG_MAP_NAME_GIT="github-details"
GIT_SECRET_NAME="siembol-config-editor-rest-secrets"
NAMESPACE="siembol"

git_details () {
    read -p 'Github username: ' GIT_USERNAME
    read -p 'Github URL: ' GIT_URL
    read -p 'Github repo name: ' GIT_REPO_NAME
    read -p 'Github personal token: ' GIT_TOKEN

    echo "You entered these details: "
    echo "Github username: $GIT_USERNAME"
    echo "Github URL: $GIT_URL"
    echo "Github repo name: $GIT_REPO_NAME"
    echo "Github personal token: $GIT_TOKEN"

    read -p "Are these details correct? (yes/no)" choice
    echo $choice
    if [ $choice = 'yes' ]; then
        echo "creating configmap"
        kubectl create configmap $CONFIG_MAP_NAME_GIT -n $NAMESPACE --from-literal=GITHUB_USER=$GIT_USERNAME --from-literal=GITHUB_URL=$GIT_URL --from-literal=GITHUB_REPO_NAME=$GIT_REPO_NAME 
        sleep 3
        echo "Creating github secret"
        kubectl create secret generic $GIT_SECRET_NAME -n $NAMESPACE --from-literal=git=$GIT_TOKEN
        sleep 3
    else
        echo "Run script again"
        exit 1

    fi
}

init_zookeeper_nodes () {
    declare -a ZookeeperNodes=("/siembol/synchronise" "/siembol/alerts" "/siembol/correlation_alerts" "/siembol/parser_configs" "/siembol/cache") 
    echo "Creating Zookeeper nodes "
    POD_NAME=$(kubectl get pods --namespace $NAMESPACE -l "app.kubernetes.io/name=zookeeper,app.kubernetes.io/instance=siembol-zookeeper,app.kubernetes.io/component=zookeeper" -o jsonpath="{.items[0].metadata.name}")
    kubectl exec -it $POD_NAME -n $NAMESPACE -- zkCli.sh create /siembol
    for node in "${ZookeeperNodes[@]}"; do
        kubectl exec -it $POD_NAME -n $NAMESPACE -- zkCli.sh create $node
        kubectl exec -it $POD_NAME -n $NAMESPACE -- zkCli.sh set $node '{}'
        echo "$node node initialised with empty JSON object"
    done

}

echo "************** Install Script For Demo **************"
echo "*****************************************************"

zookeeper_status=$(kubectl get pods --namespace $NAMESPACE -l "app.kubernetes.io/name=zookeeper,app.kubernetes.io/instance=siembol-zookeeper,app.kubernetes.io/component=zookeeper" -o jsonpath="{.items[0].status.containerStatuses[0].ready}")
if [ "$zookeeper_status" = true ]; then 
    git_details
    echo "************************************************************"
    init_zookeeper_nodes
else
    echo "Zookeeper pod is not running yet, please try again in a few seconds"
    exit 1
fi

echo "************************************************************"
echo "******  You can now deploy siembol from helm charts   ******"
echo "************************************************************"