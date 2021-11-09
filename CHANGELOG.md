# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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