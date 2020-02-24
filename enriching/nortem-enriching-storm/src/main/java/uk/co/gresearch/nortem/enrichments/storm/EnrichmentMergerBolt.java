package uk.co.gresearch.nortem.enrichments.storm;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.common.error.ErrorMessage;
import uk.co.gresearch.nortem.common.error.ErrorType;
import uk.co.gresearch.nortem.common.constants.NortemMessageFields;
import uk.co.gresearch.nortem.common.storm.KafkaBatchWriterMessage;
import uk.co.gresearch.nortem.common.storm.KafkaBatchWriterMessages;
import uk.co.gresearch.nortem.enrichments.evaluation.EnrichmentEvaluatorLibrary;
import uk.co.gresearch.nortem.enrichments.storm.common.EnrichmentTuples;
import uk.co.gresearch.nortem.enrichments.storm.common.EnrichmentPairs;
import uk.co.gresearch.nortem.enrichments.storm.common.EnrichmentExceptions;
import uk.co.gresearch.nortem.enrichments.storm.common.StormEnrichmentAttributes;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;

public class EnrichmentMergerBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INVALID_TYPE_IN_TUPLES = "Invalid type in tuple provided";
    private static final String MERGING_ERROR = "Unable to merge the event: {} with the enrichments : {}";
    private static final String EVENT_INFO_LOG = "The event after enrichments: {}";

    private OutputCollector collector;
    private final String outputTopic;
    private final String errorTopic;

    public EnrichmentMergerBolt(StormEnrichmentAttributes attributes) {
        this.outputTopic = attributes.getEnrichingOutputTopic();
        this.errorTopic = attributes.getEnrichingErrorTopic();
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        Object enrichmentsObj = tuple.getValueByField(EnrichmentTuples.ENRICHMENTS.toString());
        if (!(enrichmentsObj instanceof EnrichmentPairs)) {
            LOG.error(INVALID_TYPE_IN_TUPLES);
            throw new IllegalArgumentException(INVALID_TYPE_IN_TUPLES);
        }
        EnrichmentPairs enrichments = (EnrichmentPairs)enrichmentsObj;

        String event = tuple.getStringByField(EnrichmentTuples.EVENT.toString());

        Object exceptionsObj = tuple.getValueByField(EnrichmentTuples.EXCEPTIONS.toString());
        if (!(exceptionsObj instanceof EnrichmentExceptions)) {
            LOG.error(INVALID_TYPE_IN_TUPLES);
            throw new IllegalArgumentException(INVALID_TYPE_IN_TUPLES);
        }
        EnrichmentExceptions exceptions = (EnrichmentExceptions)exceptionsObj;

        try {
            event = EnrichmentEvaluatorLibrary.mergeEnrichments(event,
                    enrichments,
                    Optional.of(NortemMessageFields.ENRICHING_TIME.toString()));
        } catch (Exception e) {
            LOG.error(MERGING_ERROR, event, enrichments.toString());
            exceptions.add(ErrorMessage.createErrorMessage(e, ErrorType.ENRICHMENT_ERROR).toString());
        }

        LOG.debug(EVENT_INFO_LOG, event);
        KafkaBatchWriterMessages messages = new KafkaBatchWriterMessages();
        messages.add(new KafkaBatchWriterMessage(outputTopic, event));
        exceptions.forEach(x -> messages.add(new KafkaBatchWriterMessage(errorTopic, x)));
        collector.emit(tuple, new Values(messages));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(EnrichmentTuples.KAFKA_MESSAGES.toString()));
    }
}
