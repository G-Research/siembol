package uk.co.gresearch.siembol.common.storm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class KafkaBatchWriterBoltTest {
    private static final ObjectReader JSON_PARSERS_CONFIG_READER = new ObjectMapper()
            .readerFor(KafkaBatchWriterAttributes.class);
    private static final ObjectReader JSON_MAP_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {});

    /**
     * {
     *   "kafka.batch.writer.attributes": {
     *     "batch.size": 1,
     *     "producer.properties": {
     *       "client.id": "writer",
     *       "compression.type": "snappy",
     *       "security.protocol": "PLAINTEXT"
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String batchWriterConfig;

    @ClassRule
    public static KafkaJunitRule kafkaRule = new KafkaJunitRule(EphemeralKafkaBroker.create());

    private KafkaBatchWriterAttributes attributes;
    private KafkaBatchWriterBolt writerBolt;
    private String bootstrapServer;
    private Tuple tuple;
    private OutputCollector collector;
    private KafkaBatchWriterMessages messages;
    private String fieldName = "field";

    @Before
    public void setUp() throws Exception {
        attributes = JSON_PARSERS_CONFIG_READER
                .readValue(batchWriterConfig);

        bootstrapServer = String.format("127.0.0.1:%d", kafkaRule.helper().kafkaPort());
        attributes.getProducerProperties().put("bootstrap.servers", bootstrapServer);

        collector = Mockito.mock(OutputCollector.class);
        messages = new KafkaBatchWriterMessages();

        tuple = Mockito.mock(Tuple.class);
        when(tuple.getValueByField(eq(fieldName))).thenReturn(messages);

        kafkaRule.waitForStartup();
        writerBolt = new KafkaBatchWriterBolt(attributes, fieldName);
        writerBolt.prepare(null, null, collector);
    }

    @Test
    public void testLargeBatch() throws Exception {
        KafkaBatchWriterMessage message = new KafkaBatchWriterMessage("output", "dummy");
        messages.add(message);
        messages.add(message);
        for (int i = 0; i < 50; i++) {
            writerBolt.execute(tuple);
        }

        List<String> outputMessages = kafkaRule.helper().consumeStrings("output", 100)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputMessages);
        Assert.assertEquals(100, outputMessages.size());
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals("dummy", outputMessages.get(i));
        }
        verify(collector, times(1)).ack(any());
    }

    @Test
    public void testOneSizeBatch() throws Exception {
        attributes.setBatchSize(1);
        writerBolt = new KafkaBatchWriterBolt(attributes, fieldName);
        writerBolt.prepare(null, null, collector);

        KafkaBatchWriterMessage message = new KafkaBatchWriterMessage("output", "dummy");
        messages.add(message);
        for (int i = 0; i < 100; i++) {
            writerBolt.execute(tuple);
        }

        List<String> outputMessages = kafkaRule.helper().consumeStrings("output", 100)
                .get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(outputMessages);
        Assert.assertEquals(100, outputMessages.size());
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals("dummy", outputMessages.get(i));
        }
        verify(collector, times(100)).ack(any());
    }
}
