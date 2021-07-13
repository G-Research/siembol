package uk.co.gresearch.siembol.configeditor.sync.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.siembol.common.model.StormParsingApplicationAttributesDto;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorServiceContext;
import uk.co.gresearch.siembol.configeditor.sync.common.ConfigServiceHelper;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.ERROR;
import static uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult.StatusCode.OK;

public class GetParsingAppStormTopologyActionTest {
    private static final ObjectReader ADMIN_CONFIG_READER = new ObjectMapper()
            .readerFor(StormParsingApplicationAttributesDto.class);

    /**
     *{
     *   "config_version": 1,
     *   "client.id.prefix": "siembol.writer",
     *   "group.id.prefix": "siembol.reader",
     *   "zookeeper.attributes": {
     *     "zk.url": "global_url",
     *     "zk.path": "global_path",
     *     "zk.base.sleep.ms": 1000,
     *     "zk.max.retries": 3
     *   },
     *   "kafka.batch.writer.attributes": {
     *     "batch.size": 50,
     *     "producer.properties": {
     *       "bootstrap.servers": "global_servers",
     *       "security.protocol": "SASL_PLAINTEXT"
     *     }
     *   },
     *   "storm.attributes": {
     *     "bootstrap.servers": "dummy",
     *     "first.pool.offset.strategy": "UNCOMMITTED_LATEST",
     *     "kafka.spout.properties": {
     *       "session.timeout.ms": 300000,
     *       "security.protocol": "SASL_PLAINTEXT"
     *     },
     *     "storm.config": {
     *       "num.workers": 1
     *     }
     *   },
     *   "overridden.applications": [
     *     {
     *       "application.name": "secret",
     *       "kafka.batch.writer.attributes": {
     *         "batch.size": 1,
     *         "producer.properties": {
     *           "bootstrap.servers": "dummy",
     *           "security.protocol": "SASL_PLAINTEXT"
     *         }
     *       },
     *       "storm.attributes": {
     *         "bootstrap.servers": "dummy",
     *         "first.pool.offset.strategy": "UNCOMMITTED_LATEST",
     *         "kafka.spout.properties": {
     *           "session.timeout.ms": 300000,
     *           "security.protocol": "SASL_PLAINTEXT"
     *         },
     *         "storm.config": {
     *           "num.workers": 2
     *         }
     *       }
     *     }
     *   ]
     * }
     **/
    @Multiline
    public static String adminConfig;

    /**
     *{
     *   "config_version": 1,
     *   "client.id.prefix": "siembol.writer",
     *   "group.id.prefix": "siembol.reader",
     *   "zookeeper.attributes": {
     *     "zk.url": "global_url",
     *     "zk.path": "global_path",
     *     "zk.base.sleep.ms": 1000,
     *     "zk.max.retries": 3
     *   },
     *   "kafka.batch.writer.attributes": {
     *     "batch.size": 50,
     *     "producer.properties": {
     *       "bootstrap.servers": "global_servers",
     *       "security.protocol": "SASL_PLAINTEXT"
     *     }
     *   },
     *   "storm.attributes": {
     *     "bootstrap.servers": "dummy",
     *     "first.pool.offset.strategy": "UNCOMMITTED_LATEST",
     *     "kafka.spout.properties": {
     *       "session.timeout.ms": 300000,
     *       "security.protocol": "SASL_PLAINTEXT"
     *     },
     *     "storm.config": {
     *       "num.workers": 1
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String adminConfigNoOverriddenApplications;

    /**
     *{
     *   "parsing_applications_version": 0,
     *   "parsing_applications": [
     *     {
     *       "parsing_app_name": "secret",
     *       "parsing_app_version": 1,
     *       "parsing_app_author": "siembol",
     *       "parsing_app_settings": {
     *         "parsing_app_type": "single_parser",
     *         "input_topics": [
     *           "test"
     *         ],
     *         "error_topic": "test",
     *         "input_parallelism": 4,
     *         "parsing_parallelism": 4,
     *         "output_parallelism": 4,
     *         "parse_metadata": false
     *       },
     *       "parsing_settings": {
     *         "single_parser": {
     *           "output_topic": "test",
     *           "parser_name": "test"
     *         }
     *       }
     *     },
     *     {
     *       "parsing_app_name": "public",
     *       "parsing_app_version": 1,
     *       "parsing_app_author": "siembol",
     *       "parsing_app_settings": {
     *         "parsing_app_type": "single_parser",
     *         "input_topics": [
     *           "test"
     *         ],
     *         "error_topic": "test",
     *         "input_parallelism": 4,
     *         "parsing_parallelism": 4,
     *         "output_parallelism": 4,
     *         "parse_metadata": false
     *       },
     *       "parsing_settings": {
     *         "single_parser": {
     *           "output_topic": "test",
     *           "parser_name": "test"
     *         }
     *       }
     *     }
     *   ]
     * }
     **/
    @Multiline
    public static String release;

    private ConfigServiceHelper serviceHelper;
    private GetParsingAppStormTopologyAction getStormTopologyAction;
    private ConfigEditorServiceContext context;
    private String topologyImage = "dummyImage";
    private String serviceName = "dummyService";

    @Before
    public void setUp() {
        context = new ConfigEditorServiceContext();
        context.setAdminConfig(adminConfig);
        context.setConfigRelease(release);
        serviceHelper = Mockito.mock(ConfigServiceHelper.class);
        when(serviceHelper.getStormTopologyImage()).thenReturn(Optional.of(topologyImage));
        when(serviceHelper.getName()).thenReturn(serviceName);
        when(serviceHelper.isInitAdminConfig(eq(adminConfig))).thenReturn(false);
        when(serviceHelper.isInitRelease(eq(release))).thenReturn(false);

        getStormTopologyAction = new GetParsingAppStormTopologyAction(serviceHelper);
    }

    @Test
    public void getStormTopologyOk() throws IOException {
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertTrue(result.getAttributes().getServiceContext().getStormTopologies().isPresent());
        Assert.assertEquals(2, result.getAttributes().getServiceContext().getStormTopologies().get().size());

        verify(serviceHelper, times(2)).getStormTopologyImage();
        verify(serviceHelper, times(2)).getName();

        StormTopologyDto topologySecret = result.getAttributes().getServiceContext().getStormTopologies().get().get(0);
        StormTopologyDto topologyPublic = result.getAttributes().getServiceContext().getStormTopologies().get().get(1);
        Assert.assertEquals(serviceName, topologySecret.getServiceName());
        Assert.assertEquals("parsing-secret", topologySecret.getTopologyName());
        Assert.assertEquals("parsing-public", topologyPublic.getTopologyName());
        Assert.assertEquals(topologyImage, topologySecret.getImage());
        Assert.assertEquals(topologyImage, topologyPublic.getImage());
        Assert.assertNotNull(topologySecret.getTopologyId());
        Assert.assertNotNull(topologyPublic.getTopologyId());
        Assert.assertEquals(2, topologySecret.getAttributes().size());
        Assert.assertEquals(2, topologyPublic.getAttributes().size());

        String adminConfigSecretStr = new String(Base64.getDecoder().decode(topologySecret.getAttributes().get(0)));
        StormParsingApplicationAttributesDto adminConfigSecret = ADMIN_CONFIG_READER.readValue(adminConfigSecretStr);

        String adminConfigPublicStr = new String(Base64.getDecoder().decode(topologyPublic.getAttributes().get(0)));
        StormParsingApplicationAttributesDto adminConfigPublic = ADMIN_CONFIG_READER.readValue(adminConfigPublicStr);

        Assert.assertFalse(adminConfigSecretStr.contains("overridden.applications"));
        Assert.assertFalse(adminConfigSecretStr.contains("config_version"));
        Assert.assertFalse(adminConfigPublicStr.contains("overridden.applications"));
        Assert.assertFalse(adminConfigPublicStr.contains("config_version"));

        Assert.assertEquals(1, adminConfigSecret.getKafkaBatchWriterAttributes().getBatchSize().intValue());
        Assert.assertEquals(50, adminConfigPublic.getKafkaBatchWriterAttributes().getBatchSize().intValue());
        Assert.assertEquals(1, adminConfigSecret.getKafkaBatchWriterAttributes().getBatchSize().intValue());
        Assert.assertEquals(50, adminConfigPublic.getKafkaBatchWriterAttributes().getBatchSize().intValue());

        Assert.assertEquals(Integer.valueOf(2),
                adminConfigSecret.getStormAttributes().getStormConfig().getRawMap().get("num.workers"));
        Assert.assertEquals(Integer.valueOf(1),
                adminConfigPublic.getStormAttributes().getStormConfig().getRawMap().get("num.workers"));
    }

    @Test
    public void getStormTopologyNoOverriddenAppsOk() throws IOException {

        context.setAdminConfig(adminConfigNoOverriddenApplications);
        when(serviceHelper.isInitAdminConfig(eq(adminConfigNoOverriddenApplications))).thenReturn(false);
        when(serviceHelper.isInitRelease(eq(release))).thenReturn(false);

        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertTrue(result.getAttributes().getServiceContext().getStormTopologies().isPresent());
        Assert.assertEquals(2, result.getAttributes().getServiceContext().getStormTopologies().get().size());

        verify(serviceHelper, times(2)).getStormTopologyImage();
        verify(serviceHelper, times(2)).getName();

        StormTopologyDto topologySecret = result.getAttributes().getServiceContext().getStormTopologies().get().get(0);
        StormTopologyDto topologyPublic = result.getAttributes().getServiceContext().getStormTopologies().get().get(1);
        Assert.assertEquals(serviceName, topologySecret.getServiceName());
        Assert.assertEquals("parsing-secret", topologySecret.getTopologyName());
        Assert.assertEquals("parsing-public", topologyPublic.getTopologyName());
        Assert.assertEquals(topologyImage, topologySecret.getImage());
        Assert.assertEquals(topologyImage, topologyPublic.getImage());
        Assert.assertNotNull(topologySecret.getTopologyId());
        Assert.assertNotNull(topologyPublic.getTopologyId());
        Assert.assertEquals(2, topologySecret.getAttributes().size());
        Assert.assertEquals(2, topologyPublic.getAttributes().size());

        String adminConfigSecretStr = new String(Base64.getDecoder().decode(topologySecret.getAttributes().get(0)));
        StormParsingApplicationAttributesDto adminConfigSecret = ADMIN_CONFIG_READER.readValue(adminConfigSecretStr);

        String adminConfigPublicStr = new String(Base64.getDecoder().decode(topologyPublic.getAttributes().get(0)));
        StormParsingApplicationAttributesDto adminConfigPublic = ADMIN_CONFIG_READER.readValue(adminConfigPublicStr);

        Assert.assertFalse(adminConfigSecretStr.contains("overridden.applications"));
        Assert.assertFalse(adminConfigSecretStr.contains("config_version"));
        Assert.assertFalse(adminConfigPublicStr.contains("overridden.applications"));
        Assert.assertFalse(adminConfigPublicStr.contains("config_version"));

        Assert.assertEquals(50, adminConfigSecret.getKafkaBatchWriterAttributes().getBatchSize().intValue());
        Assert.assertEquals(50, adminConfigPublic.getKafkaBatchWriterAttributes().getBatchSize().intValue());
        Assert.assertEquals(50, adminConfigSecret.getKafkaBatchWriterAttributes().getBatchSize().intValue());
        Assert.assertEquals(50, adminConfigPublic.getKafkaBatchWriterAttributes().getBatchSize().intValue());

        Assert.assertEquals(Integer.valueOf(1),
                adminConfigSecret.getStormAttributes().getStormConfig().getRawMap().get("num.workers"));
        Assert.assertEquals(Integer.valueOf(1),
                adminConfigPublic.getStormAttributes().getStormConfig().getRawMap().get("num.workers"));
    }

    @Test
    public void getStormTopologyInitAdminConfigOk() {
        when(serviceHelper.isInitAdminConfig(eq(adminConfig))).thenReturn(true);
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertFalse(result.getAttributes().getServiceContext().getStormTopologies().isPresent());
        verify(serviceHelper, times(1)).isInitAdminConfig(eq(adminConfig));
        verify(serviceHelper, times(0)).getStormTopologyImage();
    }

    @Test
    public void getStormTopologyInitReleaseOk() {
        when(serviceHelper.isInitRelease(eq(release))).thenReturn(true);
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(OK, result.getStatusCode());
        Assert.assertNotNull(result.getAttributes().getServiceContext());
        Assert.assertFalse(result.getAttributes().getServiceContext().getStormTopologies().isPresent());
        verify(serviceHelper, times(1)).isInitRelease(eq(release));
    }

    @Test
    public void getStormTopologyMissingAdminConfig() {
        context.setAdminConfig(null);
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void getStormTopologyMissingRelease() {
        context.setConfigRelease(null);
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void getStormTopologyInvalidAdminConfig() {
        context.setAdminConfig("INVALID");
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }

    @Test
    public void getStormTopologyInvalidRelease() {
        context.setConfigRelease("INVALID");
        ConfigEditorResult result = getStormTopologyAction.execute(context);
        Assert.assertEquals(ERROR, result.getStatusCode());
    }
}
