export const mockAdminConfigData =
{
    'alerts.correlation.output.topic': 'siembol.correlation.alerts',
    'alerts.engine': 'siembol_alerts',
    'alerts.engine.bolt.num.executors': 1,
    'alerts.engine.clean.interval.sec': 1,
    'alerts.input.topics': [
        'siembol.indexing',
    ],
    'alerts.output.topic': 'siembol.alerts',
    'alerts.topology.name': 'siembol_alerts',
    config_version: 1,
    'kafka.error.topic': 'siembol.indexing',
    'kafka.producer.properties': {
        'bootstrap.servers': '',
        'client.id': 'siembol.writer',
    },
    'kafka.spout.num.executors': 1,
    'kafka.writer.bolt.num.executors': 1,
    'storm.attributes': {
        'bootstrap.servers': '',
        'first.pool.offset.strategy': 'UNCOMMITTED_LATEST',
        'kafka.spout.properties': {
            'group.id': 'siembol.reader',
            'security.protocol': 'SASL_PLAINTEXT',
            'session.timeout.ms': 30000,
        },
        'storm.config': {
            'max.spout.pending': 5000,
            'num.workers': 1,
            'topology.message.timeout.secs': 50
        },
    },
    'zookeeper.attributes': {
        'zk.base.sleep.ms': 1000,
        'zk.max.retries': 3,
        'zk.path': '/siembol/alerting/rules',
        'zk.url': '',
    },
};

export const mockAdminConfigFiles =
{
  config_version: 1,
  files: [
    {
      content: mockAdminConfigData,
      file_history: [],
      file_name: 'admin_config.json',
    },
  ],
};

export const mockAdminConfig =
{
    configData: mockAdminConfigData,
    fileHistory: [],
    version: 1,
};
