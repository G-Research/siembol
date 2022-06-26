Quickstart Guide
================

Local Install
----------------

### 1. Run minikube.sh

```bash
deployment/quickstart_install/sh-scripts/minikube.sh
```
or
```bash
deployment/quickstart_install/ps-scripts/minikube.ps1
```

### 2. Install dependencies
#### 1. Run dependencies.sh
```bash
deployment/quickstart_install/sh-scripts/dependencies.sh
```
or
```bash
deployment/quickstart_install/ps-scripts/dependencies.ps1
```

### 3. Prepare GitHub

#### 1. Prepare Siembol Config Repository

1. Go to https://github.com/G-Research/siembol-config
2. Fork into your own organization or personal account

#### 2. Create GitHub token

1. Go to https://github.com/settings/tokens
2. Click Generate new token
4. Select "repo - Full control of private repositories" scope
5. Hit "Generate token"
6. Keep this token value as you will need it for the next step.

#### 3. Run demoInstall.sh
1. This will ask for your github details related to the Siembol Config repository and the token created in previous step.
2. This will also initialise Zookeeper nodes.

```bash
deployment/quickstart_install/sh-scripts/demoInstall.sh
```
or
```bash
deployment/quickstart_install/ps-scripts/demoInstall.ps1
```

### 4. Siembol install

To install Siembol in the cluster

```bash
helm install siembol deployment/helm-k8s/ -f deployment/helm-k8s/values.yaml -n=siembol
```

This step might take a few minutes depending on the specs of your development machine.

### Check it out!

In a browser, go to:

  * https://ui.siembol.local/home

You should now see the Siembol UI homepage. You can also try Storm UI to see running topologies:

  * https://storm.siembol.local

### Enrichment tables

To add enrichment tables.

```bash
deployment/quickstart_install/sh-scripts/enrichmentStore.sh
```
or
```bash
deployment/quickstart_install/ps-scripts/enrichmentStore.ps1
```


### Kafka UI

We are using Kafdrop UI to view topics and messages.  You can send messages to parsing topics and to test siembol configs. 

1. To install this and create a kafka client pod:
```bash
deployment/quickstart_install/sh-scripts/kafkaExtra.sh
```
or 
```bash
deployment/quickstart_install/ps-scripts/kafkaExtra.ps1
```

2. Exec into the kafka client pod:
 ```bash
 kubectl exec --tty -i kafka-client --namespace siembol -- bash
 ```
 3. Connect to the broker:
 ```bash
 kafka-console-producer.sh \
--broker-list kafka-0.kafka-headless.siembol.svc.cluster.local:9092 \
--topic <your-topic>
 ```
 4. Produce your message in terminal window

### Monitoring

We are scraping Siembol metrics using Prometheus and displaying the metrics in a Grafana dashboard. 

To install these components:

1. Download charts:
```bash
helm dependency update deployment/helm-k8s/monitoring/
```
2. Install the charts:
```bash
helm install monitoring deployment/helm-k8s/monitoring/ -n=siembol
```

To see the dashboard:

  * http://grafana.siembol.local/

You can also see the raw Prometheus metrics from:

  * http://prometheus.siembol.local/

## Cleaning up
If you're done poking about on a local instance, you can clean up with:

1. For cleaning up siembol resources and dependencies:
```bash 
deployment/quickstart_install/sh-scripts/cleanUp.sh
```
or 
```bash 
deployment/quickstart_install/ps-scripts/cleanUp.ps1
```
2. For deleting everything; delete the siembol minikube profile:
```bash
minikube delete -p siembol
sudo rm /etc/resolver/minikube-*
```
