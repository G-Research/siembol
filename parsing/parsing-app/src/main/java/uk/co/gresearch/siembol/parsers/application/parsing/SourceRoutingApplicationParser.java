package uk.co.gresearch.siembol.parsers.application.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Override
    protected List<ParserResult> parseInternally(String source, String metadata, byte[] message) {
        List<ParserResult> ret = new ArrayList<>();
        SiembolParserWrapper parser = sourceToParserMap.getOrDefault(source, defaultParser);
        ret.add(parser.parseToResult(metadata, message));
        return ret;
    }

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

    public static abstract class Builder<T extends SourceRoutingApplicationParser> extends
            ParsingApplicationParser.Builder<T> {
        private static final long serialVersionUID = 1L;
        static final String DUPLICATE_SOURCE_MSG = "Parser with source %s already exists";

        protected HashMap<String, SiembolParserWrapper> sourceToParserMap = new HashMap<>();
        protected SiembolParserWrapper defaultParser;

        public Builder<T> defaultParser(String topic, SerializableSiembolParser siembolParser) {
            defaultParser = new SiembolParserWrapper(siembolParser, topic);
            return this;
        }

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
