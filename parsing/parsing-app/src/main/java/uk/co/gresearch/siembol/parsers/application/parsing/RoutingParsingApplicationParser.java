package uk.co.gresearch.siembol.parsers.application.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.ParserResult;
import uk.co.gresearch.siembol.parsers.common.SerializableSiembolParser;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RoutingParsingApplicationParser extends ParsingApplicationParser {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_ROUTER_FIELDS = "Missing routing fields: %s, %s, in the parsed message: %s";
    private static final String MISSING_ARGUMENTS = "Missing arguments in routing parsing application";

    private final String routingConditionField;
    private final String routingMessageField;
    private final SiembolParserWrapper routerParser;
    private final ArrayList<String> mergedFields;
    private final ArrayList<SiembolParserWrapper> parsers;

    protected RoutingParsingApplicationParser(Builder<?> builder) {
        super(builder);
        this.routingConditionField = builder.routingConditionField;
        this.routingMessageField = builder.routingMessageField;
        this.mergedFields = builder.mergedFields;
        this.parsers = builder.parsers;
        this.routerParser = builder.routerParser;
    }

    @Override
    protected List<ParserResult> parseInternally(String metadata, byte[] message) {
        List<ParserResult> ret = new ArrayList<>();
        ParserResult routerResult = routerParser.parseToResult(metadata, message);
        if (routerResult.getException() != null) {
            ret.add(routerResult);
            return ret;
        }

        List<Map<String, Object>> routerParsedMessages = routerResult.getParsedMessages();
        for (Map<String, Object> parsedMsg : routerParsedMessages) {
            if (!parsedMsg.containsKey(routingConditionField)
                    || !parsedMsg.containsKey(routingMessageField)) {
                String errorMsg = String.format(MISSING_ROUTER_FIELDS,
                        routingConditionField, routingMessageField, parsedMsg.toString());
                LOG.error(errorMsg);
                routerResult.setException(new IllegalStateException(errorMsg));
                ret.add(routerResult);
                continue; //NOTE: we try the next message that could be valid
            }

            String messageToParse = parsedMsg.get(routingMessageField).toString();
            String messageToCondition = parsedMsg.get(routingConditionField).toString();
            for (SiembolParserWrapper parser : parsers) {
                if (!parser.checkCondition(messageToCondition)) {
                    continue;
                }

                ParserResult result = parser.parseToResult(metadata, messageToParse.getBytes());
                if (result.getParsedMessages() != null && !result.getParsedMessages().isEmpty()) {
                    for (String field : mergedFields) {
                        if (parsedMsg.containsKey(field)) {
                            result.getParsedMessages().forEach(x -> x.put(field, parsedMsg.get(field)));
                        }
                    }
                }
                ret.add(result);
                break;
            }
        }

        return ret;
    }

    public static Builder<RoutingParsingApplicationParser> builder() {
        return new Builder<RoutingParsingApplicationParser>() {
            private static final long serialVersionUID = 1L;
            @Override
            public RoutingParsingApplicationParser build() {
                if (routerParser == null
                        || defaultParser == null
                        || routingConditionField == null
                        || routingMessageField == null) {
                    LOG.error(MISSING_ARGUMENTS);
                    throw new IllegalArgumentException(MISSING_ARGUMENTS);
                }

                parsers.add(defaultParser);
                return new RoutingParsingApplicationParser(this);
            }
        };
    }

    public static abstract class Builder<T extends RoutingParsingApplicationParser> extends
            ParsingApplicationParser.Builder<T> {
        private static final long serialVersionUID = 1L;

        protected String routingConditionField;
        protected String routingMessageField;
        protected ArrayList<String> mergedFields = new ArrayList<>();
        protected SiembolParserWrapper routerParser;
        protected SiembolParserWrapper defaultParser;
        protected ArrayList<SiembolParserWrapper> parsers =  new ArrayList<>();

        public Builder<T> routingConditionField(String routingConditionField) {
            this.routingConditionField = routingConditionField;
            return this;
        }

        public Builder<T> routingMessageField(String routingMessageField) {
            this.routingMessageField = routingMessageField;
            return this;
        }

        public Builder<T> mergedFields(List<String> mergedFields) {
            if (mergedFields != null ) {
                this.mergedFields = new ArrayList<>(mergedFields);
            }
            return this;
        }

        public Builder<T> routerParser(SerializableSiembolParser siembolParser) throws Exception {
            final RouterCondition alwaysMatch = x -> true;
            this.routerParser = new SiembolParserWrapper(alwaysMatch, siembolParser, null);
            return this;
        }

        public Builder<T> defaultParser(String topic, SerializableSiembolParser siembolParser) throws Exception {
            final RouterCondition alwaysMatch = x -> true;
            defaultParser = new SiembolParserWrapper(alwaysMatch, siembolParser, topic);
            return this;
        }

        public Builder<T> addParser(String topic, SerializableSiembolParser siembolParser, String pattern) throws Exception {
            final Pattern conditionPattern = Pattern.compile(pattern, Pattern.DOTALL);
            final RouterCondition condition = x -> conditionPattern.matcher(x).matches();
            parsers.add(new SiembolParserWrapper(condition, siembolParser, topic));
            return this;
        }

        public abstract T build();
    }
}
