package uk.co.gresearch.siembol.enrichments.evaluation;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.enrichments.common.EnrichmentCommand;
import uk.co.gresearch.siembol.alerts.common.EvaluationResult;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.alerts.engine.BasicMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EnrichingRuleTest {
    private String name = "test_rule";
    private Integer version = 1;
    private String tableName = "test_table";
    private String key = "test_key";

    private Map<String, Object> event;
    private BasicMatcher matcher;
    List<Pair<String, String>> enrichmentFields;
    List<Pair<String, String>> enrichmentTags;
    private EnrichingRule rule;

    @Before
    public void setUp() {
        enrichmentFields = new ArrayList<>();
        enrichmentFields.add(Pair.of("table_field", "event_field"));
        enrichmentTags = new ArrayList<>();
        enrichmentTags.add(Pair.of("is_test", "true"));
        matcher = Mockito.mock(BasicMatcher.class);
        event = new HashMap<>();
        when(matcher.match(ArgumentMatchers.<Map<String, Object>>any())).thenReturn(EvaluationResult.MATCH);
    }

    @Test
    public void metadataOK() {
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .enrichmentTags(enrichmentTags)
                .key(key)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();

        String ruleName = rule.getRuleName();
        String fullRuleName = rule.getFullRuleName();

        Assert.assertEquals(name, ruleName);
        Assert.assertEquals(name + "_v1", fullRuleName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void metadataMissingTags() {
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .key(key)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void metadataMissingKey() {
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .enrichmentTags(enrichmentTags)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void metadataMissingTableName() {
        rule = EnrichingRule.enrichingRuleBuilder()
                .key(key)
                .enrichmentTags(enrichmentTags)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();
    }

    @Test
    public void matchOK() {
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .enrichmentTags(enrichmentTags)
                .enrichmentFields(enrichmentFields)
                .key(key)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();

        AlertingResult ret = rule.match(event);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertNotNull(ret.getAttributes().getEvent());
        EnrichmentCommand command = (EnrichmentCommand)ret.getAttributes().getEvent()
                .get(EnrichmentFields.ENRICHMENT_COMMAND.toString());
        Assert.assertNotNull(command);
        Assert.assertEquals(tableName, command.getTableName());
        Assert.assertEquals(key, command.getKey());
        Assert.assertEquals(1, command.getTags().size());
        Assert.assertEquals("is_test", command.getTags().get(0).getKey());
        Assert.assertEquals("true", command.getTags().get(0).getValue());
        Assert.assertEquals("table_field", command.getEnrichmentFields().get(0).getKey());
        Assert.assertEquals("event_field", command.getEnrichmentFields().get(0).getValue());
    }

    @Test
    public void matchOkKeySubstitution() {
        key = "${host}";
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .enrichmentTags(enrichmentTags)
                .key(key)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();

        event.put("host", "dummy_host");
        AlertingResult ret = rule.match(event);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.MATCH, ret.getAttributes().getEvaluationResult());
        Assert.assertNotNull(ret.getAttributes().getEvent());
        EnrichmentCommand command = (EnrichmentCommand)ret.getAttributes().getEvent()
                .get(EnrichmentFields.ENRICHMENT_COMMAND.toString());
        Assert.assertNotNull(command);
        Assert.assertEquals(tableName, command.getTableName());
        Assert.assertEquals("dummy_host", command.getKey());
        Assert.assertEquals(1, command.getTags().size());
        Assert.assertEquals("is_test", command.getTags().get(0).getKey());
        Assert.assertEquals("true", command.getTags().get(0).getValue());
    }

    @Test
    public void matchFailedKeySubstitution() {
        key = "${host}";
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .enrichmentTags(enrichmentTags)
                .key(key)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();

        AlertingResult ret = rule.match(event);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.NO_MATCH, ret.getAttributes().getEvaluationResult());
    }

    @Test
    public void matchErrorExistingField() {
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .enrichmentTags(enrichmentTags)
                .key(key)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();

        event.put(EnrichmentFields.ENRICHMENT_COMMAND.toString(), "dummy");
        AlertingResult ret = rule.match(event);
        Assert.assertEquals(AlertingResult.StatusCode.ERROR, ret.getStatusCode());
    }

    @Test
    public void ruleMatchNoMatch() {
        rule = EnrichingRule.enrichingRuleBuilder()
                .tableName(tableName)
                .enrichmentTags(enrichmentTags)
                .key(key)
                .matchers(Arrays.asList(matcher))
                .name(name)
                .version(version)
                .build();

        when(matcher.match(any())).thenReturn(EvaluationResult.NO_MATCH);
        AlertingResult ret = rule.match(event);
        Assert.assertEquals(AlertingResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(EvaluationResult.NO_MATCH, ret.getAttributes().getEvaluationResult());
    }
}