Siembol
=======

Siembol provides a scalable, advanced security analytics framework based on open-source big data technologies. Siembol normalizes, enriches, and alerts on data from various sources which allows security teams to respond to attacks before they become incidents.

History
-------

Siembol was developed in-house at G-Research as a security data processing application, forming the core of the G-Research Security Data Platform. We knew that we needed a highly efficient, real-time event processing engine and implemented both Splunk and Metron in the early years of our experience.  However, neither product attended to all of our needs -- we wanted specific features that mattered to G-Research.

As early adopters of Apache Metron, we believed in the product and tried hard to make Metron work for our needs. Ultimately, we recognised its limitations and began to add the missing features and shoring up its instabilities.  However, by the time we were able to give back to the Metron community, Metron's time had already passed.  Since we still believe in the core mission of Metron, we wanted to release our work under the project name 'siembol'.  We hope it will provide the security community an alternative for the void left by Metron's move to the Apache Attic.

How Siembol improves upon Metron
--------------------------------

### Components for alert escalation

- Security teams can easily create a rule-based alert from a single data source, or they can create advanced correlation rules that combine various data sources
- We are planning to prepare a tool for translating Sigma rule specification (generic and open signature format for SIEM alerting [https://github.com/SigmaHQ/sigma](https://github.com/SigmaHQ/sigma)) into the siembol alerting rule engine

### Component for integration with other systems – siembol response

- Easy way to integrate siembol with other systems such as Jira, The Hive, Cortex, ELK, LDAP
- Functionality to provide additional enrichments about an alert, such as ELK searches or ldap searches, with the option to filter the alert as part of an automatic incident response
- Plugin interface allowing for custom integration with other systems used in incident response
- We are planning to publish a collection of plugins that we are using internally at G-Research and provide space for collecting plugins from the siembol community

### Advanced parsing framework for building fault tolerant parsers

- Originally designed framework for normalising logs (parsing) including chaining of extractors and transformations which allow the user to: 
   - extract json, csv structures, key value pairs, timestamps 
   - parse timestamps using standard formatters to an epoch form 
   - transform messages by renaming fields, filtering fields, or even the option to filter the whole message
- Supporting use cases for advanced log ingestion using multiple parsers and a routing logic
- Supporting a generic text parser, syslog, BSD syslog, and Neflow v9 binary parser

### Advanced enrichment component

- Defining rules for selecting enrichment logic, joining enrichment tables, and defining how to enrich the processed log

### Configurations and rules are defined by a web application siembol UI

- All configurations are stored in json format and edited by web forms in order to avoid mistakes and speed-up the creation and learning time
- Configurations are stored in git repositories
- Supporting high integrity use cases with protected GitHub main branches for deploying configurations
- Supporting validation and testing configurations. Moreover, siembol UI supports creating and evaluating test cases
- Siembol prefers a declarative json language rather than a script language like Stellar. We consider declarative language with testing and validation less error prone and simpler to understand
- Supporting oauth2/oidc  for authentication and authorisation in siembol UI
- All siembol services can have multiple instances with authorisation based on oidc group membership. This allows multitenancy usage without the need to deploy multiple instances of siembol
- We are planning to test and tune oauth2/oidc integration with popular identity providers

### Easy installation to try out with prepared docker images and helm charts

- Siembol supports deployment on external hadoop clusters to ensure the high performance which we expect at GR. However, we are providing k8s helm charts for all deployment dependencies in order to test siembol in development environments.

Use-Cases
---------

### SIEM log collection using open source technologies

- Siembol can be used for a centralised security collecting and monitoring logs from different sources. Since we need to collect and inspect logs from third party tools, the format of logs is usually not under our complete control. Thus it is important for SIEM to support normalisation of logs into standardized format with common fields such as timestamp. It is often useful to enrich a log with metadata provided by cmdb or other internal systems which are important for building detections. For example, data repositories can be enriched by data classification, network devices by a network zone, username by active directory group, etc. Csirt team is using siembol for building detections on top of normalised logs using siembol alerting services. Alerts triggered from the detections are integrated in incident response is defined and evaluated by siembol response service. This allows for integration of siembol with systems such as Jira, The Hive, or Cortex, and provides additional enrichments by searching ELK or doing Ldap queries. TODO: provide basic stats about siembol at GR

### Detection tool for detection of leaks and attacks on infrastructure

- Siembol can be used as a tool for detecting attacks or leaks by teams responsible for the system platform. For example, the Big Data team at G-Research is using siembol to detect leaks and attacks on the Hadoop platform. These detections are then used as another data source in siembol SIEM log collection for the Csirt team which handles these incidents.

High Level Architecture
-----------------------

### Data Pipelines

![pipelines](images/pipelines.svg)

### Services

- Parsing - normalising logs into messages with one layer of key/value pairs
- Enrichment - adding useful data to events to assist in detection and investigations
- Alerting - filtering matching events from an incoming data stream of events based on a configurable rule set. The correlation alerting allows one to group several detections together before raising an alert
- Response

### Infrastructure dependencies

- Kafka - message broker for data pipelines
- Storm - stream processing framework for services except siembol response integrated in kafka streaming
- Github - store for service configurations used in siembol UI
- Zookeeper - synchronisation cache for updating service configurations from git to services
- kubernetes cluster - environment to deploy siembol UI and related microservices for management and orchestration of siembol services configurations 
- Identity provider - identity provider (oauth2/oidc) used for siembol UI, allowing for oidc groups in managing authorisation to services

### Architecture

![pipelines](images/architecture.svg)
