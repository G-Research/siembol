# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.6.0] - 2022-12-15

- Fixing error messages in Siembol UI
- Improved initialisation in Siembol response
- Reworking configuration testing in config editor rest module
- Adding numeric compare matcher in Siembol alerting
- Sending correlated alerts after matching in Siembol correlation alerting
- Minor refactoring of Siembol monitoring
- Adding javadoc to Siembol java modules
- Upgrading ngx-formly dependency to version 6.0.2 in Siembol UI


## [2.5.0] - 2022-06-30

- Adding siembol-monitoring module with siembol-heartbeat service
- Simplifying parsing app parallelism settings
- Adding field protection in parsing
- Sorting fields in Markdown table formatter in Siembol response
- Adding metadata fields to enrichment tables
- Adding gauge type in Siembol common metrics
- Upgrading alerting spark dependencies and refactoring to be more generic
- Adding time exclusion evaluator in Siembol response
- Adding json path extractor in parsing
- Fixing computing desired state in config editor rest sync service
- Rejecting rules with only negated matchers in alerting
- Adding optional items in Siembol UI and configurations

## [2.4.0] - 2022-04-29

- Upgrading to Java 17 in java modules
- Upgrading to Storm 2.4.0
- Improving error messages in Siembol UI
- Reworking filtering and searching in Siembol UI
- Redesigning config manager view in Siembol UI
- Compressing json in ZooKeeper connector
- Fixing initialisation of provided evaluators in Siembol response

## [2.3.0] - 2022-03-11

- Adding kafka writer evaluator in Siembol response
- Fixing handling empty strings in csv parser in Siembol parsing
- Various performance improvements in Siembol UI
- Adding CONTAINS matcher in Alerting
- Supporting cloning configurations with test cases in Siembol UI

## [2.2.0] - 2022-02-04

- Reworking kafka writing in Storm applications to use async sending in order to improve performance
- Deprecating custom batching in kafka writer and use Storm and Kafka batching instead 
- Upgrading Apache Kafka dependency to 3.1.0 and Spring Boot applications to 2.6.3 
- Upgrading Angular to 13.2.0 in Siembol UI
- Adding management page into Siembol UI including management links and restarting all Siembol Storm applications
- Adding topic routing parsing and header routing parsing application types
- Various fixes required for Siembol k8s deployment

## [2.1.0] - 2021-11-19

- Supporting autocreation of ZooKeeper nodes in ZooKeeper connector
- Upgrading Apache Curator dependency to 5.2.0
- Fixing dependencies in config editor rest that reduces the size of the package 
- Truncate logs during updating configurations in order not to leak sensitive data

## [2.0.0] - 2021-11-09

- Upgrading Java to 11 (Java 13 in tests)
- Upgrading Storm to 2.3.0
- Supporting downloading enrichment tables from http store
- Adding Config editor rest API for updating enrichment tables
- Fixing extracting csv with last empty column in Siembol parsing
- Improvements in Manage Applications dialog in Siembol UI
- Various upgrades of dependencies in java modules and config editor ui

## [1.3.0] - 2021-09-09

- Loading rules from multiple ZooKeeper nodes in Siembol alerting
- Fixing adding tags in Siembol correlation alerts
- Various minor fixes in Sigma rule importer in Siembol UI
- Various minor fixes in Siembol UI - uppercase searches for tags, tags in deployment dialog validated before release
- Adding Application manager in Siembol UI for managing Siembol applications (storm topologies)

## [1.2.0] - 2021-07-19

- adding composite matchers (and, or) in Siembol alerting
- Adding Sigma rule importer in Siembol UI
- Adding editor features (copy, paste, undo, redo) in Siembol UI
- Improved Siembol response kafka stream integration

## [1.1.0] - 2021-05-17

- Supporting deleting configs in Siembol UI
- Supporting deleting test cases in Siembol UI

## [1.0.0] - 2021-04-27

- First public release 
