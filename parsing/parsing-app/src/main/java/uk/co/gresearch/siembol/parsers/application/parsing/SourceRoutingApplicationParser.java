package uk.co.gresearch.siembol.parsers.application.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
/**
 * An object for parsing application that integrates a routing parsing application using information about the source
 *
 * <p>This derived class of ParsingApplicationParser is using template pattern for implementing
 * a parsing application that integrates a routing parsing application using information about the source.
 * The final parser will be selected based on a map of a source to a parser.
 * Default parser will be selected if the map does not contain the source.
 *
 * @author  Marian Novotny
 */
public class SourceRoutingApplicationParser extends ParsingApplicationParser {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    static final String MISSING_ARGUMENTS = "Missing arguments in source routing application parser";

    private final HashMap<String, SiembolParserWrapper> sourceToParserMap;
    private final SiembolParserWrapper defaultParser;

    protected SourceRoutingApplicationParser(Builder<?> builder) {
        super(builder);
        this.sourceToParserMap = builder.sourceToParserMap;
        this.defaultParser = builder.defaultParser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ParserResult parseInternally(String source, String metadata, byte[] message) {
        var parser = sourceToParserMap.getOrDefault(source, defaultParser);
        return parser.parseToResult(metadata, message);
    }

    /**
     * Creates SourceRoutingApplicationParser builder instance
     *
     * @return SourceRoutingApplicationParser builder
     */
    public static Builder<SourceRoutingApplicationParser> builder() {
        return new Builder<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public SourceRoutingApplicationParser build() {
                if (defaultParser == null || sourceToParserMap == null) {
                    LOG.error(MISSING_ARGUMENTS);
                    throw new IllegalArgumentException(MISSING_ARGUMENTS);
                }

                return new SourceRoutingApplicationParser(this);
            }
        };
    }

    /**
     * A builder for source routing application parser
     *
     * <p>This class is using Builder pattern.
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends SourceRoutingApplicationParser> extends
            ParsingApplicationParser.Builder<T> {
        private static final long serialVersionUID = 1L;
        static final String DUPLICATE_SOURCE_MSG = "Parser with source %s already exists";

        protected HashMap<String, SiembolParserWrapper> sourceToParserMap = new HashMap<>();
        protected SiembolParserWrapper defaultParser;

        /**
         * Sets the default parser
         * @param topic an output topic for parsing
         * @param siembolParser a serializable siembol parser
         *
         * @return this builder
         * @see SerializableSiembolParser
         */
        public Builder<T> defaultParser(String topic, SerializableSiembolParser siembolParser) {
            defaultParser = new SiembolParserWrapper(siembolParser, topic);
            return this;
        }

        /**
         * Adds the parser with its source
         * @param source a source identification for selecting the parser
         * @param topic an output topic for parsing
         * @param siembolParser a serializable siembol parser
         *
         * @return this builder
         * @throws IllegalArgumentException if the source is not unique and has been already used
         * @see SerializableSiembolParser
         *
         */
        public Builder<T> addParser(String source, String topic, SerializableSiembolParser siembolParser) {
            if (sourceToParserMap.containsKey(source)) {
                throw new IllegalArgumentException(String.format(DUPLICATE_SOURCE_MSG, source));
            }

            var parser = new SiembolParserWrapper(siembolParser, topic);
            sourceToParserMap.put(source, parser);
            return this;
        }

        public abstract T build();
    }
}
