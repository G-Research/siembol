package uk.co.gresearch.siembol.parsers.netflow;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
/**
 * An object for parsing a netflow v9 message
 *
 * <p>This class implements a fault-tolerant netflow v9 parser.
 * Parsing of fields in Netflow v9 protocol is based on the netflow template messages that
 * are identified by a device and template id.
 * Network devices are using template id field as a counter rather than as a unique id on the network and
 * collisions of template id values on a network with multiple collectors are common.
 * This way we are using NetflowTransportProvider interface to provide global id of the template in order
 * to avoid collisions.
 *
 * @author Marian Novotny
 * @see NetflowParsingResult
 * @see NetflowTransportProvider
 *
 */
public class NetflowParser<T> {
    public static final int SUPPORTED_VERSION = 9;
    public static final int TEMPLATE_FLOW_SET_ID = 0;
    public static final int OPTIONS_FLOW_SET_ID = 1;
    public static final int DATA_FLOW_SET_OFFSET = 255;
    public static final int NETFLOW_HEADER_SIZE = 20;
    public static final int RECORD_FLOWSET_AND_LEN_SIZE = 4;
    public static final int TEMPLATE_ID_AND_FIELD_COUNT_SIZE = 4;
    public static final int FIELD_TYPE_AND_LEN_SIZE = 4;
    public static final int RECORD_MAX_PADDING_SIZE = 3;
    public static final String TEMPLATE_ID = "template_id";
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());

    private final NetflowTransportProvider<T> transportProvider;

    /**
     * Creates a netflow parser
     *
     * @param transportProvider provider of global template id from a netflow message
     */
    public NetflowParser(NetflowTransportProvider<T> transportProvider) {
        this.transportProvider = transportProvider;
    }

    /**
     * Parses a netflow message
     *
     * @param metadata an input string used in transport provider to identify the device and template
     * @param data binary netflow payload
     * @return netflow parsing result with the parsed netflow message
     */
    public NetflowParsingResult parse(String metadata, byte[] data) {
        NetflowTransportMessage<T> message = transportProvider.message(metadata, data);
        return parse(message);
    }

    private NetflowHeader parseHeader(BinaryBuffer buffer) {
        if (!buffer.hasRemaining(NETFLOW_HEADER_SIZE)) {
            LOG.error("Insufficient buffer size for reading header");
            return null;
        }

        int version = buffer.readUShort();
        int count = buffer.readUShort();
        long uptime = buffer.readUInt();
        long timestamp = buffer.readUInt();
        long sequence = buffer.readUInt();
        int sourceId = buffer.readInt();

        if (version != SUPPORTED_VERSION) {
            LOG.error(String.format("Unsupported Netflow version: %d", version));
            return null;
        }
        return new NetflowHeader(version, count, uptime, timestamp, sequence, sourceId);
    }

    private List<NetflowField> createTemplate(BinaryBuffer buffer, int fieldCount)
    {
        List<NetflowField> ret = new ArrayList<>();

        for (int i = 0; i < fieldCount; i++) {
            int fieldType = buffer.readUShort();
            int fieldLen = buffer.readUShort();
            ret.add(new NetflowField(fieldType, fieldLen));
        }

        return ret;
    }

    private boolean parseTemplates(NetflowTransportMessage<T> message,
                                   NetflowHeader header,
                                   BinaryBuffer buffer,
                                   int length)
    {
        int processed = 0;
        while (processed < length) {
            if (length - processed < TEMPLATE_ID_AND_FIELD_COUNT_SIZE) {
                return false;
            }

            int templateId = buffer.readUShort();
            if (templateId <= DATA_FLOW_SET_OFFSET) {
                return false;
            }

            int fieldCount = buffer.readUShort();
            processed += TEMPLATE_ID_AND_FIELD_COUNT_SIZE + fieldCount * FIELD_TYPE_AND_LEN_SIZE;
            if (processed > length) {
                return false;
            }

            List<NetflowField> currentTemplate = createTemplate(buffer, fieldCount);
            transportProvider.updateTemplate(message, header, templateId, currentTemplate);
            if (length - processed <= RECORD_MAX_PADDING_SIZE ){
                //NOTE: padding
                break;
            }
        }

        return processed > 0;
    }

    private List<List<Pair<String, Object>>> getDataFields(int templateId,
                                                           BinaryBuffer buffer,
                                                           int length,
                                                           List<NetflowField> template) {

        List<List<Pair<String, Object>>> ret = new ArrayList<>();
        int processed = 0;

        while (processed < length) {
            List<Pair<String, Object>> record = new ArrayList<>();
            record.add(Pair.of(TEMPLATE_ID, String.valueOf(templateId)));
            for (NetflowField field : template) {
                processed += field.getLength();
                if (processed > length) {
                    return null;
                }

                String fieldName = field.getName();
                Object fieldValue = field.getValue(buffer);

                record.add(Pair.of(fieldName, fieldValue));
            }
            ret.add(record);
            if (length - processed <= RECORD_MAX_PADDING_SIZE) {
                //NOTE: padding
                break;
            }
        }
        return ret;
    }

    private NetflowParsingResult parse(NetflowTransportMessage<T> transportMessage) {
        BinaryBuffer buffer = transportMessage.getNetflowPayload();

        NetflowHeader header = parseHeader(buffer);
        if (header == null) {
            return new NetflowParsingResult(NetflowParsingResult.StatusCode.PARSING_HEADER_ERROR,
                    transportMessage);
        }

        List<List<Pair<String, Object>>> dataFlowSet = new ArrayList<>();
        int processedRecords = 0;
        while (processedRecords < header.getCount() && buffer.hasRemaining())
        {
            if (!buffer.hasRemaining(RECORD_FLOWSET_AND_LEN_SIZE)) {
                return new NetflowParsingResult(NetflowParsingResult.StatusCode.PARSING_ERROR,
                        transportMessage,
                        header);
            }

            int offset = buffer.getBuffer().position();
            int flowSetId = buffer.readUShort();
            int length = buffer.readUShort();
            if (length < RECORD_FLOWSET_AND_LEN_SIZE ||
                    !buffer.hasRemaining(length - RECORD_FLOWSET_AND_LEN_SIZE)) {
                return new NetflowParsingResult(NetflowParsingResult.StatusCode.PARSING_ERROR,
                        transportMessage,
                        header);
            }

            switch (flowSetId) {
                case TEMPLATE_FLOW_SET_ID:
                    if (!parseTemplates(transportMessage,
                            header,
                            buffer,
                            length - RECORD_FLOWSET_AND_LEN_SIZE)) {
                        return new NetflowParsingResult(NetflowParsingResult.StatusCode.PARSING_TEMPLATE_ERROR,
                                transportMessage,
                                header);
                    }
                    processedRecords++;
                    break;
                case OPTIONS_FLOW_SET_ID:
                    processedRecords++;
                    break;
                default:
                    if (flowSetId < DATA_FLOW_SET_OFFSET){
                        return new NetflowParsingResult(NetflowParsingResult.StatusCode.PARSING_ERROR,
                                transportMessage,
                                header);
                    }
                    Optional<List<NetflowField>> template = transportProvider.getTemplate(
                            transportMessage,
                            header,
                            flowSetId);

                    if (!template.isPresent()) {
                        LOG.error(String.format("Unknown template %d for source: %s, sourceID: %d",
                                flowSetId, transportMessage.getGlobalSource(), header.getSourceId()));
                        return new NetflowParsingResult(NetflowParsingResult.StatusCode.UNKNOWN_TEMPLATE,
                                transportMessage,
                                header);
                    }

                    List<List<Pair<String, Object>>> currentData = getDataFields(flowSetId,
                            buffer,
                            length - RECORD_FLOWSET_AND_LEN_SIZE,
                            template.get());

                    if (currentData == null) {
                        return new NetflowParsingResult(NetflowParsingResult.StatusCode.PARSING_ERROR,
                                transportMessage,
                                header);
                    }

                    processedRecords += currentData.size();
                    dataFlowSet.addAll(currentData);
            }

            if (buffer.hasRemaining()) {
                //we skip remaining bytes, padding or unparsed records
                buffer.setPosition(offset + length);
            }
        }

        return new NetflowParsingResult(NetflowParsingResult.StatusCode.OK,
                transportMessage,
                header,
                dataFlowSet);
    }
}
