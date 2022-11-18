package uk.co.gresearch.siembol.parsers.netflow;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.SiembolParser;

import java.lang.invoke.MethodHandles;
import java.util.*;

import static uk.co.gresearch.siembol.common.constants.SiembolMessageFields.ORIGINAL;
import static uk.co.gresearch.siembol.common.constants.SiembolMessageFields.TIMESTAMP;
/**
 * An object for netflow parsing
 *
 * <p>This class is an implementation of SiembolParser interface.
 * It is used for parsing a netflow v9 messages.
 *
 * It evaluates chain of extractors and transformations if registered.
 * @author  Marian Novotny
 * @see SiembolParser
 * @see NetflowTransportProvider
 *
 */

public class SiembolNetflowParser implements SiembolParser {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    public static final String NETFLOW_SOURCE_ID = "netflow_source_id";
    public static final String NETFLOW_GLOBAL_SOURCE = "netflow_source";
    public static final String NETFLOW_UNKNOWN_TEMPLATE = "netflow_unknown_template";
    public static final String UNSUPPORTED_MSG = "The method is unsupported because netflow parsing requires metadata";

    private final NetflowParser<?> netflowParser;

    private Map<String, Object> getUnknownTemplateObject(NetflowParsingResult parsingResult, byte[] bytes) {
        Map<String, Object> ret = new HashMap<>();

        ret.put(TIMESTAMP.getName(), System.currentTimeMillis());
        ret.put(NETFLOW_GLOBAL_SOURCE, parsingResult.getGlobalSource());
        ret.put(NETFLOW_SOURCE_ID, parsingResult.getSourceId());
        ret.put(ORIGINAL.getName(), Base64.getEncoder().encodeToString(bytes));
        ret.put(NETFLOW_UNKNOWN_TEMPLATE, true);

        return ret;
    }

    public SiembolNetflowParser(NetflowTransportProvider<?> netflowTransportProvider) {
        netflowParser = new NetflowParser<>(netflowTransportProvider);
    }

    public SiembolNetflowParser() {
        this(new SimpleTransportProvider());
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> parse(String metadata, byte[] bytes) {
        ArrayList<Map<String, Object>> ret = new ArrayList<>();
        try {
            NetflowParsingResult result = netflowParser.parse(metadata, bytes);
            if (result.getStatusCode() == NetflowParsingResult.StatusCode.UNKNOWN_TEMPLATE) {
                ret.add(getUnknownTemplateObject(result, bytes));
                return ret;
            }

            if (result.getStatusCode() != NetflowParsingResult.StatusCode.OK) {
                throw new IllegalStateException(result.getStatusCode().toString());
            }

            if (result.getDataFlowSet() != null) {
                ret.ensureCapacity(result.getDataFlowSet().size());
                for (List<Pair<String, Object>> dataFlowset : result.getDataFlowSet()) {
                    Map<String, Object> current = new HashMap<>();

                    current.put(ORIGINAL.toString(), result.getOriginalString());
                    current.put(TIMESTAMP.toString(), result.getTimestamp());
                    current.put(NETFLOW_SOURCE_ID, result.getSourceId());
                    current.put(NETFLOW_GLOBAL_SOURCE, result.getGlobalSource());

                    dataFlowset.forEach(item -> current.put(item.getKey(), item.getValue()));
                    ret.add(current);
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to parse message: %s",
                    Base64.getEncoder().encodeToString(bytes));
            LOG.debug(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }

        return ret;
    }

    @Override
    public List<Map<String, Object>> parse(byte[] message) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
}
