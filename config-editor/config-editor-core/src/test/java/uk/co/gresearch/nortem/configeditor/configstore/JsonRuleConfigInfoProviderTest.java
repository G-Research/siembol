package uk.co.gresearch.nortem.configeditor.configstore;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorFile;

import java.util.ArrayList;
import java.util.List;

public class JsonRuleConfigInfoProviderTest {
    /**
     * {
     *     "rule_name": "info_provider-test",
     *     "rule_author": "john",
     *     "rule_version": 12345,
     *     "rule_description": "Test rule",
     *     "enrichments": { },
     *     "actions": { }
     * }
     **/
    @Multiline
    public static String testRule;
    /**
     * {
     *     "rule_name": "info_provider_test",
     *     "rule_author": "john",
     *     "rule_version": 0,
     *     "rule_description": "Test rule",
     *     "enrichments": { },
     *     "actions": { }
     * }
     **/
    @Multiline
    public static String testNewRule;

    /**
     * {
     *   "rules_version" : 1,
     *   "rules": [{
     *      "rule_name": "info_provider_test",
     *      "rule_author": "mark",
     *      "rule_version": 12,
     *      "rule_description": "Test rule",
     *      "enrichments": { },
     *      "actions": { }
     *      }]
     * }
     **/
    @Multiline
    public static String release;

    /**
     * {
     *     "rule_name": "../../../test",
     *     "rule_author": "steve",
     *     "rule_version": 12345,
     *     "rule_description": "Test rule",
     *     "enrichments": { },
     *     "actions": { }
     * }
     **/
    @Multiline
    public static String maliciousRule;

    public static String user = "steve@secret.net";
    private final ConfigInfoProvider infoProvider = JsonRuleConfigInfoProvider.create();

    @Test
    public void RuleInfoTestChangeAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(user, testRule);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals(12346, info.getVersion());
        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals("Updating rule: info_provider-test to version: 12346", info.getCommitMessage());

        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals(user, info.getCommitterEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("info_provider-test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").indexOf("\"rule_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("info_provider-test.json").indexOf("\"rule_author\": \"steve\",") > 0);
        Assert.assertFalse(info.isNewConfig());
    }

    @Test
    public void RuleInfoTestUnchangedAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo("john@secret.net", testRule);
        Assert.assertEquals(info.getOldVersion(), 12345);
        Assert.assertEquals(info.getCommitter(), "john");
        Assert.assertEquals(info.getCommitMessage(), "Updating rule: info_provider-test to version: 12346");
        Assert.assertEquals(info.getCommitterEmail(), "john@secret.net");
        Assert.assertEquals(info.getFilesContent().size(), 1);
        Assert.assertEquals(info.getFilesContent().containsKey("info_provider-test.json"), true);
        Assert.assertEquals(info.getFilesContent()
                .get("info_provider-test.json").indexOf("\"rule_version\": 12346,") > 0, true);
        Assert.assertEquals(info.getFilesContent()
                .get("info_provider-test.json").indexOf("\"rule_author\": \"john\",") > 0, true);
        Assert.assertEquals(info.isNewConfig(), false);

    }

    @Test
    public void RuleInfoNewRule() {
        ConfigInfo info = infoProvider.getConfigInfo(user, testNewRule);
        Assert.assertEquals(info.getOldVersion(), 0);
        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitMessage(), "Adding new rule: info_provider_test");
        Assert.assertEquals(info.getCommitterEmail(), user);
        Assert.assertEquals(info.getFilesContent().size(), 1);
        Assert.assertEquals(info.getFilesContent().containsKey("info_provider_test.json"), true);
        Assert.assertEquals(info.getFilesContent()
                .get("info_provider_test.json").indexOf("\"rule_version\": 1,") > 0, true);
        Assert.assertEquals(info.isNewConfig(), true);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void RuleInfoWrongJson() {
        ConfigInfo info = infoProvider.getConfigInfo(user,"WRONG JSON");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void RuleInfoWrongMissingMetadata() {
        ConfigInfo info = infoProvider.getConfigInfo(user, maliciousRule);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void RuleInfoWrongUser() {
        ConfigInfo info = infoProvider.getConfigInfo("INVALID", testRule);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ReleaseInfoWrongUser() {
        ConfigInfo info = infoProvider.getReleaseInfo("INVALID", testRule);
    }

    @Test
    public void ReleaseTest() {
        ConfigInfo info = infoProvider.getReleaseInfo("steve@secret.net", release);

        Assert.assertEquals(info.getOldVersion(), 1);
        Assert.assertEquals(info.getVersion(), 2);
        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitMessage(), "Rules released to version: 2");

        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitterEmail(), user);

        Assert.assertEquals(info.getFilesContent().size(), 1);
        Assert.assertEquals(info.getFilesContent().containsKey("rules.json"), true);
        Assert.assertEquals(info.getFilesContent()
                .get("rules.json").indexOf("\"rules_version\": 2,") > 0, true);

    }

    @Test
    public void FilterRulesTest() {
        Assert.assertEquals(infoProvider.isReleaseFile("a.json"), false);
        Assert.assertEquals(infoProvider.isReleaseFile("rules.json"), true);
        Assert.assertEquals(infoProvider.isStoreFile("abc.json"), true);
        Assert.assertEquals(infoProvider.isStoreFile("json.txt"), false);
    }

    @Test
    public void RulesVersionTest() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("rules.json", release, ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
        Assert.assertEquals(version, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void RulesVersionTestMissingFile() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("a.json", release, ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
    }

    @Test(expected = IllegalArgumentException.class)
    public void RulesVersionMissingVersion() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("rules.json", "{}", ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
    }
}
