package uk.co.gresearch.siembol.deployment.storm.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.deployment.storm.model.KubernetesAttributesDto;

import java.io.IOException;

@Ignore
public class KubernetesProviderImplTest {
    private static final ObjectReader READER = new ObjectMapper()
            .readerFor(StormTopologyDto.class);

    private final String topologyConfig = """
            {
             "topology_name": "parsing-heartbeat",
             "topology_id": "id1234",
             "image": "gr/siembol-parsing-storm:1.72-SNAPSHOT",
             "service_name": "parsing",
             "attributes": [
                 "testattributes1",
                 "testattributes2"
             ]
            }
            """;

    private final String expectedYaml = """
            apiVersion: batch/v1
            kind: Job
            metadata:
              name: parsing-heartbeat
              namespace: siembol
            spec:
              template:
                spec:
                  restartPolicy: Never
                  containers:
                    - env:
                        - name: NIMBUS_SEEDS
                          value: '["nimbus"]'
                      args: ["testattributes1", "testattributes2"]
                      image: gr/siembol-parsing-storm:1.72-SNAPSHOT
                      name: parsing-heartbeat
                  securityContext:
                    runAsUser: 1000
            """;

    KubernetesProvider provider;

    @Rule
    public KubernetesServer server = new KubernetesServer(true, true);

    @Before
    public void setUp() throws IOException {
        KubernetesAttributesDto attr = new KubernetesAttributesDto();
        attr.setStormNimbusServer("[\"nimbus\"]");
        attr.setNamespace("siembol");
        attr.setStormSubmitJobTemplateFile("../../config/storm-topology-manager/storm-submit.yaml");

        provider = new KubernetesProviderImpl(server.getClient(), attr);
    }

    @Test
    public void testCreateOrReplaceJob() throws IOException {
        StormTopologyDto attr = READER.readValue(topologyConfig);
        provider.createOrReplaceJob(attr);
    }
}