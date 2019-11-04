package uk.co.gresearch.nortem.configeditor.service.centrifuge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.utils.HttpProvider;
import uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeResponseDto;
import uk.co.gresearch.nortem.configeditor.common.ConfigEditorUtils;
import uk.co.gresearch.nortem.configeditor.service.centrifuge.model.CentrifugeTestSpecificationDto;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;

class CentrifugeSchemaService {
    private static final String GET_SCHEMA_PATH = "/api/v1/rules/schema";
    private static final String VALIDATE_RULES_PATH = "/api/v1/rules/validate";
    private static final String TEST_RULES_PATH = "/api/v1/rules/test";
    private static final String FIELDS_PATH = "/api/v1/fields";
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectReader TEST_SPECIFICATION_READER = new ObjectMapper()
            .readerFor(CentrifugeTestSpecificationDto.class);

    private final HttpProvider httpProvider;
    private final String rulesSchema;
    private final String testSchema;
    private final String centrifugeFields;

    CentrifugeSchemaService(Builder builder) {
        this.httpProvider = builder.httpProvider;
        this.rulesSchema = builder.rulesSchema;
        this.centrifugeFields = builder.centrifugeFields;
        this.testSchema = builder.testSchema;
    }

    public String getRulesSchema() {
        return rulesSchema;
    }

    public String getTestSchema() {
        return testSchema;
    }

    public String getFields() {
        return centrifugeFields;
    }

    public CentrifugeResponseDto validateRules(String rules) throws IOException {
        String json = this.httpProvider.post(VALIDATE_RULES_PATH, rules);
        return MAPPER.readValue(json, CentrifugeResponseDto.class);
    }

    public CentrifugeResponseDto testRules(String rules, String testSpecification) throws IOException {
        CentrifugeTestSpecificationDto specificationDto = TEST_SPECIFICATION_READER.readValue(testSpecification);
        CentrifugeResponseDto.Attributes attr = new CentrifugeResponseDto.Attributes();
        attr.setEvent(specificationDto.getEventContent());
        attr.setJsonRules(rules);

        String body = MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(attr);
        String json = this.httpProvider.post(TEST_RULES_PATH, body);
        return MAPPER.readValue(json, CentrifugeResponseDto.class);
    }

    public static class Builder {
        private final HttpProvider httpProvider;
        private final String uiConfig;
        private String testSchema;
        private Optional<String> testUiConfig = Optional.empty();

        private String rulesSchema;
        private String centrifugeFields;

        public Builder(HttpProvider httpProvider, String uiConfig) {
            this.httpProvider = httpProvider;
            this.uiConfig = uiConfig;
        }

        public Builder uiTestConfig(Optional<String> testUiConfig) {
            this.testUiConfig = testUiConfig;
            return this;
        }

        public CentrifugeSchemaService build() throws Exception {
            LOG.info("Obtaining and computing centrifuge schema");
            Optional<String> schema = getAndComputeSchema();
            if (!schema.isPresent()) {
                throw new IllegalStateException("Error during obtaining centrifuge schema");
            }

            rulesSchema = schema.get();
            LOG.info("Computation of Centrifuge schema completed");

            LOG.info("Computing centrifuge test schema");
            NortemJsonSchemaValidator testValidator = new NortemJsonSchemaValidator(CentrifugeTestSpecificationDto.class);
            String validatorTestSchema = testValidator.getJsonSchema().getAttributes().getJsonSchema();
            testSchema = testUiConfig.isPresent()
                    ? ConfigEditorUtils.computeRulesSchema(validatorTestSchema, testUiConfig.get()).get()
                    : validatorTestSchema;

            LOG.info("Computing centrifuge test schema completed");

            LOG.info("Obtaining centrifuge fields");
            Optional<String> fields = getFields();
            if (!fields.isPresent()) {
                throw new IllegalStateException("Error during obtaining centrifuge fields");
            }
            centrifugeFields = fields.get();
            LOG.info("Obtaining centrifuge fields completed");
            return new CentrifugeSchemaService(this);
        }

        private Optional<String> getSchema() throws IOException {
            String responseBody = this.httpProvider.get(GET_SCHEMA_PATH);
            CentrifugeResponseDto response = MAPPER.readValue(responseBody, CentrifugeResponseDto.class);

            if (response == null
                    || response.getStatusCode() != CentrifugeResponseDto.StatusCode.OK
                    || response.getAttributes() == null) {
                throw new IllegalStateException("Empty schema response");
            }

            return Optional.ofNullable(MAPPER.writeValueAsString(response.getAttributes().getSchema()));
        }

        private Optional<String> computeRulesSchema(String rulesSchema, String uiConfig) throws IOException {
            return ConfigEditorUtils.computeRulesSchema(rulesSchema, uiConfig);
        }

        private Optional<String> getAndComputeSchema() throws IOException {
            Optional<String> schema = getSchema();
            if (!schema.isPresent()) {
                throw new IllegalStateException("unable to retrieve valid schema");
            }
            return computeRulesSchema(schema.get(), uiConfig);
        }

        private Optional<String> getFields() throws IOException {
            String responseBody = httpProvider.get(FIELDS_PATH);
            CentrifugeResponseDto response = MAPPER.readValue(responseBody, CentrifugeResponseDto.class);
            if (response == null
                    || response.getStatusCode() != CentrifugeResponseDto.StatusCode.OK
                    || response.getAttributes() == null) {
                throw new IllegalStateException("Empty fields response");
            }

            return Optional.ofNullable(MAPPER.writeValueAsString(response.getAttributes().getFields()));
        }
    }
}
