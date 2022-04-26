package uk.co.gresearch.siembol.enrichments.compiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.gresearch.siembol.enrichments.common.EnrichmentResult;

import java.io.IOException;
import java.util.Map;

import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.enrichments.common.EnrichmentResult.StatusCode.OK;
import static uk.co.gresearch.siembol.enrichments.compiler.EnrichmentCompilerImpl.createEnrichmentsCompiler;

public class EnrichmentCompilerTest {
    private static final ObjectReader JSON_MAP_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {
            });

    private final String testRules = """
            {
               "rules_version": 1,
               "rules": [
                 {
                   "rule_name": "siembol_enrichments_test",
                   "rule_version": 1,
                   "rule_author": "dummy",
                   "rule_description": "Test rule",
                   "source_type": "*",
                   "matchers": [
                     {
                       "matcher_type": "REGEX_MATCH",
                       "is_negated": false,
                       "field": "is_alert",
                       "data": "(?i)true"
                     }
                   ],
                   "table_mapping": {
                     "table_name": "test_table",
                     "joining_key": "${ip_src_addr}",
                     "tags": [
                       {
                         "tag_name": "is_test_tag",
                         "tag_value": "true"
                       }
                     ],
                     "enriching_fields": [
                       {
                         "table_field_name": "dns_name",
                         "event_field_name": "siembol:enrichments:dns"
                       }
                     ]
                   }
                 }
               ]
             }
            """;

    private final String testRulesTagsOnly = """
            {
               "rules_version": 1,
               "rules": [
                 {
                   "rule_name": "siembol_enrichments_test",
                   "rule_version": 1,
                   "rule_author": "dummy",
                   "rule_description": "Test rule",
                   "source_type": "*",
                   "matchers": [
                     {
                       "matcher_type": "REGEX_MATCH",
                       "is_negated": false,
                       "field": "is_alert",
                       "data": "(?i)true"
                     }
                   ],
                   "table_mapping": {
                     "table_name": "test_table",
                     "joining_key": "${ip_src_addr}",
                     "tags": [
                       {
                         "tag_name": "is_test_tag",
                         "tag_value": "true"
                       }
                     ]
                   }
                 }
               ]
            }
            """;


    private final String testRulesEnrichingFieldsOnly = """
            {
               "rules_version": 1,
               "rules": [
                 {
                   "rule_name": "siembol_enrichments_test",
                   "rule_version": 1,
                   "rule_author": "dummy",
                   "rule_description": "Test rule",
                   "source_type": "*",
                   "matchers": [
                     {
                       "matcher_type": "REGEX_MATCH",
                       "is_negated": false,
                       "field": "is_alert",
                       "data": "(?i)true"
                     }
                   ],
                   "table_mapping": {
                     "table_name": "test_table",
                     "joining_key": "${ip_src_addr}",
                     "enriching_fields": [
                       {
                         "table_field_name": "dns_name",
                         "event_field_name": "siembol:enrichments:dns"
                       }
                     ]
                   }
                 }
               ]
            }
            """;


    private final String testRulesMissingTagsAndEnrichingFields = """
            {
               "rules_version": 1,
               "rules": [
                 {
                   "rule_name": "siembol_enrichments_test",
                   "rule_version": 1,
                   "rule_author": "dummy",
                   "rule_description": "Test rule",
                   "source_type": "*",
                   "matchers": [
                     {
                       "matcher_type": "REGEX_MATCH",
                       "is_negated": false,
                       "field": "is_alert",
                       "data": "(?i)true"
                     }
                   ],
                   "table_mapping": {
                     "table_name": "test_table",
                     "joining_key": "${ip_src_addr}"
                   }
                 }
               ]
            }
            """;

    private final String testRule = """
            {
               "rule_name": "siembol_enrichments_test",
               "rule_version": 1,
               "rule_author": "dummy",
               "rule_description": "Test rule",
               "source_type": "*",
               "matchers": [
                 {
                   "matcher_type": "REGEX_MATCH",
                   "is_negated": false,
                   "field": "is_alert",
                   "data": "true"
                 }
               ],
               "table_mapping": {
                 "table_name": "test_table",
                 "joining_key": "${ip_src_addr}",
                 "tags": [
                   {
                     "tag_name": "is_test_tag",
                     "tag_value": "true"
                   }
                 ],
                 "enriching_fields": [
                   {
                     "table_field_name": "dns_name",
                     "event_field_name": "siembol:enrichments:dns"
                   }
                 ]
               }
            }
            """;

    private final String testSpecification = """
            {
              "event": {
                "source_type": "secret",
                "is_alert": "true",
                "ip_src_addr": "1.2.3.4"
              },
              "testing_table_name": "test_table",
              "testing_table_mapping": {
                  "1.2.3.4": {
                    "dns_name": "secret.abc"
                  }
                }
            }
            """;

    private final String testSpecificationNoMatch = """
            {
              "event": {
                "source_type": "secret",
                "is_alert": "false",
                "ip_src_addr": "1.2.3.4"
              },
              "testing_table_name": "test_table",
              "testing_table_mapping": {
                "1.2.3.4": {
                  "dns_name": "secret.abc"
                }
              }
            }
            """;

    private EnrichmentCompiler enrichmentCompiler;

    @Before
    public void setUp() throws Exception {
        enrichmentCompiler = createEnrichmentsCompiler();
    }

    @Test
    public void getRulesSchema() {
        EnrichmentResult result = enrichmentCompiler.getSchema();
        Assert.assertNotNull(result.getAttributes().getRulesSchema());
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void getTestSpecificationSchema() {
        EnrichmentResult result = enrichmentCompiler.getTestSpecificationSchema();
        Assert.assertNotNull(result.getAttributes().getTestSchema());
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void validateRulesOK() {
        EnrichmentResult result = enrichmentCompiler.validateConfigurations(testRules);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void validateRulesInvalidJson() {
        EnrichmentResult result = enrichmentCompiler.validateConfigurations("INVALID");
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("INVALID"));
    }

    @Test
    public void compileeRulesInvalidJson() {
        EnrichmentResult result = enrichmentCompiler.compile("INVALID");
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("INVALID"));
    }

    @Test
    public void validateRulesMissingRequiredFields() {
        EnrichmentResult result = enrichmentCompiler.validateConfigurations(testRules.replace("table_mapping",
                "unknown"));
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("missing required properties"));
    }

    @Test
    public void validateRuleOK() {
        EnrichmentResult result = enrichmentCompiler.validateConfiguration(testRule);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void validateRuleInvalidJson() {
        EnrichmentResult result = enrichmentCompiler.validateConfiguration("INVALID");
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("INVALID"));
    }

    @Test
    public void validateRuleInvalidRegex() {
        EnrichmentResult result = enrichmentCompiler.validateConfiguration(
                testRule.replaceAll( "true", "(?<"));
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("PatternSyntaxException"));
    }

    @Test
    public void validateRuleMissingRequiredFields() {
        EnrichmentResult result = enrichmentCompiler.validateConfiguration(testRule.replace("table_mapping",
                "unknown"));
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void testRulesOK() throws IOException {
        EnrichmentResult result = enrichmentCompiler.testConfigurations(testRules, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestRawResult());
        Assert.assertNotNull(result.getAttributes().getTestResult());
        Map<String, Object> rawResult = JSON_MAP_READER.readValue(result.getAttributes().getTestRawResult());
        Assert.assertEquals("secret", rawResult.get("source_type"));
        Assert.assertEquals("true", rawResult.get("is_alert"));
        Assert.assertEquals("1.2.3.4", rawResult.get("ip_src_addr"));
        Assert.assertEquals("true", rawResult.get("is_test_tag"));
        Assert.assertEquals("secret.abc", rawResult.get("siembol:enrichments:dns"));
    }

    @Test
    public void testRuleOK() throws IOException {
        EnrichmentResult result = enrichmentCompiler.testConfiguration(testRule, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestRawResult());
        Assert.assertNotNull(result.getAttributes().getTestResult());
        Map<String, Object> rawResult = JSON_MAP_READER.readValue(result.getAttributes().getTestRawResult());
        Assert.assertEquals("secret", rawResult.get("source_type"));
        Assert.assertEquals("true", rawResult.get("is_alert"));
        Assert.assertEquals("1.2.3.4", rawResult.get("ip_src_addr"));
        Assert.assertEquals("true", rawResult.get("is_test_tag"));
        Assert.assertEquals("secret.abc", rawResult.get("siembol:enrichments:dns"));
    }

    @Test
    public void testRuleEmptyCommandOK() throws IOException {
        EnrichmentResult result = enrichmentCompiler.testConfiguration(testRule, testSpecificationNoMatch);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestRawResult());
        Assert.assertNotNull(result.getAttributes().getTestResult());
        Assert.assertTrue(result.getAttributes().getTestResult().contains("Nothing to enrich"));
        Map<String, Object> rawResult = JSON_MAP_READER.readValue(result.getAttributes().getTestRawResult());
        Assert.assertEquals("secret", rawResult.get("source_type"));
        Assert.assertEquals("false", rawResult.get("is_alert"));
        Assert.assertEquals("1.2.3.4", rawResult.get("ip_src_addr"));
    }

    @Test
    public void testRulesTagsOnlyOK() throws IOException {
        EnrichmentResult result = enrichmentCompiler.testConfigurations(testRulesTagsOnly, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestRawResult());
        Assert.assertNotNull(result.getAttributes().getTestResult());
        Map<String, Object> rawResult = JSON_MAP_READER.readValue(result.getAttributes().getTestRawResult());
        Assert.assertEquals("secret", rawResult.get("source_type"));
        Assert.assertEquals("true", rawResult.get("is_alert"));
        Assert.assertEquals("1.2.3.4", rawResult.get("ip_src_addr"));
        Assert.assertEquals("true", rawResult.get("is_test_tag"));
        Assert.assertNull(rawResult.get("siembol:enrichments:dns"));
    }

    @Test
    public void testRulesEnrichingFieldOnlyOK() throws IOException {
        EnrichmentResult result = enrichmentCompiler.testConfigurations(testRulesEnrichingFieldsOnly, testSpecification);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getTestRawResult());
        Assert.assertNotNull(result.getAttributes().getTestResult());
        Map<String, Object> rawResult = JSON_MAP_READER.readValue(result.getAttributes().getTestRawResult());
        Assert.assertEquals("secret", rawResult.get("source_type"));
        Assert.assertEquals("true", rawResult.get("is_alert"));
        Assert.assertEquals("1.2.3.4", rawResult.get("ip_src_addr"));
        Assert.assertNull(rawResult.get("is_test_tag"));
        Assert.assertEquals("secret.abc", rawResult.get("siembol:enrichments:dns"));
    }

    @Test
    public void testRulesMissingTagsAndEnrichingFields() {
        EnrichmentResult result = enrichmentCompiler.testConfigurations(testRulesMissingTagsAndEnrichingFields,
                testSpecification);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
        Assert.assertTrue(result.getAttributes().getMessage().contains("Both enriching fields and tags are empty"));

    }

    @Test
    public void testingRuleInvalidRegex() {
        EnrichmentResult result = enrichmentCompiler.testConfiguration(
                testRule.replaceAll( "true", "(?<"), testSpecification);
        Assert.assertEquals(ERROR, result.getStatusCode());
        Assert.assertTrue(result.getAttributes().getMessage().contains("PatternSyntaxException"));
    }
}
