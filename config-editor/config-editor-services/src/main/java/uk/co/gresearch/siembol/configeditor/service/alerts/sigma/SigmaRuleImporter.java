package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.alerts.model.MatcherDto;
import uk.co.gresearch.siembol.alerts.model.RuleDto;
import uk.co.gresearch.siembol.common.jsonschema.SiembolJsonSchemaValidator;
import uk.co.gresearch.siembol.common.result.SiembolResult;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.siembol.configeditor.common.ConfigImporter;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorUiLayout;
import uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model.SigmaDetectionDto;
import uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model.SigmaImporterAttributesDto;
import uk.co.gresearch.siembol.configeditor.service.alerts.sigma.model.SigmaRuleDto;

import java.lang.invoke.MethodHandles;
import java.util.*;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;
import static uk.co.gresearch.siembol.configeditor.service.alerts.sigma.SigmaConditionToken.*;

public class SigmaRuleImporter implements ConfigImporter {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader IMPORTER_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(SigmaImporterAttributesDto.class);
    private static final ObjectReader SIGMA_RULE_READER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readerFor(SigmaRuleDto.class);
    private static final ObjectReader SIGMA_RULE_MAP_READER = new ObjectMapper(new YAMLFactory())
            .readerFor(new TypeReference<Map<String, Object>>() {
            });
    private static final ObjectWriter ALERTING_RULE_WRITER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerFor(RuleDto.class)
            .with(SerializationFeature.INDENT_OUTPUT);
    private static final String ERROR_ATTRIBUTES_INIT_LOG = "Error in initialising sigma importer attributes schema";
    private static final String ERROR_IMPORT_CONFIG_LOG = "Error during importing sigma rule: {}, " +
            "attributes: {}, user:{}, exception: {}";
    private static final String ERROR_TOKENS_PARSING = "Problem during parsing of condition tokens";

    private final String importerAttributesSchema;
    private final SiembolJsonSchemaValidator importerAttributesValidator;

    public SigmaRuleImporter(Builder builder) {
        this.importerAttributesSchema = builder.importerAttributesSchema;
        this.importerAttributesValidator = builder.importerAttributesValidator;
    }

    @Override
    public ConfigEditorResult getImporterAttributesSchema() {
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setConfigImporterAttributesSchema(importerAttributesSchema);
        return new ConfigEditorResult(OK, attributes);
    }

    @Override
    public ConfigEditorResult validateImporterAttributes(String attributes) {
        return ConfigEditorResult.fromValidationResult(importerAttributesValidator.validate(attributes));
    }

    @Override
    public ConfigEditorResult importConfig(UserInfo user, String importerAttributes, String configuration) {
        SiembolResult attributesValidationResult = importerAttributesValidator.validate(importerAttributes);
        if (attributesValidationResult.getStatusCode() != SiembolResult.StatusCode.OK) {
            return ConfigEditorResult.fromValidationResult(attributesValidationResult);
        }

        try {
            SigmaImporterAttributesDto attributes = IMPORTER_ATTRIBUTES_READER.readValue(importerAttributes);
            SigmaRuleDto sigmaRule = SIGMA_RULE_READER.readValue(configuration);
            Map<String, Object> sigmaRuleMap = SIGMA_RULE_MAP_READER.readValue(configuration);
            RuleDto ret = createRule(attributes, sigmaRuleMap);
            ret.setRuleAuthor(user.getUserName());

            List<MatcherDto> matchers = createMatchers(attributes, sigmaRule);
            ret.setMatchers(matchers);

            String imported = ALERTING_RULE_WRITER.writeValueAsString(ret);
            ConfigEditorAttributes resultAttributes = new ConfigEditorAttributes();
            resultAttributes.setImportedConfiguration(imported);
            return new ConfigEditorResult(OK, resultAttributes);
        } catch (Exception e) {
            LOG.error(ERROR_IMPORT_CONFIG_LOG,
                    configuration,
                    importerAttributes,
                    user.getUserName(),
                    ExceptionUtils.getStackTrace(e));
            return ConfigEditorResult.fromException(BAD_REQUEST, e);
        }
    }

    private RuleDto createRule(SigmaImporterAttributesDto attributes, Map<String, Object> sigmaRuleMap) throws Exception {
        RuleDto ret = new RuleDto();
        BeanUtils.copyProperties(ret, attributes.getRuleMetadataMapping());
        EvaluationLibrary.substituteBean(ret, sigmaRuleMap);
        ret.setRuleName(ConfigEditorUtils.getNormalisedConfigName(ret.getRuleName()));
        return ret;
    }

    private List<MatcherDto> createMatchers(SigmaImporterAttributesDto attributes, SigmaRuleDto sigmaRule) {
        Map<String, String> fieldMap = new HashMap<>();
        if (attributes.getFieldMapping() != null) {
            attributes.getFieldMapping().forEach(x -> fieldMap.put(x.getSigmaField(), x.getSiembolField()));
        }

        Map<String, SigmaSearch> sigmaSearchMap = getSigmaSearches(sigmaRule.getDetection(), fieldMap);
        List<Pair<SigmaConditionToken, String>> conditionTokens = SigmaConditionToken.tokenize(
                sigmaRule.getDetection().getCondition());

        return getMatchers(sigmaSearchMap, conditionTokens);
    }

    private Map<String, SigmaSearch> getSigmaSearches(SigmaDetectionDto detection, Map<String, String> fieldMapping) {
        Map<String, SigmaSearch> ret = new HashMap<>();

        for (Map.Entry<String, JsonNode> search: detection.getSearchesMap().entrySet()) {
            SigmaSearch.SearchType searchType = search.getValue().isArray()
                    ? SigmaSearch.SearchType.LIST
                    : SigmaSearch.SearchType.MAP;
            SigmaSearch.Builder builder = new SigmaSearch.Builder(searchType, search.getKey())
                    .fieldMapping(fieldMapping);

            if (search.getValue().isArray()) {
                builder.addList(search.getValue());
            } else {
                search.getValue().fieldNames().forEachRemaining(x -> builder.addMapEntry(x, search.getValue().get(x)));
            }
            ret.put(search.getKey(), builder.build());
        }

        return ret;
    }

    private List<MatcherDto> getMatchers(Map<String, SigmaSearch> sigmaSearchMap,
                                         List<Pair<SigmaConditionToken, String>> conditionTokens) {

        SigmaConditionTokenNode root = generateConditionSyntaxTree(sigmaSearchMap, conditionTokens);
        return root.getToken().getMatchers(root);
    }

    private Optional<Integer> getBinaryOperatorIndex(List<Pair<SigmaConditionToken, String>> conditionTokens) {
        int numOpenedBrackets = 0;
        Optional<Integer> ret = Optional.empty();
        for (int i = 0; i < conditionTokens.size(); i++) {
            switch (conditionTokens.get(i).getLeft()) {
                case TOKEN_LEFT_BRACKET:
                    numOpenedBrackets++;
                    break;
                case TOKEN_RIGHT_BRACKET:
                    numOpenedBrackets--;
                    break;
                case TOKEN_OR:
                    if (numOpenedBrackets == 0) {
                        return Optional.of(i);
                    }
                    break;
                case TOKEN_AND:
                    if (numOpenedBrackets == 0 && !ret.isPresent()) {
                        ret = Optional.of(i);
                    }
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    private SigmaConditionTokenNode generateConditionBinaryOperatorTree(
            Map<String, SigmaSearch> sigmaSearchMap,
            List<Pair<SigmaConditionToken, String>> conditionTokens,
            int operatorIndex) {
        SigmaConditionTokenNode node = new SigmaConditionTokenNode(
                conditionTokens.get(operatorIndex),
                sigmaSearchMap);

        SigmaConditionTokenNode left = generateConditionSyntaxTree(sigmaSearchMap,
                conditionTokens.subList(0, operatorIndex));
        node.setFirstOperand(left);

        SigmaConditionTokenNode right = generateConditionSyntaxTree(sigmaSearchMap,
                conditionTokens.subList(operatorIndex + 1, conditionTokens.size()));
        node.setSecondOperand(right);

        return node;
    }

    private SigmaConditionTokenNode generateConditionUnaryOperatorTree(
            Map<String, SigmaSearch> sigmaSearchMap,
            List<Pair<SigmaConditionToken, String>> conditionTokens) {
        SigmaConditionTokenNode node = new SigmaConditionTokenNode(
                conditionTokens.get(0),
                sigmaSearchMap);

        SigmaConditionTokenNode left = generateConditionSyntaxTree(sigmaSearchMap,
                conditionTokens.subList(1, conditionTokens.size()));
        node.setFirstOperand(left);

        return node;
    }

    private SigmaConditionTokenNode generateConditionSyntaxTree(
            Map<String, SigmaSearch> sigmaSearchMap,
            List<Pair<SigmaConditionToken, String>> conditionTokens) {
        if (conditionTokens.isEmpty()) {
            throw new IllegalArgumentException(ERROR_TOKENS_PARSING);
        }

        if (TOKEN_LEFT_BRACKET.equals(conditionTokens.get(0).getLeft())
                && TOKEN_RIGHT_BRACKET.equals(conditionTokens.get(conditionTokens.size() - 1).getLeft())) {
            return generateConditionSyntaxTree(sigmaSearchMap, conditionTokens.subList(1, conditionTokens.size() - 1));
        }

        Optional<Integer> binaryOperatorIndex = getBinaryOperatorIndex(conditionTokens);
        if (binaryOperatorIndex.isPresent()) {
            return generateConditionBinaryOperatorTree(sigmaSearchMap, conditionTokens, binaryOperatorIndex.get());
        }

        if (SigmaConditionTokenType.UNARY_OPERATOR.equals(conditionTokens.get(0).getLeft().getType())) {
            return generateConditionUnaryOperatorTree(sigmaSearchMap, conditionTokens);
        }

        if (TOKEN_ID.equals(conditionTokens.get(0).getLeft())
                && conditionTokens.size() == 1) {
            return new SigmaConditionTokenNode(conditionTokens.get(0), sigmaSearchMap);
        }

        throw new IllegalStateException(ERROR_TOKENS_PARSING);
    }

    public static class Builder {
        ConfigEditorUiLayout configEditorUiLayout =  new ConfigEditorUiLayout();
        String importerAttributesSchema;
        SiembolJsonSchemaValidator importerAttributesValidator;

        public Builder configEditorUiLayout(ConfigEditorUiLayout configEditorUiLayout) {
            this.configEditorUiLayout = configEditorUiLayout;
            return this;
        }

        public SigmaRuleImporter build() throws Exception {
            importerAttributesValidator  = new SiembolJsonSchemaValidator(SigmaImporterAttributesDto.class);
            Optional<String> patchedSchema = ConfigEditorUtils.patchJsonSchema(
                    importerAttributesValidator.getJsonSchema().getAttributes().getJsonSchema(),
                    configEditorUiLayout.getImportersLayout());
            if (!patchedSchema.isPresent()) {
                LOG.error(ERROR_ATTRIBUTES_INIT_LOG);
                throw new IllegalStateException(ERROR_ATTRIBUTES_INIT_LOG);
            }
            importerAttributesSchema = patchedSchema.get();

            return new SigmaRuleImporter(this);
        }
    }
}
