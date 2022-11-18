package uk.co.gresearch.siembol.parsers.application.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;
import java.lang.invoke.MethodHandles;

/**
 * An object for parsing application that integrates a single parser
 *
 * <p>This derived class of ParsingApplicationParser is using template pattern for implementing
 * a parsing application that integrates a single parser.
 *
 * @author  Marian Novotny
 */
public class SingleApplicationParser extends ParsingApplicationParser {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    static final String MISSING_ARGUMENTS = "Missing arguments in single application parser";

    private final SiembolParserWrapper parser;

    protected SingleApplicationParser(Builder<?> builder) {
        super(builder);
        this.parser = builder.parser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ParserResult parseInternally(String source, String metadata, byte[] message) {
        return parser.parseToResult(metadata, message);
    }

    /**
     * Creates SingleApplicationParser builder instance
     *
     * @return SingleApplicationParser builder
     */
    public static Builder<SingleApplicationParser> builder() {
        return new Builder<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public SingleApplicationParser build() {
                if (parser == null) {
                    LOG.error(MISSING_ARGUMENTS);
                    throw new IllegalArgumentException(MISSING_ARGUMENTS);
                }

                return new SingleApplicationParser(this);
            }
        };
    }

    /**
     * A builder for SingleApplicationParser parser
     *
     * <p>This class is using Builder pattern.
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends SingleApplicationParser> extends
            ParsingApplicationParser.Builder<T> {
        private static final long serialVersionUID = 1L;
        protected SiembolParserWrapper parser;

        /**
         * Sets the parser
         * @param topic an output topic for parsing
         * @param siembolParser a serializable siembol parser
         *
         * @return this builder
         * @see SerializableSiembolParser
         */
        public Builder<T> parser(String topic, SerializableSiembolParser siembolParser) throws Exception {
            parser = new SiembolParserWrapper(siembolParser, topic);
            return this;
        }

        public abstract T build();
    }
}