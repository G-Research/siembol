package uk.co.gresearch.siembol.deployment.storm.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.common.utils.HttpProvider;
import uk.co.gresearch.siembol.deployment.storm.model.StormResponseDto;
import uk.co.gresearch.siembol.deployment.storm.model.StormResponseTopologyDto;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

public class StormProviderImpl implements StormProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectReader RESULT_READER = new ObjectMapper().readerFor(StormResponseDto.class);
    private static final String STORM_RESPONSE_SUCCESS = "success";

    private final HttpProvider httpProvider;
    private final int killWaitSeconds;

    public StormProviderImpl(HttpProvider httpProvider, int killWaitSeconds) {
        this.httpProvider = httpProvider;
        this.killWaitSeconds = killWaitSeconds;
    }

    public boolean killTopology(String id) {
        LOG.info("Stopping topology: {}", id);
        String url = String.format(StormApplicationPaths.KILL_TOPOLOGY.getName(), id, killWaitSeconds);
        try {
            StormResponseDto response = post(url, "");
            if (response != null && response.getStatus() != null)
                return response.getStatus().equals(STORM_RESPONSE_SUCCESS);
        } catch (IllegalArgumentException | IOException e){
            LOG.error("Exception killing storm topology: ", e);
        }
        return false;
    }

    public List<StormResponseTopologyDto> listTopologies() throws IOException {
        StormResponseDto response = get(StormApplicationPaths.LIST_TOPOLOGIES);
        return response.getTopologies();
    }

    private StormResponseDto get(StormApplicationPaths path) throws IOException {
        String url = path.getName();
        String response = httpProvider.get(url);
        return RESULT_READER.readValue(response);
    }

    private StormResponseDto post(String url, String body) throws IOException {
        String response = httpProvider.post(url, body);
        return RESULT_READER.readValue(response);
    }
}
