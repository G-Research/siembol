package uk.co.gresearch.nortem.enrichments.common;

import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.gresearch.nortem.common.result.NortemResult;

import static uk.co.gresearch.nortem.common.result.NortemResult.StatusCode.OK;

public class EnrichmentResult {
    public enum StatusCode {
        OK,
        ERROR
    }

    private StatusCode statusCode;
    private EnrichmentAttributes attributes;

    public EnrichmentResult(StatusCode statusCode, EnrichmentAttributes attributes) {
        this.statusCode = statusCode;
        this.attributes = attributes;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public EnrichmentAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(EnrichmentAttributes attributes) {
        this.attributes = attributes;
    }

    public static EnrichmentResult fromException(Exception e) {
        EnrichmentAttributes attr = new EnrichmentAttributes();
        attr.setMessage(ExceptionUtils.getStackTrace(e));
        return new EnrichmentResult(StatusCode.ERROR, attr);
    }

    public static EnrichmentResult fromNortemResult(NortemResult result) {
        EnrichmentAttributes attr = new EnrichmentAttributes();
        attr.setMessage(result.getAttributes().getMessage());
        return new EnrichmentResult(result.getStatusCode() == OK ? StatusCode.OK : StatusCode.ERROR, attr);
    }
}
