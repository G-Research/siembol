package uk.co.gresearch.siembol.enrichments.common;

import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.gresearch.siembol.common.result.SiembolResult;

import static uk.co.gresearch.siembol.common.result.SiembolResult.StatusCode.OK;
/**
 * An object that combines an enrichment result status code with attributes
 *
 * <p>This class represents an enrichment result that combines a status code along with the enrichment attributes.
 *
 * @author  Marian Novotny
 * @see EnrichmentAttributes
 * @see StatusCode
 */
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

    public static EnrichmentResult fromSiembolResult(SiembolResult result) {
        EnrichmentAttributes attr = new EnrichmentAttributes();
        attr.setMessage(result.getAttributes().getMessage());
        return new EnrichmentResult(result.getStatusCode() == OK ? StatusCode.OK : StatusCode.ERROR, attr);
    }
}
