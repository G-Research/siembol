package uk.co.gresearch.nortem.parsers.application.factory;

import uk.co.gresearch.nortem.parsers.application.parsing.ParsingApplicationParser;

import java.util.List;

public class ParsingApplicationFactoryAttributes {
    private String jsonSchema;
    private String name;
    private Integer inputParallelism;
    private Integer outputParallelism;
    private Integer parsingParallelism;
    private List<String> inputTopics;
    private ParsingApplicationParser applicationParser;
    private String message;

    public String getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInputParallelism() {
        return inputParallelism;
    }

    public void setInputParallelism(Integer inputParallelism) {
        this.inputParallelism = inputParallelism;
    }

    public Integer getOutputParallelism() {
        return outputParallelism;
    }

    public void setOutputParallelism(Integer outputParallelism) {
        this.outputParallelism = outputParallelism;
    }

    public Integer getParsingParallelism() {
        return parsingParallelism;
    }

    public void setParsingParallelism(Integer parsingParallelism) {
        this.parsingParallelism = parsingParallelism;
    }

    public List<String> getInputTopics() {
        return inputTopics;
    }

    public void setInputTopics(List<String> inputTopics) {
        this.inputTopics = inputTopics;
    }

    public ParsingApplicationParser getApplicationParser() {
        return applicationParser;
    }

    public void setApplicationParser(ParsingApplicationParser applicationParser) {
        this.applicationParser = applicationParser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
