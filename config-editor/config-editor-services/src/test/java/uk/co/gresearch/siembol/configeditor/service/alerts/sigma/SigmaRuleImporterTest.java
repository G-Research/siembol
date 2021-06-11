package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.alerts.model.MatcherTypeDto;
import uk.co.gresearch.siembol.alerts.model.RuleDto;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class SigmaRuleImporterTest {
    private static final ObjectReader ALERTING_RULE_READER = new ObjectMapper().readerFor(RuleDto.class);

    /**
     * {
     *   "field_mapping": [
     *     {
     *       "sigma_field": "sigma_user",
     *       "siembol_field": "siembol_user"
     *     }
     *   ],
     *   "rule_metadata_mapping": {
     *     "rule_name": "based_on_${title}",
     *     "rule_description": "generated from ${description} and id: ${id}",
     *     "source_type": "secret_data",
     *     "tags": [
     *       {
     *         "tag_name": "sigma_tags",
     *         "tag_value": "${tags}"
     *       }
     *     ]
     *   }
     * }
     **/
    @Multiline
    private static String importerAttributes;

    /**
     * title: Sigma Title
     * id: d06be400-8045-4200-0067-740a2009db25
     * status: experimental
     * description: Detects secret
     * references:
     *     - https://github.com/siembol
     * author: Joe
     * date: 2021/10/09
     * logsource:
     *     category: process_creation
     *     product: windows
     * detection:
     *     image_path:
     *         Image|endswith: 'secret.exe'
     *     cmd_s:
     *         CommandLine|contains: '/S'
     *     cmd_c:
     *          CommandLine|contains: '/C'
     *     net_utility:
     *         Image|endswith:
     *             - '\net.exe'
     *             - '\net1.exe'
     *         CommandLine|contains:
     *             - ' user '
     *             - ' use '
     *             - ' group '
     *     condition: image_path and cmd_c and (cmd_s or not net_utility)
     * fields:
     *     - CommandLine
     * falsepositives:
     *     - Unknown
     * level: medium
     * tags:
     *     - attack.defense_evasion
     *     - attack.example
     */
    @Multiline
    private static String sigmaRuleExample;


    private SigmaRuleImporter importer;
    private UserInfo userInfo;

    @Before
    public void Setup() throws Exception {
        importer = new SigmaRuleImporter.Builder().build();
        userInfo = new UserInfo();
        userInfo.setEmail("siembol@siembol");
        userInfo.setUserName("siembol");
    }

    @Test
    public void getImporterAttributes() {
        ConfigEditorResult result = importer.getImporterAttributesSchema();
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getConfigImporterAttributesSchema());
    }

    @Test
    public void validateAttributesOK() {
        ConfigEditorResult result = importer.validateImporterAttributes(importerAttributes);
        Assert.assertEquals(OK, result.getStatusCode());
    }

    @Test
    public void validateAttributesInvalidJson() {
        ConfigEditorResult result = importer.validateImporterAttributes("INVALID");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void validateAttributesMissingRequired() {
        ConfigEditorResult result = importer.validateImporterAttributes(
                importerAttributes.replace("source_type", "uknown"));
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void importConfig() throws JsonProcessingException {
        ConfigEditorResult result = importer.importConfig(userInfo, importerAttributes, sigmaRuleExample);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getImportedConfiguration());

        RuleDto rule = ALERTING_RULE_READER.readValue(result.getAttributes().getImportedConfiguration());
        Assert.assertEquals("siembol", rule.getRuleAuthor());
        Assert.assertEquals("based_on_Sigma_Title", rule.getRuleName());
        Assert.assertEquals("generated from Detects secret and id: d06be400-8045-4200-0067-740a2009db25",
                rule.getRuleDescription());
        Assert.assertEquals(0, rule.getRuleVersion());
        Assert.assertEquals(3, rule.getMatchers().size());
        Assert.assertEquals("secret_data", rule.getSourceType());
        Assert.assertEquals("secret_data", rule.getSourceType());
        Assert.assertEquals(1, rule.getTags().size());
        Assert.assertEquals("sigma_tags", rule.getTags().get(0).getTagName());
        Assert.assertEquals("[attack.defense_evasion, attack.example]", rule.getTags().get(0).getTagValue());

        Assert.assertEquals("Image", rule.getMatchers().get(0).getField());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, rule.getMatchers().get(0).getType());
        Assert.assertFalse(rule.getMatchers().get(0).getNegated());
        Assert.assertEquals(".*\\Qsecret.exe\\E$", rule.getMatchers().get(0).getData());

        Assert.assertEquals("CommandLine", rule.getMatchers().get(1).getField());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, rule.getMatchers().get(1).getType());
        Assert.assertFalse(rule.getMatchers().get(1).getNegated());
        Assert.assertEquals(".*\\Q/C\\E.*", rule.getMatchers().get(1).getData());

        Assert.assertNull(rule.getMatchers().get(2).getField());
        Assert.assertEquals(MatcherTypeDto.COMPOSITE_OR, rule.getMatchers().get(2).getType());
        Assert.assertFalse(rule.getMatchers().get(2).getNegated());
        Assert.assertNull(rule.getMatchers().get(2).getData());
        Assert.assertEquals(2, rule.getMatchers().get(2).getMatchers().size());

        Assert.assertEquals("CommandLine",  rule.getMatchers().get(2).getMatchers().get(0).getField());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH, rule.getMatchers().get(2).getMatchers().get(0).getType());
        Assert.assertFalse(rule.getMatchers().get(2).getMatchers().get(0).getNegated());
        Assert.assertEquals(".*\\Q/S\\E.*", rule.getMatchers().get(2).getMatchers().get(0).getData());

        Assert.assertNull(rule.getMatchers().get(2).getMatchers().get(1).getField());
        Assert.assertEquals(MatcherTypeDto.COMPOSITE_AND, rule.getMatchers().get(2).getMatchers().get(1).getType());
        Assert.assertTrue(rule.getMatchers().get(2).getMatchers().get(1).getNegated());
        Assert.assertNull(rule.getMatchers().get(2).getMatchers().get(1).getData());
        Assert.assertEquals(2, rule.getMatchers().get(2).getMatchers().get(1).getMatchers().size());

        Assert.assertEquals("Image",
                rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(0).getField());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH,
                rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(0).getType());
        Assert.assertFalse(rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(0).getNegated());
        Assert.assertEquals(".*\\Q\\net.exe\\E$|.*\\Q\\net1.exe\\E$",
                rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(0).getData());

        Assert.assertEquals("CommandLine",
                rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(1).getField());
        Assert.assertEquals(MatcherTypeDto.REGEX_MATCH,
                rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(1).getType());
        Assert.assertFalse(rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(1).getNegated());
        Assert.assertEquals(".*\\Q user \\E.*|.*\\Q use \\E.*|.*\\Q group \\E.*",
                rule.getMatchers().get(2).getMatchers().get(1).getMatchers().get(1).getData());

    }

    @Test
    public void importConfigInvalidAttributes() {
        ConfigEditorResult result = importer.importConfig(userInfo,
                importerAttributes.replace("rule_metadata_mapping", "unknown"),
                sigmaRuleExample);
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getMessage());
    }

    @Test
    public void importConfigInvalidConfig() {
        ConfigEditorResult result = importer.importConfig(userInfo,
                importerAttributes,
                "INVALID");
        Assert.assertEquals(BAD_REQUEST, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getException());
    }
}
