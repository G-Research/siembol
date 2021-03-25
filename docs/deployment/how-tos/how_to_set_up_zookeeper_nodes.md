# How to set-up Zookeeper nodes for Siembol configuration
Siembol services watch zookeeper nodes for configuration. Zookeeper nodes notify the storm topologies when updates occur allowing for configuration changes without the need to restart components.

You will need to create the zookeeper nodes prior to running Siembol services. To create a node connect to a zookeeper server and run a command like the following:

```
zookeeper@siembol-storm-zookeeper-0:/apache-zookeeper-3.5.5-bin$ bin/zkCli.sh
Connecting to localhost:2181
...
[zk: localhost:2181(CONNECTED) 3] create /siembol/node
```

To initialise it with any empty JSON object:
```
zookeeper@siembol-storm-zookeeper-0:/apache-zookeeper-3.5.5-bin$ bin/zkCli.sh
Connecting to localhost:2181
...
[zk: localhost:2181(CONNECTED) 3] set /siembol/node '{}'
```

 
## Zookeeper nodes for configuration deployments
### Admin configuration settings

When Siembol services are launched in Storm, they are given the zookeeper node to watch for configuration updates. If we take an alerting component as an example, we can navigate to the Admin interface in the Siembol UI for the service and view zookeeper settings. 

`Siembol UI -> Alerts -> Admin -> Zookeeper Attributes`

![](images/alerts-zookeeper.jpg)

### Config editor rest application properties

The [config editor rest service](../../services/how-tos/how_to_set_up_service_in_config_editor_rest.md) needs to have the same zookeeper node mentioned above added into it's `application.properties`. This is added with the following entry: 

```
config-editor.services.alert.release-zookeeper.zk-path=/siembol/alerts/rules
```

These zookeeper nodes now ensure that any change to alerting rules in the Siembol UI will be deployed to alerting instances running in Storm. 

## Zookeper nodes for storm topology manager

The storm topology manager is responsible for the orchestration of Storm topologies Siembol requires. It does this by listening to a zookeeper synchronisation node, which the configuration rest service publishes a desired state to. The service will use an internal cache node to persists state, and continually try to reconcile any differences. 

Therefore it is required to have two zookeeper nodes for this to work. The synchronization zookeeper node and the cache node. The configuration rest service only requires access to write to the sync node, and it is set in in the [configuration rest services](../../services/how-tos/how_to_set_up_service_in_config_editor_rest.md) `application.properties`:

```
config-editor.storm-topologies-zookeeper.zk-path=/siembol/topologies/synchronise
```

The storm topology manager service requires read access to the synchronise node, and read/write access to it's internal cache node. Both nodes can be configured in the storm-topology-manager's `application.properties` file:

```
topology-manager.desired-state.zk-path=/siembol/topologies/synchronise
topology-manager.saved-state.zk-path=/siembol/topologies/cache
```
