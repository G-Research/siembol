package uk.co.gresearch.siembol.configeditor.rest.application;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.util.ResourceUtils;
import uk.co.gresearch.siembol.common.testing.TestingZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;
import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.common.ServiceType;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;
import uk.co.gresearch.siembol.configeditor.sync.service.StormApplicationProvider;
import uk.co.gresearch.siembol.configeditor.sync.service.StormApplicationProviderImpl;
import uk.co.gresearch.siembol.configeditor.sync.service.SynchronisationService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.model.ConfigStoreProperties;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorConfigurationProperties;
import uk.co.gresearch.siembol.configeditor.rest.common.ConfigEditorHelper;
import uk.co.gresearch.siembol.configeditor.rest.common.ServiceConfigurationProperties;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigEditorServiceFactory;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregatorImpl;
import uk.co.gresearch.siembol.configeditor.sync.service.SynchronisationServiceImpl;
import uk.co.gresearch.siembol.configeditor.testcase.TestCaseEvaluator;
import uk.co.gresearch.siembol.configeditor.testcase.TestCaseEvaluatorImpl;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(ConfigEditorConfigurationProperties.class)
public class ConfigEditorConfiguration implements DisposableBean {

    @Autowired
    private ConfigEditorConfigurationProperties properties;

    @Autowired
    private AuthorisationProvider authProvider;

    private ServiceAggregator serviceAggregator;

    @Bean
    ServiceAggregator serviceAggregator() throws Exception {
        Map<String, ConfigStoreProperties> configStorePropertiesMap = ConfigEditorHelper
                .getConfigStoreProperties(this.properties);

        ServiceAggregatorImpl.Builder builder = new ServiceAggregatorImpl.Builder(authProvider);
        for (String name : properties.getServices().keySet()) {
            ServiceConfigurationProperties serviceProperties = properties.getServices().get(name);

            ServiceType serviceType = ServiceType.fromName(serviceProperties.getType());
            ConfigEditorServiceFactory serviceFactory = ConfigEditorServiceFactory.fromServiceType(serviceType);

            ConfigEditorUiLayout uiLayout = ConfigEditorUtils.readUiLayoutFile(serviceProperties.getUiConfigFileName());
            Optional<Map<String, String>> attributes = Optional.ofNullable(serviceProperties.getAttributes());
            ConfigSchemaService schemaService = serviceFactory.createConfigSchemaService(uiLayout, attributes);

            builder.addService(name,
                    serviceType,
                    configStorePropertiesMap.get(name),
                    serviceFactory.getConfigInfoProvider() ,
                    schemaService);
        }
        serviceAggregator = builder.build();
        return serviceAggregator;
    }

    @Bean
    TestCaseEvaluator testCaseEvaluator() throws Exception {
        ConfigEditorUiLayout uiLayout = ConfigEditorUtils.readUiLayoutFile(properties.getTestCasesUiConfigFileName());
        return new TestCaseEvaluatorImpl(uiLayout);
    }

    @Bean
    @ConditionalOnProperty(prefix = "config-editor", value = "synchronisation")
    StormApplicationProvider stormApplicationProvider() throws Exception {
        return StormApplicationProviderImpl.create(zooKeeperConnectorFactory(), properties.getStormTopologiesZookeeper());
    }

    @Bean
    @ConditionalOnProperty(prefix = "config-editor", value = "synchronisation")
    SynchronisationService synchronisationService() throws Exception {
        serviceAggregator = serviceAggregator();
        ZooKeeperConnectorFactory zooKeeperConnectorFactory = zooKeeperConnectorFactory();
        StormApplicationProvider stormApplicationProvider = stormApplicationProvider();
        List<ConfigServiceHelper> aggregatorServices = serviceAggregator
                .getAggregatorServices()
                .stream()
                .map(x -> new ConfigServiceHelperImpl(x, properties, zooKeeperConnectorFactory))
                .collect(Collectors.toList());

        SynchronisationService ret = new SynchronisationServiceImpl.Builder(stormApplicationProvider)
                .addConfigServiceHelpers(aggregatorServices)
                .build();

        ret.synchroniseAllServices(properties.getSynchronisation());
        return ret;
    }

    @Bean
    @ConditionalOnProperty(prefix = "config-editor", value = "synchronisation")
    ZooKeeperConnectorFactory zooKeeperConnectorFactory() throws Exception {
        if (properties.getTestingZookeeperFiles() == null) {
            return new ZooKeeperConnectorFactoryImpl();
        }

        TestingZooKeeperConnectorFactory ret = new TestingZooKeeperConnectorFactory();
        for (Map.Entry<String, String> entry: properties.getTestingZookeeperFiles().entrySet()){
            File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + entry.getValue());
            String content = new String(Files.readAllBytes(file.toPath()));
            ret.setData(entry.getKey(), content);
        }

        return ret;
    }

    @Override
    public void destroy() {
        if (serviceAggregator != null) {
            serviceAggregator.shutDown();
        }

        if (serviceAggregator != null) {
            serviceAggregator.awaitShutDown();
        }
    }
}
