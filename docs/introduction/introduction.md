# Siembol
## History
## Why is Siembol better than Metron
### Components for alert escalation
- Security teams can easily create a rule based alert from a single data source or they  can create  advanced correlation rules that combine various data sources
- We are planning to prepare a tool for translating a sigma rule specification (a standard for SIEM alerting) into siembol alerting rule engine
### Component for integration with other systems – siembol response
- Easy way how to integrate siembol with other systems such as Jira, The hive, Cortex, Elk, Ldap
- Functionality to provide additional information about an alert such as Elk search or ldap searches with possibility to filter alert as part of an automatic incident response
- Plugin interface that allows easy integration with other systems used in incident response
- We are planning to publish collection of plugins that we are using internally at GR
### Advanced parsing framework for building fault tolerant parsers
- Originally designed framework for parsing including chaining of extractors and transformations that allows extracting json, csv, key value, timestamps and transform message by renaming fields, filtering fields or even to filter the whole message
- Supporting timestamp formatters
- Supporting use cases for advanced parsing application with multiple parsers and a routing logic
- Supporting generic text parsers, syslog and Neflow v9 binary parser
### Advanced enrichment component
- Defining rules for selecting enrichment logic and joining enrichment tables
### Configurations and rules are defined by a web application siembol ui
- using json and web forms forms for editing rules/configurations
- Rules are stored in git repositories
- Support high integrity use cases with protected master branches for releasing 
- Supporting validation and testing configurations. Moreover it supports creating and evaluating test cases directly in siembol config editor ui.
- Instead of using a script language like Stellar in Metron siembol prefers a declarative json language that is less error prone and simpler to understand
- Supporting oauth2/oidc  for authentication and authorisation in siembol config editor and multitenancy  use cases
- All siembol services can  have multiple instances with authorisation based on oidc groups
- We are planning to test and tune oidc integration with major identity providers
### Easy installation to try it with prepared docker images and helm charts
## Use-Cases
### SIEM log collection using open source technologies 
### Detection tool for detection of leaks and attacks on infrastructure
## High Level Architecture
## Services
## Deployment
