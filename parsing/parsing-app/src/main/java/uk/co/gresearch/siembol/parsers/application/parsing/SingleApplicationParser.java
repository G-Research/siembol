package uk.co.gresearch.siembol.parsers.application.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected ParserResult parseInternally(String source, String metadata, byte[] message) {
        return parser.parseToResult(metadata, message);
    }

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

    public static abstract class Builder<T extends SingleApplicationParser> extends
            ParsingApplicationParser.Builder<T> {
        private static final long serialVersionUID = 1L;
        protected SiembolParserWrapper parser;

        public Builder<T> parser(String topic, SerializableSiembolParser siembolParser) throws Exception {
            parser = new SiembolParserWrapper(siembolParser, topic);
            return this;
        }

        public abstract T build();
    }
}