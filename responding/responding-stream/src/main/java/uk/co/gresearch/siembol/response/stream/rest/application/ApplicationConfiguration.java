package uk.co.gresearch.siembol.response.stream.rest.application;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import uk.co.gresearch.siembol.common.metrics.SiembolMetricsRegistrar;
import uk.co.gresearch.siembol.common.metrics.spring.SpringMetricsRegistrar;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.ResponsePlugin;
import uk.co.gresearch.siembol.response.compiler.RespondingCompiler;
import uk.co.gresearch.siembol.response.compiler.RespondingCompilerImpl;

import uk.co.gresearch.siembol.response.stream.ruleservice.*;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnablePluginRegistries(ResponsePlugin.class)
public class ApplicationConfiguration implements DisposableBean {
    @Autowired
    private ResponseConfigurationProperties properties;
    @Autowired
    private MeterRegistry springMeterRegistrar;

    @Autowired
    @Qualifier("responsePluginRegistry")
    private OrderAwarePluginRegistry<ResponsePlugin, String> pluginRegistry;

    private RulesService streamService;

    @Bean("metricsRegistrar")
    SiembolMetricsRegistrar metricsRegistrar() {
        return new SpringMetricsRegistrar(springMeterRegistrar);
    }

    @Bean("respondingCompiler")
    @DependsOn("metricsRegistrar")
    RespondingCompiler respondingCompiler(@Autowired SiembolMetricsRegistrar metricsRegistrar) throws Exception {
        List<RespondingEvaluatorFactory> evaluatorFactories = new ArrayList<>();
        evaluatorFactories.addAll(ProvidedEvaluators.getRespondingEvaluatorFactories(properties.getEvaluatorsProperties())
                .getAttributes()
                .getRespondingEvaluatorFactories());

        List<ResponsePlugin> plugins = pluginRegistry.getPlugins();
        if (plugins != null) {
            plugins.forEach(x -> evaluatorFactories.addAll(
                    x.getRespondingEvaluatorFactories().getAttributes().getRespondingEvaluatorFactories()));
        }

        return new RespondingCompilerImpl.Builder()
                .addRespondingEvaluatorFactories(evaluatorFactories)
                .metricsRegistrar(metricsRegistrar)
                .build();
    }

    @Bean
    @DependsOn({"rulesProvider", "respondingCompiler"})
    RulesService centrifugeService(@Autowired RespondingCompiler respondingCompiler,
                                   @Autowired RulesProvider rulesProvider) {
        streamService = properties.getInactiveStreamService()
                ? new InactiveRulesService()
                : new KafkaStreamRulesService(rulesProvider, properties);
        return streamService;
    }

    @Bean("rulesProvider")
    @DependsOn({"respondingCompiler", "metricsRegistrar"})
    RulesProvider rulesProvider(
            @Autowired RespondingCompiler respondingCompiler,
            @Autowired SiembolMetricsRegistrar metricsRegistrar) throws Exception {
        return properties.getInactiveStreamService()
                ? () -> null :
                new ZooKeeperRulesProvider(properties.getZookeperAttributes(), respondingCompiler, metricsRegistrar);
    }

    @Override
    public void destroy() {
        if (streamService == null) {
            return;
        }

        streamService.close();
    }
}
