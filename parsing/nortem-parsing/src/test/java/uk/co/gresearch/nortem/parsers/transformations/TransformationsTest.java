package uk.co.gresearch.nortem.parsers.transformations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.gresearch.nortem.parsers.model.TransformationDto;

import java.io.IOException;

import java.util.Map;

public class TransformationsTest {
    private static final ObjectReader JSON_TRANSFORMATION_READER =
            new ObjectMapper().readerFor(TransformationDto.class);
    private static final ObjectReader JSON_LOG_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });

    private Map<String, Object> log;
    private Transformation transformation;
    private final TransformationFactory factory = new TransformationFactory();

    @Before
    public void setUp() throws IOException {
        log = JSON_LOG_READER.readValue(message);
    }

    /**
     * {
     *   "transformation_type": "field_name_string_replace",
     *   "attributes": {
     *     "string_replace_target": " ",
     *     "string_replace_replacement": "_"
     *   }
     * }
     **/
    @Multiline
    public static String transformationReplace;

    /**
     * {
     *   "transformation_type": "filter_message",
     *   "attributes": {
     *     "message_filter" : {
     *         "matchers" : [
     *          {
     *              "field_name" : "dummy field",
     *              "pattern" : "abc",
     *              "negated" : false
     *          },
     *          {
     *              "field_name" : "secret_field",
     *              "pattern" : "secret",
     *              "negated" : true
     *          }
     *          ]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationFilter;

    /**
     * {
     *   "transformation_type": "field_name_string_replace_all",
     *   "attributes": {
     *     "string_replace_target": " ",
     *     "string_replace_replacement": "_"
     *   }
     * }
     **/
    @Multiline
    public static String transformationReplaceAll;

    /**
     * {
     *   "transformation_type": "field_name_string_delete_all",
     *   "attributes": {
     *     "string_replace_target": " "
     *   }
     * }
     **/
    @Multiline
    public static String transformationDeleteAll;

    /**
     *{
     *   "transformation_type": "trim_value",
     *   "attributes": {
     *     "fields_filter": {
     *      "including_fields": ["timestamp", "trim_field"]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationTrim;

    /**
     *{
     *   "transformation_type": "chomp_value",
     *   "attributes": {
     *     "fields_filter": {
     *      "including_fields": ["timestamp", "chomp_field"]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationChomp;


    /**
     *{
     *   "transformation_type": "delete_fields",
     *   "attributes": {
     *     "fields_filter": {
     *       "including_fields": [".*"],
     *       "excluding_fields": ["timestamp"]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationDelete;

    /**
     *{
     *   "transformation_type": "rename_fields",
     *   "attributes": {
     *     "field_rename_map": [
     *     {
     *       "field_to_rename": "timestamp",
     *       "new_name": "timestamp_renamed"
     *     },
     *     {
     *       "field_to_rename": "dummy field",
     *       "new_name": "dummy_field_renamed"
     *     }
     *     ]
     *   }
     * }
     **/
    @Multiline
    public static String transformationRename;

    /**
     *{
     *   "transformation_type": "field_name_uppercase"
     * }
     **/
    @Multiline
    public static String transformationFieldNameUpperCase;

    /**
     *{
     *   "transformation_type": "field_name_lowercase",
     *   "attributes": {
     *   }
     * }
     **/
    @Multiline
    public static String transformationFieldLowerCase;

    /**
     *{
     *   "transformation_type": "lowercase_value",
     *   "attributes": {
     *     "fields_filter": {
     *      "including_fields": ["timestamp", "chomp_field"]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationLowerCase;

    /**
     *{
     *   "transformation_type": "uppercase_value",
     *   "attributes": {
     *     "fields_filter": {
     *      "including_fields": ["timestamp", "chomp_field"]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationUpperCase;



    /**
     * {"timestamp":12345, "test field a" : "true", "trim_field" : "   message     ", "dummy field" : "abc", "chomp_field" : "message\n"}
     **/
    @Multiline
    public static String message;

    @Test
    public void testGoodReplace() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationReplace));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("true", transformed.get("test_field a"));
        Assert.assertEquals("abc", transformed.get("dummy_field"));
    }

    @Test
    public void testGoodReplaceAll() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationReplaceAll));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("true", transformed.get("test_field_a"));
        Assert.assertEquals("abc", transformed.get("dummy_field"));
    }

    @Test
    public void testDeleteAll() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationDeleteAll));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("true", transformed.get("testfielda"));
        Assert.assertEquals("abc", transformed.get("dummyfield"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadReplace() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationReplace);
        specification.getAttributes().setStringReplaceTarget(null);
        transformation = factory.create(specification);
    }

    @Test
    public void testGoodTrim() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationTrim));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("message", transformed.get("trim_field"));
        Assert.assertEquals(12345, transformed.get("timestamp"));
    }

    @Test
    public void testGoodChomp() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationChomp));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("message", transformed.get("chomp_field"));
        Assert.assertEquals(12345, transformed.get("timestamp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadTrim() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationTrim);
        specification.getAttributes().setFieldsFilter(null);
        transformation = factory.create(specification);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadChomp() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationChomp);
        specification.getAttributes().setFieldsFilter(null);
        transformation = factory.create(specification);
    }

    @Test
    public void testGoodDelete() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationDelete));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(1, transformed.size());
        Assert.assertEquals(12345, transformed.get("timestamp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDelete() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationDelete);
        specification.getAttributes().setFieldsFilter(null);
        transformation = factory.create(specification);
    }

    @Test
    public void testGoodRename() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationRename));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(5, transformed.size());
        Assert.assertEquals(12345, transformed.get("timestamp_renamed"));
        Assert.assertEquals("true", transformed.get("test field a"));
        Assert.assertEquals("   message     ", transformed.get("trim_field"));
        Assert.assertEquals("message\n", transformed.get("chomp_field"));
        Assert.assertEquals("abc", transformed.get("dummy_field_renamed"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadRename() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationRename);
        specification.getAttributes().setFieldRenameMap(null);
        transformation = factory.create(specification);
    }

    @Test
    public void testGoodFilter() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationFilter));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(0, transformed.size());

    }

    @Test
    public void testGoodFilter2() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationFilter));
        Assert.assertTrue(transformation != null);

        log.put("secret_field", "wrong");
        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(0, transformed.size());
    }

    @Test
    public void testGoodFilter3() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationFilter));
        Assert.assertTrue(transformation != null);

        log.put("secret_field", "secret");
        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(6, transformed.size());
    }

    @Test
    public void testGoodFieldNameUpperCase() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationFieldNameUpperCase));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(5, transformed.size());
        Assert.assertEquals(12345, transformed.get("TIMESTAMP"));
        Assert.assertEquals("true", transformed.get("TEST FIELD A"));
        Assert.assertEquals("   message     ", transformed.get("TRIM_FIELD"));
        Assert.assertEquals("message\n", transformed.get("CHOMP_FIELD"));
        Assert.assertEquals("abc", transformed.get("DUMMY FIELD"));
    }

    @Test
    public void testGoodFieldNameLowerCase() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationFieldLowerCase));
        Assert.assertTrue(transformation != null);

        log.put("TestInGLoweRcase1", true);
        log.put("TestInGLoweRcase2", "true");
        log.put("TestInGLoweRcase3", 1);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(8, transformed.size());
        Assert.assertEquals(true, transformed.get("testinglowercase1"));
        Assert.assertEquals("true", transformed.get("testinglowercase2"));
        Assert.assertEquals(1, transformed.get("testinglowercase3"));

        Assert.assertEquals(12345, transformed.get("timestamp"));
        Assert.assertEquals("true", transformed.get("test field a"));
        Assert.assertEquals("   message     ", transformed.get("trim_field"));
        Assert.assertEquals("message\n", transformed.get("chomp_field"));
        Assert.assertEquals("abc", transformed.get("dummy field"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongFilter1() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationFilter);
        specification.getAttributes().setMessageFilter(null);
        factory.create(specification);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongFilter2() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationFilter);
        specification.getAttributes().getMessageFilter().getMatchers().get(0).setFieldName(null);
        factory.create(specification);
    }

    @Test
    public void testGoodLowercaseValue() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationLowerCase));
        Assert.assertTrue(transformation != null);
        log.put("chomp_field", "LowerCaseTest");

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("lowercasetest", transformed.get("chomp_field"));
        Assert.assertEquals(12345, transformed.get("timestamp"));
    }

    @Test
    public void testGoodUpperCaseValue() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationUpperCase));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("MESSAGE\n", transformed.get("chomp_field"));
        Assert.assertEquals(12345, transformed.get("timestamp"));
    }

}
