package uk.co.gresearch.siembol.parsers.syslog;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * An object for representing a syslog message used in syslog parsing
 *
 * <p>This class represents a syslog message in the internal syslog parser.
 *
 * @author Marian Novotny
 * @see SyslogParser
 *
 */
public class SyslogMessage {
    private static final int SEVERITY_CARDINALITY = 8;
    private static final String NIL_VALUE = "-";

    private long timestamp;
    private int headerVersion;
    private int priority;
    private String hostname;
    private String appName;
    private String procId;
    private String msgId;
    private String msg;
    private String timestampStr;
    private List<Pair<String, List<Pair<String, String>>>> sdElements =
            new ArrayList<>();

    private Optional<String> getStringField(String str) {
        return null == str || NIL_VALUE.equals(str)
                ? Optional.empty()
                : Optional.of(str);
    }

    public int getHeaderVersion() {
        return headerVersion;
    }

    public int getFacility() {
        return priority / SEVERITY_CARDINALITY;
    }

    public int getPriority() {
        return priority;
    }

    public int getSeverity() {
        return priority % SEVERITY_CARDINALITY;
    }

    public Optional<String> getHostname() {
        return getStringField(hostname);
    }

    public Optional<String> getAppName() {
        return getStringField(appName);
    }

    public Optional<String> getProcId() {
        return getStringField(procId);
    }

    public Optional<String> getMsgId() {
        return getStringField(msgId);
    }

    public Optional<String> getMsg() {
        return getStringField(msg);
    }

    public long getTimestamp() {
        return timestamp != 0 ? timestamp : System.currentTimeMillis();
    }

    public List<Pair<String, List<Pair<String, String>>>> getSdElements() {
        return sdElements;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setHeaderVersion(int headerVersion) {
        this.headerVersion = headerVersion;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setProcId(String procId) {
        this.procId = procId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void adSdElement(String elementId, List<Pair<String, String>> sdParameters) {
        sdElements.add(Pair.of(elementId, sdParameters));
    }

    public Optional<String> getTimestampStr() {
        return getStringField(timestampStr);
    }

    public void setTimestampStr(String timestampStr) {
        this.timestampStr = timestampStr;
    }
}
