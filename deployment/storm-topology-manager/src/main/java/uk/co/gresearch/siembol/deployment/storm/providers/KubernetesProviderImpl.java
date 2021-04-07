package uk.co.gresearch.siembol.deployment.storm.providers;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.model.StormTopologyDto;
import uk.co.gresearch.siembol.deployment.storm.model.KubernetesAttributesDto;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.*;

public class KubernetesProviderImpl implements KubernetesProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final static String NAME_PLACEHOLDER = "--name--";
    private final static String NAMESPACE_PLACEHOLDER  = "--namespace--";
    private final static String IMAGE_PLACEHOLDER = "--image--";
    private final static String NIMBUS_PLACEHOLDER = "--nimbus--";
    private final static String ARGS_PLACEHOLDER = "--args--";
    private final static String QUOTE_CHAR = "\"";
    private final static String OPEN_BRACKET_CHAR = "[";
    private final static String CLOSE_BRACKET_CHAR = "]";
    private final static String SEPARATOR = ", ";

    private final KubernetesClient client;
    private final String namespace;
    private final String stormNimbusServers;
    private final String stormSubmitJobTemplate;

    public KubernetesProviderImpl(KubernetesAttributesDto attributes) throws IOException {
        this(new DefaultKubernetesClient(), attributes);
    }

    KubernetesProviderImpl(KubernetesClient client, KubernetesAttributesDto attributes) throws IOException {
        this.client = client;
        this.namespace = attributes.getNamespace();
        this.stormNimbusServers = attributes.getStormNimbusServer();

        byte[] encoded = Files.readAllBytes(Paths.get(attributes.getStormSubmitJobTemplateFile()));
        stormSubmitJobTemplate = new String(encoded, UTF_8);
    }

    public void createOrReplaceJob(StormTopologyDto attr) {
        String template = makeJobYaml(stormSubmitJobTemplate, attr, namespace, stormNimbusServers);
        InputStream job = new ByteArrayInputStream(template.getBytes(UTF_8));
        LOG.info("Launching topology with job name: {}, {}", attr.getTopologyName(), attr.getTopologyId());

        if(jobIsActive(attr.getTopologyId())){
            LOG.warn("Trying to submit a job that is still active in K8s. This is likely due to multiple calls to " +
                     "synchronise within a short time period");
            return;
        } else if (jobExistsInCluster(attr.getTopologyId())) {
            // NOTE: If job exists and is replaced with the same config, it wont trigger a CRUD event and launch
            LOG.info("Deleting existing job {} in K8s", attr.getTopologyId());
            client.batch().jobs().inNamespace(namespace).withName(attr.getTopologyId()).delete();
        }

        LOG.debug("Job template: {}", template);
        client.load(job).inNamespace(namespace).createOrReplace();
    }

    private boolean jobExistsInCluster(String topologyId) {
        return Optional.ofNullable(
                client.batch()
                    .jobs()
                    .inNamespace(namespace)
                    .withName(topologyId)
                    .get()
        ).isPresent();
    }

    private boolean jobIsActive(String topologyId) {
        Optional<Job> activeJobs = Optional.ofNullable(client.batch()
                .jobs()
                .inNamespace(namespace)
                .withName(topologyId)
                .get());

        return activeJobs.isPresent()
                && activeJobs.get().getStatus() != null
                && activeJobs.get().getStatus().getActive() != null
                && activeJobs.get().getStatus().getActive() > 0;
    }

    private static String makeJobYaml(String template, StormTopologyDto attr, String namespace, String nimbus) {
        template = template.replaceAll(NAME_PLACEHOLDER, attr.getTopologyId());
        template = template.replaceAll(NAMESPACE_PLACEHOLDER, namespace);
        template = template.replaceAll(NIMBUS_PLACEHOLDER, nimbus);
        template = template.replaceAll(IMAGE_PLACEHOLDER, attr.getImage());

        // Convert List<String> of args to String in format ["arg1", "args2"]
        Function<String,String> addQuotes = s -> QUOTE_CHAR + s + QUOTE_CHAR;
        String result = attr.getAttributes()
                .stream()
                .map(addQuotes)
                .collect(Collectors.joining(SEPARATOR));
        String args = OPEN_BRACKET_CHAR + result + CLOSE_BRACKET_CHAR;
        template = template.replaceAll(ARGS_PLACEHOLDER, args);

        return template;
    }
}
