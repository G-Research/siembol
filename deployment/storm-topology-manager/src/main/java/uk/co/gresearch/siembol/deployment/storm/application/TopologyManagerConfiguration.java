package uk.co.gresearch.siembol.deployment.storm.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnector;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactory;
import uk.co.gresearch.siembol.common.zookeeper.ZooKeeperConnectorFactoryImpl;
import uk.co.gresearch.siembol.deployment.storm.providers.KubernetesProvider;
import uk.co.gresearch.siembol.deployment.storm.providers.KubernetesProviderImpl;
import uk.co.gresearch.siembol.deployment.storm.providers.StormProvider;
import uk.co.gresearch.siembol.deployment.storm.providers.StormProviderImpl;
import uk.co.gresearch.siembol.deployment.storm.service.TopologyManagerService;
import uk.co.gresearch.siembol.deployment.storm.service.TopologyManagerServiceImpl;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(ServiceConfigurationProperties.class)
class TopologyManagerConfiguration {
    private static final String KERBEROS = "kerberos";

    @Autowired
    private ServiceConfigurationProperties properties;

    @Bean
    KubernetesProvider kubernetesProvider() throws IOException {
        return new KubernetesProviderImpl(properties.getK8s());
    }

    @Bean
    StormProvider stormProvider() {
        HttpProvider httpProvider = KERBEROS.equals(properties.getStorm().getAuthenticationType()) ?
                new HttpProvider(properties.getStorm().getUrl(), HttpProvider::getKerberosHttpClient) :
                new HttpProvider(properties.getStorm().getUrl(), HttpProvider::getHttpClient);

        return new StormProviderImpl(httpProvider, properties.getStorm().getKillWaitSeconds());
    }

    @Bean
    ZooKeeperConnector desiredStateZkConnector() throws Exception {
        ZooKeeperConnectorFactory factory = new ZooKeeperConnectorFactoryImpl();
        return factory.createZookeeperConnector(properties.getDesiredState());
    }

    @Bean
    ZooKeeperConnector savedStateZkConnector() throws Exception {
        ZooKeeperConnectorFactory factory = new ZooKeeperConnectorFactoryImpl();
        return factory.createZookeeperConnector(properties.getSavedState());
    }

    @Bean
    TopologyManagerService synchroniseService() throws Exception {
        TopologyManagerServiceImpl topologyManagerService = new TopologyManagerServiceImpl(stormProvider(),
                kubernetesProvider(),
                desiredStateZkConnector(),
                savedStateZkConnector(),
                properties.getScheduleAtFixedRateSeconds());
        topologyManagerService.invokeSynchronise();
        return topologyManagerService;
    }
}
