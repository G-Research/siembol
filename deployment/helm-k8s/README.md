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
| `namespace`                | Name of namespace        | "siembol"           |
| `environment`              | Environment used         | "dev"               |
| `enabled_apps`             | List of apps to deploy  | [ui, rest, manager, dep_ingresses, enrichment_store] |

### Certmanager
| Parameter | Description         | Default  |
| ----------| --------------------| -------- |
| `enabled` | Enable cert manager | "true"   |

### TLS
| Parameter | Description | Default |
| ----------| ------------| --------|
| `enabled` | Enable TLS  | "true"  |

### Config Editor Rest
| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `image.repository` | Config Editor Rest image repository | "gresearchdev/siembol-config-editor-rest" |
| `image.tag` | Config Editor Rest image tag | "latest" |
| `image.pullPolicy` | Config Editor Rest image pull policy | "Always" |
| `appName` | Config Editor Rest app name | "config-editor-rest" |
