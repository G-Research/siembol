package uk.co.gresearch.siembol.configeditor.configinfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.gresearch.siembol.configeditor.common.UserInfo;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;

public class AdminConfigInfoProviderTest {
    private final String testConfig = """
            {
                "config_version": 1,
                "secret": "john",
                "object": { },
                "actions": [ "test" , "siembol"]
            }
            """;

    private final ConfigInfoProvider infoProvider = new AdminConfigInfoProvider();
    private UserInfo steve;

    @Before
    public void setUp() {
        steve = new UserInfo();
        steve.setUserName("steve");
        steve.setEmail("steve@secret.net");
    }

    @Test
    public void releaseTest() {
        ConfigInfo info = infoProvider.getReleaseInfo(steve, testConfig);

        Assert.assertEquals(info.getOldVersion(), 1);
        Assert.assertEquals(info.getVersion(), 2);
        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitMessage(), "Admin configuration released to version: 2");

        Assert.assertEquals(info.getCommitter(), "steve");
        Assert.assertEquals(info.getCommitterEmail(), steve.getEmail());

        Assert.assertEquals(info.getFilesContent().size(), 1);
        Assert.assertEquals(info.getFilesContent().containsKey("admin_config.json"), true);
        Assert.assertEquals(info.getFilesContent()
                .get("admin_config.json").get().indexOf("\"config_version\": 2,") > 0, true);

    }

    @Test
    public void filterRulesTest() {
        Assert.assertEquals(infoProvider.isReleaseFile("admin_config.json"), true);
        Assert.assertEquals(infoProvider.isReleaseFile("rules.json"), false);
    }
}