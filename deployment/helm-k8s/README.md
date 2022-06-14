# Siembol 

## Introduction 
This chart bootstraps a Siembol deployment on a Kubernetes cluster using the Helm package manager.

## Installing the Chart

To install the chart with the release name `siembol`: 

```bash
$ helm repo add gresearch https://g-research.github.io/charts
$ helm install siembol gresearch/siembol
```
These commands deploy Siembol on the Kubernetes cluster with the default configuration. The [Configuration](#configuration) section lists the parameters that can be configured during installation.

> **Tip**: List all releases using: `helm search repo gresearch/siembol --versions`

## Uninstalling the Chart

To uninstall/delete the `siembol` deployment:

```bash
$ helm delete siembol
```
The command removes all the Kubernetes components associated with the chart and deletes the Helm release.

## Configuration

### Common parameters

| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `namespace`                | Name of namespace        | siembol           |
| `environment`              | Environment used         | dev               |
| `enabled_apps`             | List of apps to deploy  | [ui, rest, manager, dep_ingresses, enrichment_store] |

### Certmanager
| Parameter | Description         | Default  |
| ----------| --------------------| -------- |
| `certmanager.enabled` | Enable cert manager | true   |

### TLS
| Parameter | Description | Default |
| ----------| ------------| --------|
| `tls.enabled` | Enable TLS  | true  |

### Config Editor Rest
| Parameter                  | Description              | Default                 |
| ---------------------------| -------------------------| ----------------------- |
| `rest.appName` | Config Editor Rest app name | config-editor-rest |
| `rest.image.repository` | Config Editor Rest image repository | gresearchdev/siembol-config-editor-rest |
| `rest.image.tag` | Config Editor Rest image tag | latest |
| `rest.image.pullPolicy` | Config Editor Rest image pull policy | Always |
| `rest.containerPort` | Port of pod | 8081 |
| `rest.mountPath` | Path to mount config | /opt/config-editor-rest |
| `rest.rulesDir` | Path to temp rules directory | /tmp/siembol-config |
| `rest.configmap.enabled` | Enable configmap with github details | true |
| `rest.configmap.name` | Name of configmap for github details | github-details |
| `rest.service.port` | Service port | 8081 |
| `rest.ingress.enabled` | Enable ingress | true |
| `rest.ingress.fqdn` | Fully qualified domain name | rest.siembol.local |
| `rest.javaOpts` | Specify Java opts | -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts |
| `rest.resources.requests.cpu` | Pod CPU requests | 250m |
| `rest.resources.requests.memory` | Pod memory requests | 256Mi |
| `rest.resources.limits.cpu` | Pod CPU limits | 500m |
| `rest.resources.limits.memory` | Pod memory limits | 512Mi |
| `rest.readinessProbe.initialDelaySeconds` | Initial delay seconds for readinessProbe | 100 |
| `rest.readinessProbe.periodSeconds` | Period seconds for readinessProbe | 30 |
| `rest.livenessProbe.initialDelaySeconds` | Initial delay seconds for livenessProbe | 100 |
| `rest.livenessProbe.periodSeconds` | Period seconds for livenessProbe | 10 |

### Config Editor UI
| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `ui.appName` | Config Editor UI app name | config-editor-ui |
| `ui.image.repository` | Config Editor UI image repository | gresearchdev/siembol-config-editor-ui |
| `ui.image.tag` | Config Editor UI image tag | latest |
| `ui.image.pullPolicy` | Config Editor UI image pull policy | Always |
| `ui.containerPort` | Port of pod | 8080 |
| `ui.service.port` | Service target port | 8080 |
| `ui.service.intport` | Service port | 80 |
| `ui.ingress.enabled` | Enable ingress | true |
| `ui.ingress.fqdn` | Fully qualified domain name | ui.siembol.local |
| `ui.resources.requests.cpu` | Pod CPU requests | 0.1 |
| `ui.resources.requests.memory` | Pod memory requests | 128Mi |
| `ui.resources.limits.cpu` | Pod CPU limits | 0.5 |
| `ui.resources.limits.memory` | Pod memory limits | 256Mi |
| `ui.readinessProbe.initialDelaySeconds` | Initial delay seconds for readinessProbe | 15 |
| `ui.readinessProbe.periodSeconds` | Period seconds for readinessProbe | 30 |
| `ui.livenessProbe.initialDelaySeconds` | Initial delay seconds for livenessProbe | 300 |
| `ui.livenessProbe.periodSeconds` | Period seconds for livenessProbe | 30 |


### Storm Topology Manager
| Parameter                  | Description              | Default                     |
| ---------------------------| -------------------------| --------------------------- |
| `manager.appName` | storm-topology-manager | storm-topology-manager |
| `manager.image.repository` | Topology Manager image repository | gresearchdev/siembol-storm-topology-manager |
| `manager.image.tag` | Topology Manager image tag | latest |
| `manager.image.pullPolicy` | Topology Manager image pull policy | Always |
| `manager.containerPort` | Port of pod | 8082 |
| `manager.serviceAccountName` | Name of service account | storm-topology-controller |
| `manager.serviceAccountEnabled` | Deploy a service account | true |
| `manager.mountPath` | Path to mount config | /opt/storm-topology-manager |
| `manager.javaOpts` | Specify Java opts | -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts |
| `manager.service.port` | Service target port | 8082 |
| `manager.ingress.enabled` | Enable ingress | true |
| `manager.ingress.fqdn` | Fully qualified domain name | topology-manager.siembol.local |
| `manager.resources.requests.cpu` | Pod CPU requests | 250m |
| `manager.resources.requests.memory` | Pod memory requests | 128Mi |
| `manager.resources.limits.cpu` | Pod CPU limits | 500m |
| `manager.resources.limits.memory` | Pod memory limits | 512Mi |
| `manager.readinessProbe.initialDelaySeconds` | Initial delay seconds for readinessProbe | 30 |
| `manager.readinessProbe.periodSeconds` | Period seconds for readinessProbe | 30 |
| `manager.livenessProbe.initialDelaySeconds` | Initial delay seconds for livenessProbe | 30 |
| `manager.livenessProbe.periodSeconds` | Period seconds for livenessProbe | 10 |

### Siembol Response
| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `response.appName` | Siembol Response app name | response |
| `response.image.repository` | Config Editor UI image repository | gresearchdev/siembol-responding-stream |
| `response.image.tag` | Siembol Response image tag | latest |
| `response.image.pullPolicy` | Siembol Response image pull policy | Always |
| `response.service.port` | Service target port | 8080 |
| `response.ingress.enabled` | Enable ingress | true |
| `response.ingress.fqdn` | Fully qualified domain name | response.siembol.local |
| `response.health.path` | Path for healthcheck | /health |
| `response.health.port` | Port for healthcheck | 8080 |
| `response.javaOpts` | Specify Java opts | -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts | 
| `response.resources.requests.cpu` | Pod CPU requests | 0.5 |
| `response.resources.requests.memory` | Pod memory requests | 256Mi |
| `response.resources.limits.cpu` | Pod CPU limits | 1 |
| `response.resources.limits.memory` | Pod memory limits | 1024Mi |
| `response.readinessProbe.initialDelaySeconds` | Initial delay seconds for readinessProbe | 20 |
| `response.readinessProbe.periodSeconds` | Period seconds for readinessProbe | 30 |
| `response.livenessProbe.initialDelaySeconds` | Initial delay seconds for livenessProbe | 100 |
| `response.livenessProbe.periodSeconds` | Period seconds for livenessProbe | 10 |


### Enrichment Store
| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `enrichment_store.appName` | Enrichment Store app name | enrichment-store |
| `enrichment_store.image.repository` | Enrichment Store image repository | php |
| `enrichment_store.image.tag` | Enrichment Store image tag | 8.0-apache |
| `enrichment_store.image.pullPolicy` | Enrichment Store image pull policy | Always |
| `enrichment_store.securityContext.runAsUser` | Specify security context user for container | 82 |
| `enrichment_store.containerPort` | Port of pod | 80 |
| `enrichment_store.service.port` | Service target port | 80 |
| `enrichment_store.pvc.name` | Persistent volume claim name | files-store |
| `enrichment_store.pvc.storageclass` | Storage Class type | "" |
| `enrichment_store.pvc.storage` | Storage size | 1Gi |
| `enrichment_store.ingress.enabled` | Enable ingress | true |
| `enrichment_store.ingress.fqdn` | Fully qualified domain name | ui.siembol.local |
| `enrichment_store.security.user` | Specify security context for user | 82 |
| `enrichment_store.security.fsGroup` | Specify security context for group | 82 |
| `enrichment_store.security.runAsGroup` | Specify security group | 82 |
| `enrichment_store.resources.requests.cpu` | Pod CPU requests | 200m |
| `enrichment_store.resources.requests.memory` | Pod memory requests | 128Mi |
| `enrichment_store.resources.limits.cpu` | Pod CPU limits | 400m |
| `enrichment_store.resources.limits.memory` | Pod memory limits | 256Mi |


### Ingress for dependencies
| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `dep_ingresses.enrichment_store.enabled` | Enable ingress | true |
| `dep_ingresses.enrichment_store.fqdn` | Fully qualified domain name | enrichment.siembol.local |
| `dep_ingresses.enrichment_store.oauth2_proxy.enabled` | Enable Oauth2 Proxy for Enrichment Store | false |
| `dep_ingresses.enrichment_store.oauth2_proxy.host` | Oauth2 Proxy host name | oauth-proxy.siembol.local |
| `dep_ingresses.storm.enabled` | Enable ingress | true |
| `dep_ingresses.storm.fqdn` | Fully qualified domain name | storm.siembol.local |
| `dep_ingresses.storm.service.name` | Name of Service | storm-ui |
| `dep_ingresses.storm.service.port` | Service port | 8080 |
| `dep_ingresses.storm.oauth2_proxy.enabled` | Enable Oauth2 Proxy for Storm UI | false |
| `dep_ingresses.storm.oauth2_proxy.host` | Oauth2 Proxy host name | oauth-proxy.siembol.local |
