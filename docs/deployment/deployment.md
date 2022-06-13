# Deployment
## Build artifacts
Building and publishing artifacts are executed by [GitHub Actions](/.github/workflows/ci.yml) triggered by events in the siembol repository.

### Java artifacts
Java artifacts are published to Central Maven Repository - [Sonatype OSS Repository Hosting](https://central.sonatype.org/pages/ossrh-guide.html)
- Snapshots - They are built if the version in [POM](/pom.xml) contains `SNAPSHOT`. Snapshots are usually not stable and we suggest to use releases in a production environment
- Releases - They are built if the version in [POM](/pom.xml) does not contain `SNAPSHOT` and are published in Central Maven Repository

### Docker images
Docker images are built both from snapshots and releases. 
- The images are tagged by two tags:
    - `latest` for tagging an image with the latest released stable version or `snapshot` for tagging an image with the latest snapshot development version
    - The version of the application from [POM](/pom.xml) 
- Springboot applications
    - An application is loaded using  [springboot properties launcher](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html)
    - [dockerfile](/deployment/docker/Dockerfile.java)
    - [storm-topology-manager](https://hub.docker.com/r/gresearchdev/siembol-storm-topology-manager/)
    - [config-editor-rest](https://hub.docker.com/r/gresearchdev/siembol-config-editor-rest/)
    - [responding-stream](https://hub.docker.com/r/gresearchdev/siembol-responding-stream/)

- Config editor UI
    - A Single page Angular application 
    - nginx-server with configurations
    - [dockerfile](/deployment/docker/Dockerfile.config-editor-ui)

- Storm topologies
    - Images are used for launching storm topologies by storm topology manager
    - [dockerfile](/deployment/docker/Dockerfile.storm)
    - Storm cli
    - Siembol java storm topology artifact
    - [parsing-storm](https://hub.docker.com/r/gresearchdev/siembol-parsing-storm/)
    - [enriching-storm](https://hub.docker.com/r/gresearchdev/siembol-enriching-storm/)
    - [alerting-storm](https://hub.docker.com/r/gresearchdev/siembol-alerting-storm/)

## Infrastructure dependencies
- k8s cluster - environment to deploy siembol ui and related micro services for managements and orchestration of siembol services configurations
    - [siembol ui](../siembol_ui/siembol_ui.md) - It combines config editor rest and config editor ui. This deployment also contains synchronisation service for synchronisation of releases, admin configurations and storm topologies
    - [Siembol response](../services/siembol_response_service.md)
    - Storm topology manager - It is used for releasing siembol storm topologies that are specified by config editor rest synchronisation service from siembol configurations 

- Github 
    - A git repository store for a service configurations that are used in [siembol ui](../siembol_ui/siembol_ui.md)
    - Pull Requests API for deploying releases and admin configuration in siembol UI
    - [Web hooks](how-tos/how_to_setup_github_webhook.md) for triggering config editor rest synchronisations of releases, admin configurations and storm topologies

- Zookeeper - synchronisation cache for 
    - Updating service deployments configurations from git repositories to services
    - Updating desired state of storm topologies that should be released
    - Storing a state of storm topology manager in order to save information about topologies there were already released
    - [how to set-up zookeeper nodes](how-tos/how_to_set_up_zookeeper_nodes.md)
    - See [how to set-up kerberos](how-tos/how_to_set_up_kerberos_for_external_dependencies.md) if you need to use kerberised storm cluster in your siembol deployment

- Kafka - message broker for data pipelines. See [how to set-up kerberos](how-tos/how_to_set_up_kerberos_for_external_dependencies.md) if you need to use kerberised kafka cluster in your siembol deployment

- Storm - stream processing framework for siembol services except siembol response integrated in kafka streaming and deployed on k8s. See [how to set-up kerberos](how-tos/how_to_set_up_kerberos_for_external_dependencies.md) if you need to use kerberised storm cluster in your siembol deployment

- Identity provider - identity povider (oauth2/oidc) used for siembol ui for
    - Authentication - Only authenticated users can access siembol ui and all commits in git repositories which are performed by siembol ui contain user profile including username and email

    - Authorisation - You can specify oidc groups for managing authorisation access to services, see [how to set-up a service authorisation in config editor rest](../services/how-tos/how_to_set_up_service_in_config_editor_rest.md)

    - See [how to set-up oauth2](../siembol_ui/how-tos/how_to_setup_oauth2_oidc_in_siembol_ui.md)
## Deployment scenarios
## Helm charts

We have developed a chart which bootstraps a Siembol deployment on a Kubernetes cluster using the Helm package manager. By using this Helm chart, Siembol can be deployed with the default [configuration](../../deployment/helm-k8s/README.md#configuration). 

There are a few dependencies that Siembol rely on which should be deployed in the namespace first, these are Storm and ZooKeeper. Kafka can be embedded, but there is support to connect to external kerberised Kafka cluster. See [how to set-up kerberos](how-tos/how_to_set_up_kerberos_for_external_dependencies.md). 

By following the [Quickstart Guide](../introduction/how-tos/quickstart.md#2-install-dependencies) you can easily deploy Storm and Zookeeper before deploying Siembol. As part of the [dependencies script](../../deployment/quickstart_install/sh-scripts/dependencies.sh), there are a few default parameters, these can be modified e.g. to add more Storm supervisors and more ZooKeeper replicas. Also, you will find that Kafka will be deployed as part of this script, which is useful to test parsing and enriching of a log message. When you have Zookeeper and Storm deployed, you can deploy Siembol. The core components provided as part of the Helm chart are:

 -  config-editor-rest
 -  config-editor-ui
 -  storm-topology-manager

You can find the default parameters in [configuration](../../deployment/helm-k8s/README.md#configuration).

Moreover, we have a few additional components: 
#### Enrichment Store 
This component is a web server which acts as a store for enrichment tables. Functionality to upload, view and download JOSN files is supported. There is also support to specify and create directories.

To create and upload an enrichment table:

```bash
echo '{"1.2.3.4":{"hostname":"test-name"}}' > hostname.json
curl -F "uploaded_file=@hostname.json;" https://enrichment.siembol.local/upload.php
```

You can specify a directory path when uploading a file by adding the `directory_path` key:
```bash
curl -F "uploaded_file=@hostname.json;" -F "directory_path=/my_path" https://enrichment.siembol.local/upload.php
```

To check out an uploaded enrichment table, go to this url in the browser:
```bash
https://enrichment.siembol.local/download.php?filename=hostname.json
```

To check out all uploaded enrichment tables and directories, go to this url in the browser:
```bash 
https://enrichment.siembol.local/
``` 

Currently enrichment store supports files up to 30 MB and this can easily be modified as required [here](../../deployment/helm-k8s/resources/php.ini-local)

#### Oauth2 Proxy
There is support for Oauth2 Proxy which is a lightweight deployment that provides authentication using Providers such as Google, GitHub and others.
This can be deployed by adding the Helm chart and specifying the required values.
```bash
helm repo add oauth2-proxy https://oauth2-proxy.github.io/manifests
helm install -f oauth-values.yaml my-release oauth2-proxy/oauth2-proxy
```
Example values: [oauth-values.yaml](../../deployment/helm-k8s/oauth-values.yaml):

Environment variables under `oauth2-proxy.extraEnvVars`:
| Name                  | Default value              | Description             |
| ---------------------------| -------------------------| ------------------- |
| `OAUTH2_PROXY_REDIRECT_URL` | https://oauth-proxy.siembol.local/oauth2/callback | The OAuth Redirect URL |
| `OAUTH2_PROXY_CLIENT_ID` | "Your Client ID" | The OAuth Client ID |
| `OAUTH2_PROXY_CLIENT_SECRET` | "Your Client Secret" | The OAuth Client Secret |
| `OAUTH2_PROXY_COOKIE_SECRET` | "YOUR SECRET"| The Cookie Secret |
| `OAUTH2_PROXY_COOKIE_DOMAIN` | .siembol.local | Cookie domain to force cookies to (e.g. .siembol.local) |
| `OAUTH2_PROXY_COOKIE_EXPIRE` | 8h | Expire timeframe for cookie |
| `OAUTH2_PROXY_COOKIE_NAME` | _siembol-oauth-proxy | The name of the cookie that the oauth_proxy creates |

| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `oauth2-proxy.ingress.enabled` | Enable ingress | true |
| `oauth2-proxy.ingress.hosts` | List of ingress hosts | - oauth-proxy.siembol.local |
| `oauth2-proxy.ingress.tls.secretName` | Name of TLS secret | oauth2-proxy-tls |
| `oauth2-proxy.ingress.tls.hosts` | List of TLS hosts | - oauth-proxy.siembol.local |
| `oauth2-proxy.redis.enabled` | Enable redis | false |

For further configuration, please see [Oauth2 Proxy docs](https://oauth2-proxy.github.io/oauth2-proxy/docs/configuration/overview/)



#### Ingress for dependencies
We have a folder for ingress specifications for extra components which allows for these components to be deployed with an Ingress and also the option to run behind Oauth2 proxy. 

### Enrichment Store
| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `dep_ingresses.enrichment_store.enabled` | Enable ingress | true |
| `dep_ingresses.enrichment_store.fqdn` | Fully qualified domain name | enrichment.siembol.local |
| `dep_ingresses.enrichment_store.oauth2_proxy.enabled` | Enable oauth2 proxy for this ingress | false |
| `dep_ingresses.enrichment_store.oauth2_proxy.host` | Host for oauth2 proxy | oauth-proxy.siembol.local |

### Storm
| Parameter                  | Description              | Default             |
| ---------------------------| -------------------------| ------------------- |
| `dep_ingresses.storm.enabled` | Enable ingress | true |
| `dep_ingresses.storm.fqdn` | Fully qualified domain name | storm.siembol.local |
| `dep_ingresses.storm.service.name` | Service name | storm-ui |
| `dep_ingresses.storm.service.port` | Port for service | 8080 |
| `dep_ingresses.storm.oauth2_proxy.enabled` | Enable oauth2 proxy for this ingress | false |
| `dep_ingresses.storm.oauth2_proxy.host` | Host for oauth2 proxy | oauth-proxy.siembol.local |



### Enable & Disable components

These components can be enabled or disabled by modifying the `enabled_apps` list.
By default the enabled_apps list consists of these components:
```bash
- ui
- rest
- manager
- dep_ingresses
- enrichment_store
```
Any component can be removed by removing it from the list in [values.yaml](../../deployment/helm-k8s/values.yaml) or you can add another component such as `response` e.g.
```bash
- ui
- rest
- manager
- dep_ingresses
- enrichment_store
- response
```

### Customize Helm Chart
When you use the Siembol chart and other charts such as Storm, Zookeeper etc. some of the configuration options can be limited for your use case. If you need to customise the deployments in ways of your own, you might fork the chart to create your own custom version. If you do this, each time the maintainers update its Helm chart, your custom version becomes out of snc and possibly obsolete. To keep your version up-to-date, you would need to pull from upstream for every update. 

### Can you customize a Helm chart without forking?
Yes, with [Kustomize](https://kustomize.io/), you can use it to perform custom deployments while always using the latest Helm chart version from your vendor. Kustomize enables you to overlay your own 'kustomizations' in yaml files. We have used it by first rendering the chart template locally, and then apply the Kustomize overlay when we deploy the app. This is very useful when deploying the same app to multiple environments, but with different combinations of requirements for each environment. For example a certain port or label is different from dev and prod environments, and in these scenarios it may be more flexible to apply a different Kustomize overlay to the same rendered Helm chart for each environment.

1. Render Helm chart using helm template command:
```bash
$ helm template storm --values values.qa.yaml . > new_templates/temp.yaml
```
The above command outputs a YAML file with all values from the values.yaml file resolved for the templates.

2. Create a new kustomization file to add e.g. labels to a pod:
```bash
$ cat new_templates/kustomization.yaml
commonLabels:
    app: kustomLabel
resources:
- new_templates/temp.yaml
```
3. Install our chart with the new label:
```bash
$ kubectl apply -k new_templates/.
```
4. We can see that our own kustomization has been applied and deployed together with the upstream chart.
```bash
kubectl get deploy storm-ui --show-labels
```
```
NAME     READY  UP-TO-DATE AVAILABLE AGE LABELS
storm-ui   1/1    1            1     10s   app=storm-ui,chart=storm-1.0.14,heritage=Helm,app=kustomLabel
```
