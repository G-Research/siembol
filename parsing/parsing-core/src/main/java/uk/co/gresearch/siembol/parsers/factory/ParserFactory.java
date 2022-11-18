package uk.co.gresearch.siembol.parsers.factory;
/**
 * An object for compiling parsers
 *
 * <p>This interface is for creating a parser, testing a parser on an input, validating a parser configuration and
 * providing a json schema for parser configurations.
 *
 * @author  Marian Novotny
 * @see ParserFactoryResult
 *
 */
public interface ParserFactory {

    /**
     * Gets the json schema of parser configurations
     *
     * @return parser factory result with the json schema of parser configurations
     * @see ParserFactoryResult
     */
    ParserFactoryResult getSchema();

    /**
     * Creates a parser from a parser configuration json string
     *
     * @param parserConfig a parser configuration json string
     * @return parser factory result with generated parser on success,
     *         otherwise parser result with an error status code.
     * @see ParserFactoryResult
     */
    ParserFactoryResult create(String parserConfig);

    /**
     * Tests a parser configuration json string on an input log message
     *
     * @param parserConfig a parser configuration json string
     * @param metadata json string with log metadata
     * @param rawLog raw bytes of the log message for testing
     * @return parser factory result with parsed message on success,
     *         otherwise parser result with an error status code.
     * @see ParserFactoryResult
     */
    ParserFactoryResult test(String parserConfig, String metadata, byte[] rawLog);

    /**
     * Validates a parser configuration json string
     *
     * @param parserConfig a parser configuration json string
     * @return parser factory result with OK status code on success,
     *         otherwise parser result with an error status code.
     * @see ParserFactoryResult
     */
    ParserFactoryResult validateConfiguration(String parserConfig);

    /**
     * Validates parser configurations json string
     *
     * @param parserConfigurations parser configurations json string
     * @return parser factory result with OK status code on success,
     *         otherwise parser result with an error status code.
     * @see ParserFactoryResult
     */
    ParserFactoryResult validateConfigurations(String parserConfigurations);
}
