package uk.co.gresearch.siembol.parsers.application.factory;

import uk.co.gresearch.siembol.parsers.application.model.ParsingApplicationTypeDto;
import uk.co.gresearch.siembol.parsers.application.parsing.ParsingApplicationParser;

import java.util.List;
/**
 * An object for representing a parsing application factory attributes
 *
 * <p>This class represents parsing application factory attributed used by a parsing application factory result.
 *
 * @author Marian Novotny
 *
 */
public class ParsingApplicationFactoryAttributes {
    private String jsonSchema;
    private String name;
    private String applicationParserSpecification;
    private Integer numWorkers;
    private Integer inputParallelism;
    private Integer outputParallelism;
    private Integer parsingParallelism;
    private List<String> inputTopics;
    private ParsingApplicationParser applicationParser;
    private String message;
    private String sourceHeaderName;
    private ParsingApplicationTypeDto applicationType;

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

    public String getApplicationParserSpecification() {
        return applicationParserSpecification;
    }

    public void setApplicationParserSpecification(String applicationParserSpecification) {
        this.applicationParserSpecification = applicationParserSpecification;
    }

    public String getSourceHeaderName() {
        return sourceHeaderName;
    }

    public void setSourceHeaderName(String sourceHeaderName) {
        this.sourceHeaderName = sourceHeaderName;
    }

    public ParsingApplicationTypeDto getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ParsingApplicationTypeDto applicationType) {
        this.applicationType = applicationType;
    }

    public Integer getNumWorkers() {
        return numWorkers;
    }

    public void setNumWorkers(Integer numWorkers) {
        this.numWorkers = numWorkers;
    }
}
