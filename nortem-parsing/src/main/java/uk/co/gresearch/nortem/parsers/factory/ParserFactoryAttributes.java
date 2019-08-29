package uk.co.gresearch.nortem.parsers.factory;

import uk.co.gresearch.nortem.parsers.common.NortemParser;
import uk.co.gresearch.nortem.parsers.common.ParserResult;
import uk.co.gresearch.nortem.parsers.extractors.ParserExtractor;
import uk.co.gresearch.nortem.parsers.transformations.Transformation;

import java.util.List;

public class ParserFactoryAttributes {
    private List<ParserExtractor> extractors;
    private List<Transformation> transformations;
    private NortemParser nortemParser;
    private String message;
    private ParserResult parserResult;
    private String jsonSchema;

    public List<ParserExtractor> getExtractors() {
        return extractors;
    }

    public void setExtractors(List<ParserExtractor> extractors) {
        this.extractors = extractors;
    }

    public List<Transformation> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<Transformation> transformations) {
        this.transformations = transformations;
    }

    public NortemParser getNortemParser() {
        return nortemParser;
    }

    public void setNortemParser(NortemParser nortemParser) {
        this.nortemParser = nortemParser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ParserResult getParserResult() {
        return parserResult;
    }

    public void setParserResult(ParserResult parserResult) {
        this.parserResult = parserResult;
    }

    public String getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
    }
}
