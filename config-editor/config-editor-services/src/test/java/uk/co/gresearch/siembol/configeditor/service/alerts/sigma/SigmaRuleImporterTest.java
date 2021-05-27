package uk.co.gresearch.siembol.configeditor.service.alerts.sigma;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;

import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.BAD_REQUEST;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class SigmaRuleImporterTest {
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
     *     condition: image_path and cmd_c and cmd_s and net_utility
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
    public void importConfig() {
        ConfigEditorResult result = importer.importConfig(userInfo, importerAttributes, sigmaRuleExample);
        //Assert.assertEquals(OK, result.getStatusCode());
    }
}
