package uk.co.gresearch.siembol.response.model;
/**
 * A data transfer object for representing provided evaluator properties
 *
 * <p>This class is used for representing provided evaluator properties.
 *
 * @author  Marian Novotny
 * @see KafkaWriterProperties
 */
public class ProvidedEvaluatorsProperties {
    private KafkaWriterProperties kafkaWriter;

    public KafkaWriterProperties getKafkaWriter() {
        return kafkaWriter;
    }

    public void setKafkaWriter(KafkaWriterProperties kafkaWriter) {
        this.kafkaWriter = kafkaWriter;
    }
}
