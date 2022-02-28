package uk.co.gresearch.siembol.response.model;

public class ProvidedEvaluatorsProperties {
    private KafkaWriterProperties kafkaWriter;

    public KafkaWriterProperties getKafkaWriter() {
        return kafkaWriter;
    }

    public void setKafkaWriter(KafkaWriterProperties kafkaWriter) {
        this.kafkaWriter = kafkaWriter;
    }
}
