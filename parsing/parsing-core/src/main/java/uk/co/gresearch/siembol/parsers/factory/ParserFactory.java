package uk.co.gresearch.siembol.parsers.factory;

public interface ParserFactory {

    ParserFactoryResult getSchema();

    ParserFactoryResult create(String parserConfig);

    ParserFactoryResult test(String parserConfig, String metadata, byte[] rawLog);

    ParserFactoryResult validateConfiguration(String parserConfig);

    ParserFactoryResult validateConfigurations(String parserConfigurations);
}
