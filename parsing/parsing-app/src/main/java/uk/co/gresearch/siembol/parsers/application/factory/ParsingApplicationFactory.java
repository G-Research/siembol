package uk.co.gresearch.siembol.parsers.application.factory;
/**
 * An object for compiling parsing applications
 *
 * <p>This interface is for creating a parsing application, validating a parsing application configuration and
 * providing json schema for parsing application configurations.
 *
 * @author  Marian Novotny
 * @see ParsingApplicationFactoryResult
 *
 */
public interface ParsingApplicationFactory {

    /**
     * Gets json schema of parsing application configurations
     *
     * @return parsing application factory result with json schema of parsing application configurations
     * @see ParsingApplicationFactoryResult
     */
    ParsingApplicationFactoryResult getSchema();

    /**
     * Creates a parsing application from a parsing application configuration json string and parser configurations
     *
     * @param parsingApplicationConfig a parsing application configuration json string 
     * @param parserConfigs parser configurations json string
     * @return parsing application factory result with generated parsing application on success,
     *         otherwise parser result with an error status code.
     * @see ParsingApplicationFactoryResult
     */
    ParsingApplicationFactoryResult create(String parsingApplicationConfig, String parserConfigs);

    /**
     * Creates a parsing application from a parser configuration json string.
     * The created application is not initialised with parsers.
     *
     * @param parsingApplicationConfig a parser configuration json string
     * @return parsing application factory result with generated parsing application on success,
     *         otherwise parsing application factory result with an error status code.
     * @see ParsingApplicationFactoryResult
     */
    ParsingApplicationFactoryResult create(String parsingApplicationConfig);

    /**
     * Validates a parsing application configuration json string
     *
     * @param parsingApplicationConfig a parsing application configuration json string
     * @return parsing application factory result with OK status code on success,
     *         otherwise parsing application factory result with an error status code.
     * @see ParsingApplicationFactoryResult
     */
    ParsingApplicationFactoryResult validateConfiguration(String parsingApplicationConfig);

    /**
     * Validates parsing application configurations json string
     *
     * @param parsingApplicationConfigurations parsing application configurations json string
     * @return parsing application factory result with OK status code on success,
     *         otherwise parsing application factory result with an error status code.
     * @see ParsingApplicationFactoryResult
     */
    ParsingApplicationFactoryResult validateConfigurations(String parsingApplicationConfigurations);
}
