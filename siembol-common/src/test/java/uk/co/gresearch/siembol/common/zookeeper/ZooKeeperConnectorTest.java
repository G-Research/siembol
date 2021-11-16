package uk.co.gresearch.siembol.common.zookeeper;

import org.apache.curator.test.TestingServer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class ZooKeeperConnectorTest {
    private static TestingServer TESTING_SERVER;

    @BeforeClass
    public static void setUp() throws Exception {
        TESTING_SERVER =  new TestingServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TESTING_SERVER.close();
    }

    @Test
    public void createConnectorNewNodeOK() throws Exception {
        final String path = "/a/b/c";
        var zooKeeperConnector = new ZooKeeperConnectorImpl.Builder()
                .zkServer(TESTING_SERVER.getConnectString())
                .path(path)
                .initValueIfNotExists("dummy_value")
                .build();
        zooKeeperConnector.initialise();
        Assert.assertEquals("dummy_value", zooKeeperConnector.getData());

        var zooKeeperConnector2 = new ZooKeeperConnectorImpl.Builder()
                .zkServer(TESTING_SERVER.getConnectString())
                .path(path)
                .initValueIfNotExists("changed_value")
                .build();
        zooKeeperConnector2.initialise();
        Assert.assertEquals("dummy_value", zooKeeperConnector.getData());
        Assert.assertEquals("dummy_value", zooKeeperConnector2.getData());
        zooKeeperConnector.close();
        zooKeeperConnector2.close();
        Thread.sleep(1000);
    }

    @Test
    public void addListenerOK() throws Exception {
        final String path = "/c/d/e";
        var zooKeeperConnector = new ZooKeeperConnectorImpl.Builder()
                .zkServer(TESTING_SERVER.getConnectString())
                .path(path)
                .initValueIfNotExists("dummy_value")
                .build();
        zooKeeperConnector.initialise();
        Assert.assertEquals("dummy_value", zooKeeperConnector.getData());

        final var called = new AtomicReference<Boolean>(false);
        Runnable callback = () -> called.set(true);
        zooKeeperConnector.addCacheListener(callback);
        zooKeeperConnector.setData("changed");
        Thread.sleep(1000);
        Assert.assertEquals("changed", zooKeeperConnector.getData());
        Assert.assertTrue(called.get());
        zooKeeperConnector.close();
        Thread.sleep(1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderMissingArguments() throws Exception {
        final String path = "/c/d/e";
        var zooKeeperConnector = new ZooKeeperConnectorImpl.Builder()
                .zkServer(TESTING_SERVER.getConnectString())
                .initValueIfNotExists("dummy_value")
                .build();
    }


    @Test(expected = IllegalStateException.class)
    public void getEmptyData() throws Exception {
        final String path = "/f/g/h";
        var zooKeeperConnector = new ZooKeeperConnectorImpl.Builder()
                .zkServer(TESTING_SERVER.getConnectString())
                .path(path)
                .build();
        zooKeeperConnector.getData();
    }

    @Test(expected = IllegalStateException.class)
    public void timeoutInitException() throws Exception {
        final String path = "/i/j/k";
        var zooKeeperConnector = new ZooKeeperConnectorImpl.Builder()
                .zkServer(TESTING_SERVER.getConnectString())
                .initTimeout(0)
                .path(path)
                .build();
        zooKeeperConnector.initialise();
    }
}
