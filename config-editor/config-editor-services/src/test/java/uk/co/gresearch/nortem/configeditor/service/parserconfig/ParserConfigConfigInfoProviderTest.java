package uk.co.gresearch.nortem.configeditor.service.parserconfig;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.nortem.configeditor.configstore.ConfigInfoProvider;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.nortem.configeditor.configstore.ConfigInfo;

import java.util.ArrayList;
import java.util.List;

public class ParserConfigConfigInfoProviderTest {
    /**
     * {
     *   "parser_name": "test_parser",
     *   "parser_author": "john",
     *   "parser_version": 12345,
     *   "parser_config": {
     *     "parser_attributes": {
     *       "parser_type": "syslog",
     *       "syslog_config": {
     *         "syslog_version": "RFC_3164",
     *         "timezone": "UTC"
     *       }
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String testParser;
    /**
     * {
     *   "parser_name": "test_parser",
     *   "parser_author": "john",
     *   "parser_version": 0,
     *   "parser_config": {
     *     "parser_attributes": {
     *       "parser_type": "syslog",
     *       "syslog_config": {
     *         "syslog_version": "RFC_3164",
     *         "timezone": "UTC"
     *       }
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String testNewParser;

    /**
     * {
     *   "parsers_version" : 1,
     *   "parser_configurations": [
     *   {
     *   "parser_name": "test_parser",
     *   "parser_author": "john",
     *   "parser_version": 1,
     *   "parser_config": {
     *     "parser_attributes": {
     *       "parser_type": "syslog",
     *       "syslog_config": {
     *         "syslog_version": "RFC_3164",
     *         "timezone": "UTC"
     *       }
     *     }
     *   }
     *  }]
     * }
     **/
    @Multiline
    public static String release;

    /**
     * {
     *   "parser_name": "../../../test_parser",
     *   "parser_author": "john",
     *   "parser_version": 12345,
     *   "parser_config": {
     *     "parser_attributes": {
     *       "parser_type": "syslog",
     *       "syslog_config": {
     *         "syslog_version": "RFC_3164",
     *         "timezone": "UTC"
     *       }
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String maliciousConfig;

    public static String user = "steve@secret.net";
    private final ConfigInfoProvider infoProvider = ParserConfigConfigInfoProvider.create();

    @Test
    public void ConfigInfoTestChangeAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(user, testParser);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals(12346, info.getVersion());
        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals("Updating configuration: test_parser to version: 12346", info.getCommitMessage());

        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals(info.getCommitterEmail(), user);

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test_parser.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("test_parser.json").indexOf("\"parser_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test_parser.json").indexOf("\"parser_author\": \"steve\",") > 0);
        Assert.assertFalse(info.isNewConfig());
    }

    @Test
    public void ConfigInfoTestUnchangedAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo("john@secret.net", testParser);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals("john", info.getCommitter());
        Assert.assertEquals("Updating configuration: test_parser to version: 12346", info.getCommitMessage());
        Assert.assertEquals("john@secret.net", info.getCommitterEmail());
        Assert.assertEquals( 1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test_parser.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("test_parser.json").indexOf("\"parser_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test_parser.json").indexOf("\"parser_author\": \"john\",") > 0);
        Assert.assertFalse(info.isNewConfig());
    }

    @Test
    public void ConfigInfoNewRule() {
        ConfigInfo info = infoProvider.getConfigInfo(user, testNewParser);
        Assert.assertEquals(0, info.getOldVersion());
        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals("Adding new configuration: test_parser", info.getCommitMessage());
        Assert.assertEquals(user, info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test_parser.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("test_parser.json").indexOf("\"parser_version\": 1,") > 0);
        Assert.assertTrue(info.isNewConfig());
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ConfigInfoWrongJson() {
        infoProvider.getConfigInfo(user,"WRONG JSON");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ConfigInfoWrongMissingMetadata() {
        infoProvider.getConfigInfo(user, maliciousConfig);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ConfigInfoWrongUser() {
        infoProvider.getConfigInfo("INVALID", testParser);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ReleaseInfoWrongUser() {
        infoProvider.getReleaseInfo("INVALID", testParser);
    }

    @Test
    public void ReleaseTest() {
        ConfigInfo info = infoProvider.getReleaseInfo("steve@secret.net", release);

        Assert.assertEquals(1, info.getOldVersion());
        Assert.assertEquals(2, info.getVersion());
        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals("Configuration released to version: 2", info.getCommitMessage());

        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals(user, info.getCommitterEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("parsers.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("parsers.json").indexOf("\"parsers_version\": 2,") > 0);

    }

    @Test
    public void FilterRulesTest() {
        Assert.assertFalse(infoProvider.isReleaseFile("a.json"));
        Assert.assertTrue(infoProvider.isReleaseFile("parsers.json"));
        Assert.assertTrue(infoProvider.isStoreFile("abc.json"));
        Assert.assertFalse(infoProvider.isStoreFile("json.txt"));
    }

    @Test
    public void RulesVersionTest() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("parsers.json", release, ConfigEditorFile.ContentType.RAW_JSON_STRING));
        int version = infoProvider.getReleaseVersion(files);
        Assert.assertEquals(1, version);
    }

    @Test(expected = IllegalArgumentException.class)
    public void RulesVersionTestMissingFile() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("a.json", release, ConfigEditorFile.ContentType.RAW_JSON_STRING));
        infoProvider.getReleaseVersion(files);
    }

    @Test(expected = IllegalArgumentException.class)
    public void RulesVersionMissingVersion() {
        List<ConfigEditorFile> files = new ArrayList<>();
        files.add(new ConfigEditorFile("parsers.json",
                "{}",
                ConfigEditorFile.ContentType.RAW_JSON_STRING));
        infoProvider.getReleaseVersion(files);
    }
}

