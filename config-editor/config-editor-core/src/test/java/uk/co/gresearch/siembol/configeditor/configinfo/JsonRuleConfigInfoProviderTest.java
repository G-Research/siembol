package uk.co.gresearch.siembol.configeditor.configinfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;

import java.util.ArrayList;
import java.util.List;

public class JsonRuleConfigInfoProviderTest {
    private final String testRule = """
            {
                "rule_name": "info_provider-test",
                "rule_author": "john",
                "rule_version": 12345,
                "rule_description": "Test rule",
                "enrichments": { },
                "actions": { }
            }
            """;

    private final String testNewRule = """
            {
                "rule_name": "info_provider_test",
                "rule_author": "john",
                "rule_version": 0,
                "rule_description": "Test rule",
                "enrichments": { },
                "actions": { }
            }
            """;

    private final String release = """
            {
              "rules_version" : 1,
              "rules": [{
                 "rule_name": "info_provider_test",
                 "rule_author": "mark",
                 "rule_version": 12,
                 "rule_description": "Test rule",
                 "enrichments": { },
                 "actions": { }
                 }]
            }
            """;

    private final String releaseWithDuplicateAfter = """
            {
              "rules_version" : 1,
              "rules": [{
                 "rule_name": "info_provider_test",
                 "rule_author": "mark",
                 "rule_version": 12,
                 "rule_description": "Test rule",
                 "enrichments": {
                    "rules_version" : 1
                 },
                 "actions": { }
                 
                 }]
            }
            """;

    private final String releaseWithDuplicateBefore = """
            {
              "rules": [{
                 "rule_name": "info_provider_test",
                 "rule_author": "mark",
                 "rule_version": 12,
                 "rule_description": "Test rule",
                 "enrichments": {
                    "rules_version" : 1
                 },
                 "actions": { }
                 
                 }],
              "rules_version" : 1
            }
            """;
    private final String releaseNoRules = """
            {
              "rules_version" : 1,
              "rules": []
            }
            """;

    private final String releaseThreeRules = """
            {
              "rules_version": 1,
              "rules": [
                {
                  "rule_name": "info_provider_test",
                  "rule_author": "mark",
                  "rule_version": 1,
                  "rule_description": "Test rule",
                  "enrichments": {},
                  "actions": {}
                },
                {
                  "rule_name": "info_provider_extra_test",
                  "rule_author": "mark",
                  "rule_version": 1,
                  "rule_description": "Test rule",
                  "enrichments": {},
                  "actions": {}
                },
                {
                  "rule_name": "secret_test",
                  "rule_author": "mark",
                  "rule_version": 1,
                  "rule_description": "Test rule",
                  "enrichments": {},
                  "actions": {}
                }
              ]
            }
            """;

    private final String maliciousRule = """
            {
                "rule_name": "../../../test",
                "rule_author": "steve",
                "rule_version": 12345,
                "rule_description": "Test rule",
                "enrichments": { },
                "actions": { }
            }
            """;

    private final String testRuleDuplicateMetadataFieldsAfter = """
            {
                "rule_name": "info_provider-test",
                "rule_author": "john",
                "rule_version": 12345,
                "rule_description": "Test rule",
                "enrichments": {
                    "rule_name": "duplicate_name",
                    "rule_author": "josh",
                    "rule_version": 1
                 },
                "actions": { }
            }
            """;

    private final String testRuleDuplicateMetadataFieldsBefore = """
            {
                "enrichments": {
                    "rule_name": "duplicate_name",
                    "rule_author": "josh",
                    "rule_version": 1
                 },
                "rule_name": "info_provider-test",
                "rule_author": "john",
                "rule_version": 12345,
                "rule_description": "Test rule",
                "actions": { }
            }
            """;
    private final ConfigInfoProvider infoProvider = JsonRuleConfigInfoProvider.create();

    private UserInfo steve;
    private UserInfo john;

    @Before
    public void setUp() {
        steve = new UserInfo();
        steve.setUserName("steve");
        steve.setEmail("steve@secret.net");

        john = new UserInfo();
        john.setUserName("john");
        john.setEmail("john@secret.net");
    }

    @Test
    public void ruleInfoTestChangeAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(steve, testRule);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals(12346, info.getVersion());
        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals("Updating rule: info_provider-test to version: 12346", info.getCommitMessage());

        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals(info.getCommitterEmail(), steve.getEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("info_provider-test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_author\": \"steve\",") > 0);
        Assert.assertFalse(info.isNewConfig());
        Assert.assertEquals(ConfigInfoType.RULE, info.getConfigInfoType());
    }

    @Test
    public void ruleInfoTestUnchangedAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(john, testRule);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals("john", info.getCommitter());
        Assert.assertEquals("Updating rule: info_provider-test to version: 12346", info.getCommitMessage());
        Assert.assertEquals("john@secret.net", info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("info_provider-test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_author\": \"john\",") > 0);
        Assert.assertFalse(info.isNewConfig());
        Assert.assertEquals(ConfigInfoType.RULE, info.getConfigInfoType());
    }

    @Test
    public void ruleInfoNewRule() {
        ConfigInfo info = infoProvider.getConfigInfo(steve, testNewRule);
        Assert.assertEquals(0, info.getOldVersion());
        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals("Adding new rule: info_provider_test", info.getCommitMessage());
        Assert.assertEquals(info.getCommitterEmail(), steve.getEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("info_provider_test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider_test.json").get().indexOf("\"rule_version\": 1,") > 0);
        Assert.assertTrue(info.isNewConfig());
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ruleInfoWrongJson() {
        infoProvider.getConfigInfo(steve, "WRONG JSON");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void RuleInfoWrongMissingMetadata() {
        infoProvider.getConfigInfo(john, maliciousRule);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ruleInfoWrongUser() {
        infoProvider.getConfigInfo(null, testRule);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void releaseInfoWrongUser() {
        infoProvider.getReleaseInfo(new UserInfo(), testRule);
    }

    @Test
    public void releaseTest() {
        ConfigInfo info = infoProvider.getReleaseInfo(steve, release);

        Assert.assertEquals(info.getOldVersion(), 1);
        Assert.assertEquals(info.getVersion(), 2);
        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitMessage(), "Rules released to version: 2");

        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitterEmail(), steve.getEmail());

        Assert.assertEquals(info.getFilesContent().size(), 1);
        Assert.assertEquals(info.getFilesContent().containsKey("rules.json"), true);
        Assert.assertEquals(info.getFilesContent()
                .get("rules.json").get().indexOf("\"rules_version\": 2,") > 0, true);

    }

    @Test
    public void filterRulesTest() {
        Assert.assertEquals(infoProvider.isReleaseFile("a.json"), false);
        Assert.assertEquals(infoProvider.isReleaseFile("rules.json"), true);
        Assert.assertEquals(infoProvider.isStoreFile("abc.json"), true);
        Assert.assertEquals(infoProvider.isStoreFile("json.txt"), false);
    }

    @Test
    public void rulesVersionTest() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("rules.json", release, ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
        Assert.assertEquals(version, 1);
    }

    @Test
    public void rulesVersionTestMissingFile() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("a.json", release, ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
        Assert.assertEquals(0, version);
    }

    @Test
    public void rulesVersionMissingVersion() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("rules.json", "{}", ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
        Assert.assertEquals(0, version);
    }

    @Test
    public void rulesVersionInvalidJson() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("rules.json", "INVALID", ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
        Assert.assertEquals(0, version);
    }

    @Test
    public void configInRelease() {
        boolean isInRelease = infoProvider.isConfigInRelease(release, "info_provider_test");
        Assert.assertTrue(isInRelease);
    }

    @Test
    public void configNotInRelease() {
        boolean isInRelease = infoProvider.isConfigInRelease(release, "info_provider");
        Assert.assertFalse(isInRelease);
    }

    @Test
    public void configNotInReleaseNoRules() {
        boolean isInRelease = infoProvider.isConfigInRelease(releaseNoRules, "info_provider");
        Assert.assertFalse(isInRelease);
    }

    @Test
    public void configInReleaseWithThreeRules() {
        boolean isInRelease = infoProvider.isConfigInRelease(releaseThreeRules, "secret_test");
        Assert.assertTrue(isInRelease);
    }

    @Test
    public void configNotInReleaseWithThreeRules() {
        boolean isInRelease = infoProvider.isConfigInRelease(releaseThreeRules, "info_provider");
        Assert.assertFalse(isInRelease);
    }

    @Test
    public void ruleInfoTestUnchangedAuthorDuplicateFieldsBefore() {
        ConfigInfo info = infoProvider.getConfigInfo(john, testRuleDuplicateMetadataFieldsBefore);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals("john", info.getCommitter());
        Assert.assertEquals("Updating rule: info_provider-test to version: 12346", info.getCommitMessage());
        Assert.assertEquals("john@secret.net", info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("info_provider-test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_author\": \"john\",") > 0);
        Assert.assertFalse(info.isNewConfig());
        Assert.assertEquals(ConfigInfoType.RULE, info.getConfigInfoType());
    }

    @Test
    public void ruleInfoTestUnchangedAuthorDuplicateFieldsAfter() {
        ConfigInfo info = infoProvider.getConfigInfo(john, testRuleDuplicateMetadataFieldsAfter);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals("john", info.getCommitter());
        Assert.assertEquals("Updating rule: info_provider-test to version: 12346", info.getCommitMessage());
        Assert.assertEquals("john@secret.net", info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("info_provider-test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").get().indexOf("\"rule_author\": \"john\",") > 0);
        Assert.assertFalse(info.isNewConfig());
        Assert.assertEquals(ConfigInfoType.RULE, info.getConfigInfoType());
    }

    @Test
    public void releaseTestWithDuplicateAfter() {
        ConfigInfo info = infoProvider.getReleaseInfo(steve, releaseWithDuplicateAfter);

        Assert.assertEquals(info.getOldVersion(), 1);
        Assert.assertEquals(info.getVersion(), 2);
        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitMessage(), "Rules released to version: 2");

        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitterEmail(), steve.getEmail());

        Assert.assertEquals(info.getFilesContent().size(), 1);
        Assert.assertEquals(info.getFilesContent().containsKey("rules.json"), true);
        Assert.assertEquals(info.getFilesContent()
                .get("rules.json").get().indexOf("\"rules_version\": 2,") > 0, true);

    }

    @Test
    public void releaseTestWithDuplicateBefore() {
        ConfigInfo info = infoProvider.getReleaseInfo(steve, releaseWithDuplicateBefore);

        Assert.assertEquals(info.getOldVersion(), 1);
        Assert.assertEquals(info.getVersion(), 2);
        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitMessage(), "Rules released to version: 2");

        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitterEmail(), steve.getEmail());

        Assert.assertEquals(info.getFilesContent().size(), 1);
        Assert.assertEquals(info.getFilesContent().containsKey("rules.json"), true);
        Assert.assertEquals(info.getFilesContent()
                .get("rules.json").get().indexOf("\"rules_version\": 2") > 0, true);

    }
}
