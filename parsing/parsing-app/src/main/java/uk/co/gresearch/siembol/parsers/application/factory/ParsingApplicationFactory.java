package uk.co.gresearch.siembol.parsers.application.factory;

public interface ParsingApplicationFactory {

    ParsingApplicationFactoryResult getSchema();

    ParsingApplicationFactoryResult create(String parserApplicationConfig, String parserConfigs);

    ParsingApplicationFactoryResult create(String parserApplicationConfig);

    ParsingApplicationFactoryResult validateConfiguration(String parserConfig);

    ParsingApplicationFactoryResult validateConfigurations(String parserConfigurations);

}
