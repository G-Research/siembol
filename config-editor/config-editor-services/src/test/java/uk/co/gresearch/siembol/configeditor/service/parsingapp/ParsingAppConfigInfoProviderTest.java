package uk.co.gresearch.siembol.configeditor.service.parsingapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoProvider;

public class ParsingAppConfigInfoProviderTest {

    private final String simpleSingleApplicationParser = """
            {
               "parsing_app_name": "test",
               "parsing_app_version": 12345,
               "parsing_app_author": "dummy",
               "parsing_app_description": "Description of parser application",
               "parsing_app_settings": {
                 "input_topics": [
                   "secret"
                 ],
                 "error_topic": "error",
                 "input_parallelism": 1,
                 "parsing_parallelism": 2,
                 "output_parallelism": 3,
                 "parsing_app_type": "single_parser"
               },
               "parsing_settings": {
                 "single_parser": {
                   "parser_name": "single",
                   "output_topic": "output"
                 }
               }
            }
            """;

    private final String simpleSingleApplicationParserNew = """
            {
               "parsing_app_name": "test",
               "parsing_app_version": 0,
               "parsing_app_author": "dummy",
               "parsing_app_description": "Description of parser application",
               "parsing_app_settings": {
                 "input_topics": [
                   "secret"
                 ],
                 "error_topic": "error",
                 "input_parallelism": 1,
                 "parsing_parallelism": 2,
                 "output_parallelism": 3,
                 "parsing_app_type": "single_parser"
               },
               "parsing_settings": {
                 "single_parser": {
                   "parser_name": "single",
                   "output_topic": "output"
                 }
               }
             }
            """;

    private final String release = """
            {
             		"parsing_applications_version" : 1,
             		"parsing_applications" : [
                            {
             			"parsing_app_name": "test",
             			"parsing_app_version": 12345,
             			"parsing_app_author": "dummy",
             			"parsing_app_description": "Description of parser application",
             			"parsing_app_settings": {
             			"input_topics": [
             				"secret"
             			],
             			"error_topic": "error",
             			"input_parallelism": 1,
             			"parsing_parallelism": 2,
             			"output_parallelism": 3,
             			"parsing_app_type": "single_parser"
                        },
             			"parsing_settings": {
             				"single_parser": {
             				"parser_name": "single",
             				"output_topic": "output"
                            }
                        }
                    }
                   ]
            }
            """;

    static String user = "unknown@secret.net";
    private final ConfigInfoProvider infoProvider = ParsingAppConfigInfoProvider.create();
    private UserInfo unknown;
    private UserInfo dummy;

    @Before
    public void setUp() {
        unknown = new UserInfo();
        unknown.setUserName("unknown");
        unknown.setEmail("unknown@secret.net");

        dummy = new UserInfo();
        dummy.setUserName("dummy");
        dummy.setEmail("dummy@secret.net");
    }

    @Test
    public void configInfoTestChangeAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(unknown, simpleSingleApplicationParser);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals(12346, info.getVersion());
        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals("Updating configuration: test to version: 12346", info.getCommitMessage());

        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals(user, info.getCommitterEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test.json"));
        Assert.assertTrue(info.getFilesContent().get("test.json").isPresent());
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").get().indexOf("\"parsing_app_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").get().indexOf("\"parsing_app_author\": \"unknown\",") > 0);
        Assert.assertFalse(info.isNewConfig());
    }

    @Test
    public void configInfoTestUnchangedAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(dummy, simpleSingleApplicationParser);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals("dummy", info.getCommitter());
        Assert.assertEquals("Updating configuration: test to version: 12346", info.getCommitMessage());
        Assert.assertEquals("dummy@secret.net", info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test.json"));
        Assert.assertTrue(info.getFilesContent().get("test.json").isPresent());
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").get().indexOf("\"parsing_app_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").get().indexOf("\"parsing_app_author\": \"dummy\",") > 0);
        Assert.assertFalse(info.isNewConfig());
    }

    @Test
    public void configInfoNewRule() {
        ConfigInfo info = infoProvider.getConfigInfo(unknown, simpleSingleApplicationParserNew);
        Assert.assertEquals(0, info.getOldVersion());
        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals("Adding new configuration: test", info.getCommitMessage());
        Assert.assertEquals(user, info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test.json"));
        Assert.assertTrue(info.getFilesContent().get("test.json").isPresent());
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").get().indexOf("\"parsing_app_version\": 1,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").get().indexOf("\"parsing_app_author\": \"unknown\",") > 0);
        Assert.assertTrue(info.isNewConfig());
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void configInfoWrongJson() {
        infoProvider.getConfigInfo(new UserInfo(), "WRONG JSON");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void configInfoWrongUser() {
        infoProvider.getConfigInfo(new UserInfo(), simpleSingleApplicationParser);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void releaseInfoWrongUser() {
        infoProvider.getReleaseInfo(new UserInfo(), simpleSingleApplicationParser);
    }

    @Test
    public void releaseTest() {
        ConfigInfo info = infoProvider.getReleaseInfo(unknown, release.trim());

        Assert.assertEquals(1, info.getOldVersion());
        Assert.assertEquals(2, info.getVersion());
        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals("Configurations released to version: 2", info.getCommitMessage());

        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals(user, info.getCommitterEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("parsing_applications.json"));
        Assert.assertTrue(info.getFilesContent().get("parsing_applications.json").isPresent());
        Assert.assertTrue(info.getFilesContent()
                .get("parsing_applications.json").get().indexOf("\"parsing_applications_version\": 2,") > 0);
    }

    @Test
    public void filterRulesTest() {
        Assert.assertFalse(infoProvider.isReleaseFile("a.json"));
        Assert.assertTrue(infoProvider.isReleaseFile("parsing_applications.json"));
        Assert.assertTrue(infoProvider.isStoreFile("abc.json"));
        Assert.assertFalse(infoProvider.isStoreFile("json.txt"));
    }
}
