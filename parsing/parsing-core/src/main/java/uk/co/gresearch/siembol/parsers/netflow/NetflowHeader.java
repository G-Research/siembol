package uk.co.gresearch.siembol.parsers.netflow;
/**
 * An object for representing a netflow header
 *
 * <p>This class represents a netflow header used by a netflow parser.
 *
 * @author Marian Novotny
 *
 */
public class NetflowHeader {
    private final int count;
    private final long uptime;
    private final long timestamp;
    private final long sequence;
    private final int sourceId;
    private final int version;

    public NetflowHeader(int version,
                         int count,
                         long uptime,
                         long timestamp,
                         long sequence,
                         int sourceId) {
        this.version = version;
        this.count = count;
        this.uptime = uptime;
        this.timestamp = timestamp;
        this.sequence = sequence;
        this.sourceId = sourceId;
    }

    public int getCount() {
        return count;
    }

    public long getUptime() {
        return uptime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSequence() {
        return sequence;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getVersion() {
        return version;
    }
}
