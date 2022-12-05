package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * A data transfer object for representing kafka writer evaluator attributes
 *
 * <p>This class is used for json (de)serialisation of kafka writer evaluator attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 */
@Attributes(title = "kafka writer evaluator attributes", description = "Attributes for kafka writer evaluator")
public class KafkaWriterEvaluatorAttributesDto {
    @JsonProperty("topic_name")
    @Attributes(required = true, description = "The name of the kafka topic")
    private String topicName;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
