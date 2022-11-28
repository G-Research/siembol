package uk.co.gresearch.siembol.configeditor.configinfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfoType;

import java.util.ArrayList;

public class TestCaseConfigInfoProviderTest {
    private final String testCase = """
             {
               "test_specification": {
                 "secret": true,
                 "version": 1
               },
               "assertions": [
                 {
                   "assertion_type": "path_and_value_matches",
                   "json_path": "$.a",
                   "expected_pattern": "^.*mp$",
                   "negated_pattern": false,
                   "description": "match string",
                   "active": true
                 },
                 {
                   "assertion_type": "only_if_path_exists",
                   "json_path": "s",
                   "expected_pattern": "secret",
                   "negated_pattern": true,
                   "description": "skipped assertion",
                   "active": false
                 }
               ],
               "test_case_name": "test_case",
               "version": 12345,
               "author": "john",
               "config_name": "syslog",
               "description": "unitest test case"
             }
            """;

    private final String testCaseNew = """
             {
               "test_case_name": "test_case",
               "version": 0,
               "author": "john",
               "config_name": "syslog",
               "description": "unitest test case",
               "test_specification": {
                 "secret": true
               },
               "assertions": [
                 {
                   "assertion_type": "path_and_value_matches",
                   "json_path": "$.a",
                   "expected_pattern": "^.*mp$",
                   "negated_pattern": false,
                   "description": "match string",
                   "active": true
                 },
                 {
                   "assertion_type": "only_if_path_exists",
                   "json_path": "s",
                   "expected_pattern": "secret",
                   "negated_pattern": true,
                   "description": "skipped assertion",
                   "active": false
                 }
               ]
             }
            """;

    private final String maliciousTestCase = """
             {
               "test_case_name": "./../../test",
               "version": 1,
               "author": "john",
               "config_name": "syslog",
               "description": "unitest test case",
               "test_specification": {
                 "secret": true
               },
               "assertions": [
                 {
                   "assertion_type": "path_and_value_matches",
                   "json_path": "$.a",
                   "expected_pattern": "^.*mp$",
                   "negated_pattern": false,
                   "description": "match string",
                   "active": true
                 },
                 {
                   "assertion_type": "only_if_path_exists",
                   "json_path": "s",
                   "expected_pattern": "secret",
                   "negated_pattern": true,
                   "description": "skipped assertion",
                   "active": false
                 }
               ]
             }
            """;

    private final TestCaseInfoProvider infoProvider = new TestCaseInfoProvider();
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
    public void testCaseTestChangeAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(steve, testCase);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals(12346, info.getVersion());
        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals("Updating test case: syslog-test_case to version: 12346", info.getCommitMessage());

        Assert.assertEquals("steve", info.getCommitter());
        Assert.assertEquals(steve.getEmail(), info.getCommitterEmail());

        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("syslog-test_case.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("syslog-test_case.json").get().indexOf("\"version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("syslog-test_case.json").get().indexOf("\"author\": \"steve\",") > 0);
        Assert.assertFalse(info.isNewConfig());
        Assert.assertEquals(ConfigInfoType.TEST_CASE, info.getConfigInfoType());
    }

    @Test
    public void testCaseTestTestUnchangedAuthor() {
        ConfigInfo info = infoProvider.getConfigInfo(john, testCase);
        Assert.assertEquals(12345, info.getOldVersion());
        Assert.assertEquals("john", info.getCommitter());
        Assert.assertEquals("Updating test case: syslog-test_case to version: 12346", info.getCommitMessage());
        Assert.assertEquals("john@secret.net", info.getCommitterEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("syslog-test_case.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("syslog-test_case.json").get().indexOf("\"version\": 12346,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("syslog-test_case.json").get().indexOf("\"author\": \"john\",") > 0);
        Assert.assertFalse(info.isNewConfig());
        Assert.assertEquals(ConfigInfoType.TEST_CASE, info.getConfigInfoType());
    }

    @Test
    public void testCaseNew() {
        ConfigInfo info = infoProvider.getConfigInfo(steve, testCaseNew);
        Assert.assertEquals(info.getOldVersion(), 0);
        Assert.assertEquals(info.getCommitter(), steve.getUserName());
        Assert.assertEquals(info.getCommitMessage(), "Adding new test case: syslog-test_case");
        Assert.assertEquals(info.getCommitterEmail(), steve.getEmail());
        Assert.assertEquals(1, info.getFilesContent().size());
        Assert.assertTrue(info.getFilesContent().containsKey("syslog-test_case.json"));
        Assert.assertTrue(info.getFilesContent()
                .get("syslog-test_case.json").get().indexOf("\"version\": 1,") > 0);
        Assert.assertTrue(info.getFilesContent()
                .get("syslog-test_case.json").get().indexOf("\"author\": \"steve\",") > 0);
        Assert.assertTrue(info.isNewConfig());
        Assert.assertEquals(ConfigInfoType.TEST_CASE, info.getConfigInfoType());
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testCaseWrongJson() {
        infoProvider.getConfigInfo(john, "WRONG JSON");
    }


    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testCaseWrongMissingMetadata() {
        infoProvider.getConfigInfo(john, maliciousTestCase);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testCaseWrongMissingMetadata2() {
        infoProvider.getConfigInfo(john, testCase.replace("config_name", "undefined"));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testCaseWrongUser() {
        infoProvider.getConfigInfo(new UserInfo(), testCase);
    }

    @Test(expected = java.lang.UnsupportedOperationException.class)
    public void testCaseReleaseTest() {
        infoProvider.getReleaseInfo(steve, "");
    }

    @Test
    public void testCasefilterTest() {
        Assert.assertTrue(infoProvider.isStoreFile("abc.json"));
        Assert.assertFalse(infoProvider.isStoreFile("json.txt"));
    }

    @Test(expected = java.lang.UnsupportedOperationException.class)
    public void rulesVersionTest() {
        infoProvider.getReleaseVersion(new ArrayList<>());
    }
}

