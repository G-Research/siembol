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
    - `latest` for tagging the latest image 
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