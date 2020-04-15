package uk.co.gresearch.siembol.configeditor.service.parsingapp;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.configstore.ConfigInfoProvider;

public class ParsingAppConfigInfoProviderTest {

    /**
     *{
     *   "parsing_app_name": "test",
     *   "parsing_app_version": 12345,
     *   "parsing_app_author": "dummy",
     *   "parsing_app_description": "Description of parser application",
     *   "parsing_app_settings": {
     *     "input_topics": [
     *       "secret"
     *     ],
     *     "error_topic": "error",
     *     "input_parallelism": 1,
     *     "parsing_parallelism": 2,
     *     "output_parallelism": 3,
     *     "parsing_app_type": "single_parser"
     *   },
     *   "parsing_settings": {
     *     "single_parser": {
     *       "parser_name": "single",
     *       "output_topic": "output"
     *     }
     *   }
     * }
     **/
    @Multiline
    static String simpleSingleApplicationParser;

    /**
     *{
     *   "parsing_app_name": "test",
     *   "parsing_app_version": 0,
     *   "parsing_app_author": "dummy",
     *   "parsing_app_description": "Description of parser application",
     *   "parsing_app_settings": {
     *     "input_topics": [
     *       "secret"
     *     ],
     *     "error_topic": "error",
     *     "input_parallelism": 1,
     *     "parsing_parallelism": 2,
     *     "output_parallelism": 3,
     *     "parsing_app_type": "single_parser"
     *   },
     *   "parsing_settings": {
     *     "single_parser": {
     *       "parser_name": "single",
     *       "output_topic": "output"
     *     }
     *   }
     * }
     **/
    @Multiline
    static String simpleSingleApplicationParserNew;

    /**
     *{
     * 		"parsing_applications_version" : 1,
     * 		"parsing_applications" : [
     *                {
     * 			"parsing_app_name": "test",
     * 			"parsing_app_version": 12345,
     * 			"parsing_app_author": "dummy",
     * 			"parsing_app_description": "Description of parser application",
     * 			"parsing_app_settings": {
     * 			"input_topics": [
     * 				"secret"
     * 			],
     * 			"error_topic": "error",
     * 			"input_parallelism": 1,
     * 			"parsing_parallelism": 2,
     * 			"output_parallelism": 3,
     * 			"parsing_app_type": "single_parser"
     *            },
     * 			"parsing_settings": {
     * 				"single_parser": {
     * 				"parser_name": "single",
     * 				"output_topic": "output"
     *                }
     *            }
     *        }
     *       ]
     * }
     *
     **/
    @Multiline
    static String release;


    static String user = "unknown@secret.net";
    private final ConfigInfoProvider infoProvider = ParsingAppConfigInfoProvider.create();

    @Test
    public void ConfigInfoTestChangeAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(user, simpleSingleApplicationParser);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals(12346, info.getVersion());
        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals("Updating configuration: test to version: 12346", info.getCommitMessage());

        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals(user, info.getCommitterEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").indexOf("\"parsing_app_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").indexOf("\"parsing_app_author\": \"unknown\",") > 0);
        Assert.assertFalse(info.isNewConfig());
    }

    @Test
    public void ConfigInfoTestUnchangedAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo("dummy@secret.net", simpleSingleApplicationParser);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals("dummy", info.getCommitter());
        Assert.assertEquals("Updating configuration: test to version: 12346", info.getCommitMessage());
        Assert.assertEquals("dummy@secret.net", info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").indexOf("\"parsing_app_version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").indexOf("\"parsing_app_author\": \"dummy\",") > 0);
        Assert.assertFalse(info.isNewConfig());
    }

    @Test
    public void ConfigInfoNewRule() {
        ConfigInfo info = infoProvider.getConfigInfo(user, simpleSingleApplicationParserNew);
        Assert.assertEquals(0, info.getOldVersion());
        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals("Adding new configuration: test", info.getCommitMessage());
        Assert.assertEquals(user, info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("test.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").indexOf("\"parsing_app_version\": 1,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("test.json").indexOf("\"parsing_app_author\": \"unknown\",") > 0);
        Assert.assertTrue(info.isNewConfig());
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ConfigInfoWrongJson() {
        infoProvider.getConfigInfo(user, "WRONG JSON");
    }


    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ConfigInfoWrongUser() {
        infoProvider.getConfigInfo("INVALID", simpleSingleApplicationParser);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void ReleaseInfoWrongUser() { 
        infoProvider.getReleaseInfo("INVALID", simpleSingleApplicationParser);
    }

    @Test
    public void ReleaseTest() {
        ConfigInfo info = infoProvider.getReleaseInfo("unknown@secret.net", release.trim());

        Assert.assertEquals(1, info.getOldVersion());
        Assert.assertEquals(2, info.getVersion());
        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals("Configurations released to version: 2", info.getCommitMessage());

        Assert.assertEquals("unknown", info.getCommitter());
        Assert.assertEquals(user, info.getCommitterEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("parsing_applications.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("parsing_applications.json").indexOf("\"parsing_applications_version\": 2,") > 0);
    }

    @Test
    public void FilterRulesTest() {
        Assert.assertFalse(infoProvider.isReleaseFile("a.json"));
        Assert.assertTrue(infoProvider.isReleaseFile("parsing_applications.json"));
        Assert.assertTrue(infoProvider.isStoreFile("abc.json"));
        Assert.assertFalse(infoProvider.isStoreFile("json.txt"));
    }
}
