package uk.co.gresearch.siembol.response.stream.ruleservice;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import reactor.core.publisher.Mono;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.common.error.ErrorMessage;
import uk.co.gresearch.siembol.common.error.ErrorType;
import uk.co.gresearch.siembol.response.stream.rest.application.ResponseConfigurationProperties;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.RespondingResultAttributes;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;

import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.UUID;

import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;

public class KafkaStreamRulesService implements RulesService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INIT_START = "Kafka stream service initialisation started";
    private static final String INIT_COMPLETED = "Kafka stream service initialisation completed";
    private final KafkaStreams streams;
    private final RulesProvider rulesProvider;

    public KafkaStreamRulesService(RulesProvider rulesProvider,
                                   ResponseConfigurationProperties properties) {
        this(rulesProvider, properties, new KafkaStreamsFactoryImpl());
    }

    KafkaStreamRulesService(RulesProvider rulesProvider,
                            ResponseConfigurationProperties properties,
                            KafkaStreamsFactory kafkaStreamsFactory) {
        this.rulesProvider = rulesProvider;
        streams = createStreams(kafkaStreamsFactory, properties);
        streams.start();
    }

    private KafkaStreams createStreams(KafkaStreamsFactory kafkaStreamsFactory,
                                       ResponseConfigurationProperties properties) {
        LOG.info(INIT_START);
        StreamsBuilder builder = new StreamsBuilder();
        builder.<String, String>stream(properties.getInputTopic())
                .mapValues(this::processMessage)
                .filter((x, y) -> y.getStatusCode() != OK)
                .mapValues(x -> x.getAttributes().getMessage())
                .to(properties.getErrorTopic());

        Properties configuration = new Properties();
        configuration.putAll(properties.getStreamConfig());
        configuration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        configuration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        Topology topology = builder.build(configuration);

        KafkaStreams ret = kafkaStreamsFactory.createKafkaStreams(topology, configuration);
        LOG.info(INIT_COMPLETED);
        return ret;
    }

    private RespondingResult formatErrorMessage(RespondingResult result, String originalString) {
        LOG.error("error message: {}", result.getAttributes().getMessage());
        ErrorMessage msg = new ErrorMessage();
        msg.setErrorType(ErrorType.RESPONSE_ERROR);
        msg.setMessage(result.getAttributes().getMessage());
        msg.setRawMessage(originalString);
        msg.setRuleName(result.getAttributes().getRuleName());

        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setMessage(msg.toString());
        return new RespondingResult(ERROR, attributes);
    }

    private RespondingResult processMessage(String value) {
        try {
            ResponseAlert alert = ResponseAlert.fromOriginalString(UUID.randomUUID().toString(), value);
            alert.put(SiembolMessageFields.RESPONSE_TIME.toString(), System.currentTimeMillis());
            LOG.info("Processing alert guid {}", alert.getResponseAlertId());
            LOG.debug("alert for processing: {}", value);

            RespondingResult result = rulesProvider.getEngine().evaluate(alert);
            LOG.info("Processing finished, status code: {}", result.getStatusCode());
            if (result.getStatusCode() != OK
                    || result.getAttributes().getResult() == ResponseEvaluationResult.NO_MATCH) {
                return formatErrorMessage(result, value);
            }
            LOG.debug("Computed alert: {}", result.getAttributes().getAlert().toString());
            return result;
        } catch (Exception e) {
            return formatErrorMessage(RespondingResult.fromException(e), value);
        }
    }

    @Override
    public RespondingResult getRulesMetadata() {
        return rulesProvider.getEngine().getRulesMetadata();
    }

    @Override
    public Mono<Health> checkHealth() {
        return Mono.just(streams.state().isRunningOrRebalancing() || streams.state().equals(KafkaStreams.State.CREATED)
                ? Health.up().build() :
                Health.down().build());
    }

    @Override
    public void close() {
        streams.close();
    }
}
