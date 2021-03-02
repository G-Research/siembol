# Siembol
Siembol provides a scalable, advanced security analytics framework based on open-source big data technologies. Siembol normalizes, enriches, and alerts on data from various sources which allows security teams to respond to attacks before they become incidents.
## History
Siembol is an in-house developed security data processing application, forming the core of GR Security Data Platform. Following our experience of using Splunk and Apache Metron it was clear that we needed a highly efficient, real-time event processing engine with features that mattered to GR. We were early adopters of Apache Metron and recognised its limits and missing features that we aimed to implement in siembol. 
## How Siembol improves upon Metron
### Components for alert escalation
- Security teams can easily create a rule based alert from a single data source or they  can create  advanced correlation rules that combine various data sources
- We are planning to prepare a tool for translating a sigma rule specification (a standard for SIEM alerting) into siembol alerting rule engine
### Component for integration with other systems – siembol response
- Easy way how to integrate siembol with other systems such as Jira, The hive, Cortex, Elk, Ldap
- Functionality to provide additional enrichments about an alert such as Elk searches or ldap searches with possibility to filter the alert as a part of an automatic incident response
- Plugin interface that allows to implement custom integration with other systems used in incident response
- We are planning to publish collection of plugins that we are using internally at GR and provide space for collecting plugins from the siembol community
### Advanced parsing framework for building fault tolerant parsers
- Originally designed framework for normalising logs (parsing) including chaining of extractors and transformations that allows 
   - extracting json, csv structures, key value pairs, timestamps 
   - parse timestamp using standard formatters to an epoch form 
   - transform message by renaming fields, filtering fields or even possibility to filter the whole message
- Supporting use cases for advanced log ingestion using multiple parsers and a routing logic
- Supporting a generic text parser, syslog, BSD syslog and Neflow v9 binary parser
### Advanced enrichment component
- Defining rules for selecting enrichment logic, joining enrichment tables and defining how to enrich the processed log

### Configurations and rules are defined by a web application siembol ui
- All configurations are stored in json format and edited by web forms in order to avoid mistakes and speed-up the creation and learning time
- Configurations are stored in git repositories
- Supporting high integrity use cases with protected github main branches for deployong configurations
- Supporting validation and testing configurations. Moreover, siembol ui supports creating and evaluating test cases
- Siembol prefers a declarative json language rather than a script language like Stellar. We consider declarative language with testing and validation less error prone and simpler to understand
- Supporting oauth2/oidc  for authentication and authorisation in siembol ui
- All siembol services can  have multiple instances with authorisation based on oidc groups. This allows multitenancy usage without need to deploy multiple instances of siembol
- We are planning to test and tune oauth2/oidc integration with major identity providers
### Easy installation to try it with prepared docker images and helm charts
- Siembol supports deployment on external hadoop cluster to ensure high performance which we are using at GR. However we are providing k8s helm charts for all deployemnt dependencies in order to try siembol in a developement environment.
## Use-Cases
### SIEM log collection using open source technologies
- Siembol can be used for a centralised security collecting and monitoring logs from different sources. The format of logs is usually not under our complete control since we need to collect and inspect logs from third party tools. This way it is important for the siem to support normalisation of logs into standardized format with common fields such as timestamp. It is often usefull to enrich a log about metadata provided by cmdb or other internal systems systems that are important for building detections. For example data repositories can be enriched by data clasiffication, network devices by a network zone, username by active directory group etc. Csirt team is using siembol for building detections on top of normalised logs using siembol alerting services. Alerts triggered from the detections are integrated in incident response defined and evaluated by siembol response service. This allows integration of siembol with systems such as Jira, The Hive, Cortex and provide additional enrichments by seraching Elk, doing Ldap queries. TODO: basic stats about siembol at GR

### Detection tool for detection of leaks and attacks on infrastructure
- Siembol can be used as a tool for detecting attacks or leaks by teams responsible for a ceratain system platform. Big data team at GR is using siembol for detecting leaks on Hadoop platform. These detections are used as just other data source in siembol in SIEM log collection use case. 

## High Level Architecture
### Data Pipelines
TODO: diagram of typical data pipelines
### Services
- Parsing
- Enrichment
- Alerting
- Response
### Infrastructure dependencies
- Kafka 
- Storm
- Zookeeper
- Github
- k8s cluster
- Identity provider

### Architecture
TODO: diagram of software architecture
## Deployment
##