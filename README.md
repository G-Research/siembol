![Siembol](logo.svg)

[![Black Hat Arsenal](https://raw.githubusercontent.com/toolswatch/badges/54ad78bc63b24ce445e8241f179fe1ddeecf8eef/arsenal/usa/2021.svg)](https://www.blackhat.com/us-21/arsenal/schedule/index.html#siembol-an-open-source-real-time-siem-tool-based-on-big-data-technologies-24038)
[![Black Hat Arsenal](https://raw.githubusercontent.com/toolswatch/badges/master/arsenal/europe/2021.svg?sanitize=true)](https://www.blackhat.com/eu-21/arsenal/schedule/index.html#siembol-an-open-source-real-time-siem-tool-based-on-big-data-technologies-25165)
[![Black Hat Arsenal](https://raw.githubusercontent.com/toolswatch/badges/master/arsenal/usa/2022.svg?sanitize=true)](https://www.blackhat.com/us-22/arsenal/schedule/#siembol-an-open-source-real-time-siem-tool-based-on-big-data-technologies-27927)

[![Apache License](https://img.shields.io/badge/License-Apache%202.0-blue)](https://www.apache.org/licenses/LICENSE-2.0)

# Siembol 

Siembol provides a scalable, advanced security analytics framework based on open-source big data technologies. Siembol normalizes, enriches, and alerts on data from various sources, which allows security teams to respond to attacks before they become incidents.

Introduction
------------

Siembol is an open-source, real-time security information and event management tool developed in-house at G-Research.
 
Siembol's use cases:

* **SIEM Log Collection Using Open Source Technologies**

Siembol can be used to centralize both security data collecting and the monitoring of logs from different sources.

* **Detection of Leaks and Attacks on Infrastructure**

Siembol can be used as a tool for detecting attacks or leaks by teams responsible for the system platform. 

For more extensive introduction, visit: [Introduction](/docs/introduction/introduction.md).

Installation
------------

To install locally, visit: [Quickstart Guide](/docs/introduction/how-tos/quickstart.md).

How to contribute
-----------------

If you wish to contribute to Siembol, first read: [Contribution Guide](/docs/introduction/how-tos/how_to_contribute.md).

#### Code of Conduct
G-Research has adopted a Code of Conduct that is to be honored by everyone who participates in the Siembol community formally or informally.
Please read the full text: [Code of Conduct](/CODE_OF_CONDUCT.md)

####
All notable changes to this project are documented in this file: [CHANGELOG](/CHANGELOG.md)

Siembol UI
-------------

To learn more about Siembol's UI, visit: [Siembol UI](/docs/siembol_ui/siembol_ui.md).

There you will find guides on:
- [Adding a new configuration](/docs/siembol_ui/how-tos/how_to_add_new_config_in_siembol_ui.md)
- [Submitting configurations](/docs/siembol_ui/how-tos/how_to_submit_config_in_siembol_ui.md)
- [Importing a sigma rule](/docs/siembol_ui/how-tos/how_to_import_sigma_rules.md)
- [Releasing configurations](/docs/siembol_ui/how-tos/how_to_release_configurations_in_siembol_ui.md)
- [Testing configurations](/docs/siembol_ui/how-tos/how_to_test_config_in_siembol_ui.md)
- [Testing release](/docs/siembol_ui/how-tos/how_to_test_release_in_siembol_ui.md)  
- [Adding links to the homepage](/docs/siembol_ui/how-tos/how_to_add_links_to_siembol_ui_home_page.md)
- [Setting up OAUTH2 OIDC](/docs/siembol_ui/how-tos/how_to_setup_oauth2_oidc_in_siembol_ui.md)
- [Modifying the layout](/docs/siembol_ui/how-tos/how_to_modify_ui_layout.md)
- [Managing applications](/docs/siembol_ui/how-tos/how_to_manage_applications.md)
- [Use ui-bootstrap file](/docs/siembol_ui/how-tos/how_to_use_ui_bootstrap_file.md)
- [Filter configs and save searches](/docs/siembol_ui/how-tos/how_to_filter_configs_and_save_searches.md)

Services
---------

To explore Siembol's services, visit: [Siembol services](/docs/services/services.md).

There you will find guides on:
- [Setting up a service in the config editor rest](/docs/services/how-tos/how_to_set_up_service_in_config_editor_rest.md)
- [Alerting service](/docs/services/siembol_alerting_services.md)
- [Parsing service](/docs/services/siembol_parsing_services.md)
  - [Setting up NetFlow v9 parsing](/docs/services/how-tos/how_to_setup_netflow_v9_parsing.md)
- [Enrichment service](/docs/services/siembol_enrichment_service.md)
  - [Setting up an enrichment table](/docs/services/how-tos/how_to_set_up_enrichment_table.md)
- [Response service](/docs/services/siembol_response_service.md)
  - [Writing a response plugin](/docs/services/how-tos/how_to_write_response_plugin.md)
        
Deployment
----------

To deploy Siembol, refer to: [Siembol deployment](/docs/deployment/deployment.md).

There you will find guides on:
- [Setting up ZooKeeper nodes](/docs/deployment/how-tos/how_to_set_up_zookeeper_nodes.md)
- [Setting up a GitHub webhook](/docs/deployment/how-tos/how_to_setup_github_webhook.md)
- [Tuning the performance of Storm topologies](/docs/deployment/how-tos/how_to_tune_performance_of_storm_topologies.md)
- [Setting up Kerberos for external dependencies](/docs/deployment/how-tos/how_to_set_up_kerberos_for_external_dependencies.md)
- [Customizing Helm chart](/docs/deployment/how-tos/how_to_customize_helm_charts.md)
