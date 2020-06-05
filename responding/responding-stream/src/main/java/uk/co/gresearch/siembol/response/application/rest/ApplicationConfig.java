package uk.co.gresearch.siembol.response.application.rest;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import uk.co.gresearch.siembol.response.application.ruleservice.*;
import uk.co.gresearch.siembol.response.common.ProvidedEvaluators;
import uk.co.gresearch.siembol.response.common.RespondingEvaluatorFactory;
import uk.co.gresearch.siembol.response.common.ResponsePlugin;
import uk.co.gresearch.siembol.response.compiler.RespondingCompiler;
import uk.co.gresearch.siembol.response.compiler.RespondingCompilerImpl;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnablePluginRegistries(ResponsePlugin.class)
public class ApplicationConfig implements DisposableBean {
    @Autowired
    private RespondingConfigProperties properties;
    @Autowired
    private RespondingMetricFactory counterFactory;
    @Autowired
    @Qualifier("responsePluginRegistry")
    private OrderAwarePluginRegistry<ResponsePlugin, String> pluginRegistry;

    private RespondingCompiler respondingCompiler;
    private RulesService streamService;
    private RulesProvider rulesProvider;

    @Bean
    RespondingCompiler respondingCompiler() throws Exception {
        List<RespondingEvaluatorFactory> evaluatorFactories = new ArrayList<>();
        evaluatorFactories.addAll(ProvidedEvaluators.getRespondingEvaluatorFactories()
                .getAttributes()
                .getRespondingEvaluatorFactories());

        List<ResponsePlugin> plugins = pluginRegistry.getPlugins();
        if (plugins != null) {
            plugins.forEach(x -> evaluatorFactories.addAll(
                    x.getRespondingEvaluatorFactories().getAttributes().getRespondingEvaluatorFactories()));
        }

        return new RespondingCompilerImpl.Builder()
                .addRespondingEvaluatorFactories(evaluatorFactories)
                .metricFactory(counterFactory)
                .build();
    }

    @Bean
    RulesService centrifugeService() throws Exception {
        respondingCompiler = respondingCompiler();
        rulesProvider = rulesProvider();
        streamService = properties.getInactiveStreamService()
                ? new InactiveRulesService()
                : new KafkaStreamRulesService(rulesProvider, properties);
        return streamService;
    }

    @Bean
    RulesProvider rulesProvider() throws Exception {
        rulesProvider = properties.getInactiveStreamService()
                ? () -> null :
                new ZookeeperRulesProvider(properties.getZookeperAttributes(), respondingCompiler);

        return rulesProvider;
    }

    @Override
    public void destroy() {
        if (streamService == null) {
            return;
        }

        streamService.close();
    }
}
