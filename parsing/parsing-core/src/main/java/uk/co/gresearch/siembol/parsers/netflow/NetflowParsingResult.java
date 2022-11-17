package uk.co.gresearch.siembol.parsers.netflow;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
/**
 * An object for representing a netflow parsing result
 *
 * <p>This class represents a netflow parsing result used by a netflow parser.
 * It includes a status code of the result along with a netflow parsed message.
 *
 * @author Marian Novotny
 *
 */
public class NetflowParsingResult {
    enum StatusCode {
        OK,
        PARSING_HEADER_ERROR,
        PARSING_TEMPLATE_ERROR,
        PARSING_ERROR,
        UNKNOWN_TEMPLATE,
    }

    private final StatusCode code;
    private final NetflowHeader header;
    private final NetflowTransportMessage<?> transportMessage;
    private final List<List<Pair<String, Object>>> dataFlowSet;

    NetflowParsingResult(StatusCode code,
                         NetflowTransportMessage<?> transportMessage) {
        this(code, transportMessage, null, null);
    }

    NetflowParsingResult(StatusCode code,
                         NetflowTransportMessage<?> transportMessage,
                         NetflowHeader header) {
        this(code, transportMessage, header,  null);
    }

    NetflowParsingResult(StatusCode code,
                         NetflowTransportMessage<?> transportMessage,
                         NetflowHeader header,
                         List<List<Pair<String, Object>>> dataFlowSet){
        this.code = code;
        this.transportMessage = transportMessage;
        this.header = header;
        this.dataFlowSet = dataFlowSet;
    }

    public StatusCode getStatusCode(){
        return code;
    }

    public List<List<Pair<String, Object>>> getDataFlowSet(){
        return dataFlowSet;
    }

    public long getTimestamp(){
        return header.getTimestamp() * 1000;
    }

    public String getSourceId() {
        return String.valueOf(header.getSourceId());
    }

    public String getOriginalString() {
        return transportMessage.getOriginalString();
    }

    public String getGlobalSource() {
        return transportMessage.getGlobalSource();
    }

}
