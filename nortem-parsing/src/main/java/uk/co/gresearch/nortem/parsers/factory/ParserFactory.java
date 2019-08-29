package uk.co.gresearch.nortem.parsers.factory;

public interface ParserFactory {

    ParserFactoryResult getSchema();

    ParserFactoryResult create(String parserConfig);

    ParserFactoryResult test(String parserConfig, byte[] rawLog);

    ParserFactoryResult validateConfiguration(String parserConfig);

    ParserFactoryResult validateConfigurations(String parserConfigurations);
}
