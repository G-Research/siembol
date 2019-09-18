package uk.co.gresearch.nortem.parsers.application.factory;

public interface ParsingApplicationFactory {

    ParsingApplicationFactoryResult getSchema();

    ParsingApplicationFactoryResult create(String parserApplicationConfig, String parserConfigs);

    ParsingApplicationFactoryResult validateConfiguration(String parserConfig);

    ParsingApplicationFactoryResult validateConfigurations(String parserConfigurations);

}
