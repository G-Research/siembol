package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

@Attributes(title = "kafka writer evaluator attributes", description = "Attributes for sleep evaluator")
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
