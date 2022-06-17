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
    private static final String UNSUPPORTED_ROUTER_PARSER_MESSAGES_SIZE_MSG =
            "Router parser should produce only one message";
    private static final int SUPPORTED_ROUTER_PARSER_MESSAGES_SIZE = 1;

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
    protected ParserResult parseInternally(String source, String metadata, byte[] message) {
        ParserResult routerResult = routerParser.parseToResult(metadata, message);
        if (routerResult.getException() != null) {
            return routerResult;
        }

        List<Map<String, Object>> routerParsedMessages = routerResult.getParsedMessages();
        if (routerParsedMessages.isEmpty()) {
            return routerResult;
        }

        if (routerParsedMessages.size() != SUPPORTED_ROUTER_PARSER_MESSAGES_SIZE) {
            LOG.debug(UNSUPPORTED_ROUTER_PARSER_MESSAGES_SIZE_MSG);
            routerResult.setException(new IllegalStateException(UNSUPPORTED_ROUTER_PARSER_MESSAGES_SIZE_MSG));
            return routerResult;
        }

        var parsedMsg = routerParsedMessages.get(0);
        if (!parsedMsg.containsKey(routingConditionField)
                || !parsedMsg.containsKey(routingMessageField)) {
            String errorMsg = String.format(MISSING_ROUTER_FIELDS,
                    routingConditionField, routingMessageField, parsedMsg);
            LOG.debug(errorMsg);
            routerResult.setException(new IllegalStateException(errorMsg));
            return routerResult;
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
            return result;
        }

        return routerResult;
    }
    public static Builder<RoutingParsingApplicationParser> builder() {
        return new Builder<>() {
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

        public Builder<T> routerParser(SerializableSiembolParser siembolParser) {
            final RouterCondition alwaysMatch = x -> true;
            this.routerParser = new SiembolParserWrapper(alwaysMatch, siembolParser, null);
            return this;
        }

        public Builder<T> defaultParser(String topic, SerializableSiembolParser siembolParser) {
            defaultParser = new SiembolParserWrapper(siembolParser, topic);
            return this;
        }

        public Builder<T> addParser(String topic, SerializableSiembolParser siembolParser, String pattern) {
            final Pattern conditionPattern = Pattern.compile(pattern, Pattern.DOTALL);
            final RouterCondition condition = x -> conditionPattern.matcher(x).matches();
            parsers.add(new SiembolParserWrapper(condition, siembolParser, topic));
            return this;
        }

        public abstract T build();
    }
}
