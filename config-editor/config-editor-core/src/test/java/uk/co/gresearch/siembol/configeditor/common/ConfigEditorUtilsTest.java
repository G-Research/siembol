package uk.co.gresearch.siembol.configeditor.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ConfigEditorUtilsTest {
    private final String rulesSchema = """
               {
                 "rules_schema": {
                   "type": "object",
                   "description": "Incident Response Rules",
                   "title": "rules",
                   "properties": {
                     "rules_version": {
                       "type": "integer",
                       "description": "Incident response rules version",
                       "default": 0
                     },
                     "rules": {
                       "type": "array",
                       "items": {
                         "type": "object",
                         "description": "Response rule that should handle response to a siembol alert",
                         "title": "rule",
                         "properties": {
                           "rule_name": {
                             "type": "string",
                             "description": "ResponseRule name that uniquely identifies the rule"
                           },
                           "rule_author": {
                             "type": "string",
                             "description": "The owner of the rule"
                           },
                           "rule_version": {
                             "type": "integer",
                             "description": "The version of the rule",
                             "default": 0
                           },
                           "rule_description": {
                             "type": "string",
                             "description": "The description of the rule"
                           },
                           "evaluators": {
                             "type": "array",
                             "items": {
                               "type": "object",
                               "description": "Response evaluator used in response rules",
                               "title": "response evaluator",
                               "oneOf": [
                                 {
                                   "type": "object",
                                   "title": "matching_evaluator",
                                   "properties": {
                                     "evaluator_type": {
                                       "enum": [
                                         "matching_evaluator"
                                       ],
                                       "default": "matching_evaluator"
                                     },
                                     "evaluator_attributes": {
                                       "type": "object",
                                       "description": "Attributes for matching evaluator",
                                       "title": "matching evaluator attributes",
                                       "properties": {
                                         "evaluation_result": {
                                           "enum": [
                                             "match",
                                             "filtered"
                                           ],
                                           "type": "string",
                                           "description": "Evaluation result returned by the evaluator after matching",
                                           "default": "match"
                                         },
                                         "matchers": {
                                           "type": "array",
                                           "items": {
                                             "type": "object",
                                             "description": "Matcher for matching fields in response rules",
                                             "title": "matcher",
                                             "properties": {
                                               "matcher_type": {
                                                 "enum": [
                                                   "REGEX_MATCH",
                                                   "IS_IN_SET"
                                                 ],
                                                 "type": "string",
                                                 "description": "Type of matcher, either Regex match or list of strings (newline delimited)"
                                               },
                                               "is_negated": {
                                                 "type": "boolean",
                                                 "description": "The matcher is negated",
                                                 "default": false
                                               },
                                               "field": {
                                                 "type": "string",
                                                 "description": "Field on which the matcher will be evaluated"
                                               },
                                               "case_insensitive": {
                                                 "type": "boolean",
                                                 "description": "Use case insensitive string compare",
                                                 "default": false
                                               },
                                               "data": {
                                                 "type": "string",
                                                 "description": "Matcher expression as defined by matcher type"
                                               }
                                             },
                                             "required": [
                                               "data",
                                               "field",
                                               "matcher_type"
                                             ]
                                           },
                                           "description": "Matchers of the evaluator",
                                           "minItems": 1
                                         }
                                       },
                                       "required": [
                                         "evaluation_result",
                                         "matchers"
                                       ]
                                     }
                                   },
                                   "required": [
                                     "evaluator_type",
                                     "evaluator_attributes"
                                   ]
                                 }
                               ]
                             },
                             "description": "Evaluators of the rule",
                             "minItems": 1
                           }
                         },
                         "required": [
                           "evaluators",
                           "rule_author",
                           "rule_name",
                           "rule_version"
                         ]
                       },
                       "description": "Response rules",
                       "minItems": 1
                     }
                   },
                   "required": [
                     "rules",
                     "rules_version"
                   ]
                 }
               }
            """;

    private final String layoutConfig = """
             {
                 "$..evaluators": {
                   "widget": {
                         "formlyConfig": {
                             "type": "tab-array"
                         }
                     }
                 },
                 "$..rule_description": {
                   "widget": {
                         "formlyConfig": {
                             "type": "textarea",
                             "wrappers": []
                         }
                     }
                 },
                 "$..matchers.items": {
                   "widget": {
                         "formlyConfig": {
                             "wrappers": [
                                 "expansion-panel"
                             ]
                         }
                     }
                 },
                 "$..matchers.items.properties.data": {
                   "title" : "changed"
                 }
             }
            """;


    private final String unknownKeyConfig = """
             {
                 "robots": {
                   "type": "tab-array"
                 }
             }
            """;

    private final String multipleKeyConfig = """
             {
                 "$..items": {
                   "type": "tab-array"
                 }
             }
            """;

    private final String valueWithString = """
             {
                 "rules_schema.description": {
                   "type": "tab-array"
                 }
             }
            """;

    private final String valueEmptyObject = """
             {
                 "$..evaluators": {
                 }
             }
            """;

    private final String valueString = """
             {
                 "$..evaluators": "dummy"
             }
            """;

    private static final ObjectReader JSON_OBJECT_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() {
            });
    private static final ObjectReader FORM_ATTRIBUTES_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, JsonNode>>() {
            });


    @Test
    public void patchSchemaOk() throws IOException {
        Optional<String> patched = ConfigEditorUtils.patchJsonSchema(rulesSchema,
                FORM_ATTRIBUTES_READER.readValue(layoutConfig));
        Assert.assertTrue(patched.isPresent());
        Map<String, Object> schema = JSON_OBJECT_READER.readValue(patched.get());
        Assert.assertNotNull(schema);
    }

    @Test
    public void patchSchemaEmptyConfigOk() throws IOException {
        Optional<String> patched = ConfigEditorUtils.patchJsonSchema(rulesSchema,
                FORM_ATTRIBUTES_READER.readValue("{}"));
        Assert.assertTrue(patched.isPresent());
        Map<String, Object> schema = JSON_OBJECT_READER.readValue(patched.get());
        Assert.assertNotNull(schema);
    }

    @Test
    public void patchSchemaNonExistingPath() throws IOException {
        Optional<String> patched = ConfigEditorUtils.patchJsonSchema(rulesSchema,
                FORM_ATTRIBUTES_READER.readValue(unknownKeyConfig));
        Assert.assertFalse(patched.isPresent());
    }

    @Test
    public void patchSchemaMultiplePaths() throws IOException {
        Optional<String> patched = ConfigEditorUtils.patchJsonSchema(rulesSchema,
                FORM_ATTRIBUTES_READER.readValue(multipleKeyConfig));
        Assert.assertFalse(patched.isPresent());
    }

    @Test
    public void patchSchemaNotJsonObject() throws IOException {
        Optional<String> patched = ConfigEditorUtils.patchJsonSchema(rulesSchema,
                FORM_ATTRIBUTES_READER.readValue(valueWithString));
        Assert.assertFalse(patched.isPresent());
    }

    @Test
    public void patchSchemaEmptyObject() throws IOException {
        Optional<String> patched = ConfigEditorUtils.patchJsonSchema(rulesSchema,
                FORM_ATTRIBUTES_READER.readValue(valueEmptyObject));
        Assert.assertTrue(patched.isPresent());
    }

    @Test
    public void patchSchemaStringValue() throws IOException {
        Optional<String> patched = ConfigEditorUtils.patchJsonSchema(rulesSchema,
                FORM_ATTRIBUTES_READER.readValue(valueString));
        Assert.assertFalse(patched.isPresent());
    }

}
