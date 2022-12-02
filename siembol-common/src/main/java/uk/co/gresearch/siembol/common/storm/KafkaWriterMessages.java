package uk.co.gresearch.siembol.common.storm;

import java.util.ArrayList;
/**
 * A serializable object for representing a list of Kafka writer messages
 *
 * <p>This class implements serializable interface and is used for representing a list of Kafka writer messages
 *
 * @author Marian Novotny
 * @see KafkaWriterMessage
 */
public class KafkaWriterMessages extends ArrayList<KafkaWriterMessage> {
    private static final long serialVersionUID = 1L;
}
