package uk.co.gresearch.siembol.configeditor.rest;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.co.gresearch.siembol.configeditor.common.AuthorisationProvider;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigSchemaService;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigStore;
import uk.co.gresearch.siembol.configeditor.service.common.ConfigEditorServiceType;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregator;
import uk.co.gresearch.siembol.configeditor.serviceaggregator.ServiceAggregatorImpl;
import uk.co.gresearch.siembol.configeditor.testcase.TestCaseEvaluator;
import uk.co.gresearch.siembol.configeditor.testcase.TestCaseEvaluatorImpl;

import java.util.Map;
import java.util.Optional;

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
        ServiceAggregatorImpl.Builder builder = new ServiceAggregatorImpl.Builder(authProvider);
        for (String name : properties.getServices().keySet()) {
            ServiceConfigurationProperties serviceProperties = properties.getServices().get(name);

            ConfigEditorServiceType serviceType = ConfigEditorServiceType.fromName(serviceProperties.getType());
            ConfigStore configStore = serviceType.createConfigStore(serviceProperties.getConfigStore());

            Optional<String> uiLayout = ConfigEditorUtils.readUiLayoutFile(serviceProperties.getUiConfigFileName());
            Optional<String> testSpecUiLayout = ConfigEditorUtils.readUiLayoutFile(
                    serviceProperties.getTestSpecUiConfigFileName());
            Optional<Map<String, String>> attributes = Optional.ofNullable(serviceProperties.getAttributes());

            ConfigSchemaService schemaService = serviceType.createConfigSchemaService(
                    uiLayout, testSpecUiLayout, attributes);
            builder.addService(name, serviceProperties.getType(), configStore, schemaService);
        }
        serviceAggregator = builder.build();
        return serviceAggregator;
    }

    @Bean
    TestCaseEvaluator testCaseEvaluator() throws Exception {
        Optional<String> uiLayout = ConfigEditorUtils.readUiLayoutFile(properties.getTestCasesUiConfigFileName());
        return new TestCaseEvaluatorImpl(uiLayout);
    }

    @Override
    public void destroy()  {
        if (serviceAggregator != null) {
            serviceAggregator.getConfigStoreServices().forEach(x -> x.shutDown());
        }

        if (serviceAggregator != null) {
            serviceAggregator.getConfigStoreServices().forEach(x -> x.awaitShutDown());
        }
    }
}
