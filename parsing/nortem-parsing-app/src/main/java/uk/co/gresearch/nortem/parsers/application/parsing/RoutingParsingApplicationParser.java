package uk.co.gresearch.nortem.parsers.application.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.parsers.common.ParserResult;
import uk.co.gresearch.nortem.parsers.common.SerializableNortemParser;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RoutingParsingApplicationParser extends ParsingApplicationParser {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final String MISSING_ROUTER_FIELDS = "Missing routing fields: %s, %s, in the parsed message: %s";
    static final String MISSING_ARGUMENTS = "Missing arguments in routing parsing application";

    private final String routingConditionField;
    private final String routingMessageField;
    private final NortemParserWrapper routerParser;
    private final ArrayList<String> mergedFields;
    private final ArrayList<NortemParserWrapper> parsers;

    protected RoutingParsingApplicationParser(Builder<?> builder) {
        super(builder);
        this.routingConditionField = builder.routingConditionField;
        this.routingMessageField = builder.routingMessageField;
        this.mergedFields = builder.mergedFields;
        this.parsers = builder.parsers;
        this.routerParser = builder.routerParser;
    }

    @Override
    protected List<ParserResult> parseInternally(byte[] message) {
        List<ParserResult> ret = new ArrayList<>();
        ParserResult routerResult = routerParser.parseToResult(message);
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
            for (NortemParserWrapper parser : parsers) {
                if (!parser.checkCondition(messageToCondition)) {
                    continue;
                }

                ParserResult result = parser.parseToResult(messageToParse.getBytes());
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
        protected String routingConditionField;
        protected String routingMessageField;
        protected ArrayList<String> mergedFields = new ArrayList<>();
        protected NortemParserWrapper routerParser;
        protected NortemParserWrapper defaultParser;
        protected ArrayList<NortemParserWrapper> parsers =  new ArrayList<>();

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

        public Builder<T> routerParser(SerializableNortemParser nortemParser) throws Exception {
            final RouterCondition alwaysMatch = x -> true;
            this.routerParser = new NortemParserWrapper(alwaysMatch, nortemParser, null);
            return this;
        }

        public Builder<T> defaultParser(String topic, SerializableNortemParser nortemParser) throws Exception {
            final RouterCondition alwaysMatch = x -> true;
            defaultParser = new NortemParserWrapper(alwaysMatch, nortemParser, topic);
            return this;
        }

        public Builder<T> addParser(String topic, SerializableNortemParser nortemParser, String pattern) throws Exception {
            final Pattern conditionPattern = Pattern.compile(pattern);
            final RouterCondition condition = x -> conditionPattern.matcher(x).matches();
            parsers.add(new NortemParserWrapper(condition, nortemParser, topic));
            return this;
        }

        public abstract T build();
    }
}
