package uk.co.gresearch.siembol.spark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;
import uk.co.gresearch.siembol.common.model.testing.AlertingSparkTestingResultDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
/**
 * An object for representing alerting spark result
 *
 * <p>This class implements Serializable interface. It contains a list of matched events (json strings) and
 *  the list of exceptions (json strings of ErrorMessages).
 *  It provides functionality for merging two alerting results that is used in the map reduce job.
 *
 * @author Marian Novotny
 * @see AlertingSparkTestingResultDto
 *
 */
public class AlertingSparkResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ObjectWriter JSON_GENERIC_WRITER = new ObjectMapper()
            .writerFor(new TypeReference<Map<String, Object>>() { });
    private static final ObjectWriter JSON_ALERTING_RESULT_WRITER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writerFor(AlertingSparkTestingResultDto.class);
    private final int maxResult;
    private int matchesTotal = 0;
    private int exceptionsTotal = 0;
    private final ArrayList<String> matches = new ArrayList<>();
    private final ArrayList<String> exceptions = new ArrayList<>();

    private AlertingSparkResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public AlertingSparkResult(AlertingResult alertingResult, int maxResult) {
        this.maxResult = maxResult;
        if (alertingResult.getStatusCode() != AlertingResult.StatusCode.OK) {
            exceptionsTotal += 1;
        }

        if (alertingResult.getAttributes().getExceptionEvents() != null) {
            exceptionsTotal += alertingResult.getAttributes().getExceptionEvents().size();
            alertingResult.getAttributes().getExceptionEvents().stream().takeWhile(x -> exceptions.size() < maxResult)
                    .forEach(x -> {
                        try {
                            exceptions.add(JSON_GENERIC_WRITER.writeValueAsString(x));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        if (alertingResult.getAttributes().getOutputEvents() != null) {
            matchesTotal += alertingResult.getAttributes().getOutputEvents().size();
            alertingResult.getAttributes().getOutputEvents().stream().takeWhile(x -> matches.size() < maxResult)
                    .forEach(x -> {
                        try {
                            matches.add(JSON_GENERIC_WRITER.writeValueAsString(x));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    public AlertingSparkResult merge(AlertingSparkResult other) {
        matchesTotal += other.matchesTotal;
        exceptionsTotal += other.exceptionsTotal;

        other.matches.stream().takeWhile(x -> matches.size() < maxResult).forEach(matches::add);
        other.exceptions.stream().takeWhile(x -> exceptions.size() < maxResult).forEach(exceptions::add);

        return this;
    }

    public AlertingSparkTestingResultDto toAlertingSparkTestingResult() {
        var ret = new AlertingSparkTestingResultDto();
        ret.setMatchesTotal(matchesTotal);
        ret.setExceptionsTotal(exceptionsTotal);
        ret.setExceptionsStrings(exceptions);
        ret.setMatchesStrings(matches);
        return ret;
    }
    public String toString() {
        try {
            return JSON_ALERTING_RESULT_WRITER.writeValueAsString(toAlertingSparkTestingResult());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isEmpty() {
        return matchesTotal == 0 && exceptionsTotal == 0;
    }

    public static AlertingSparkResult emptyResult(int maxResult) {
        return new AlertingSparkResult(maxResult);
    }
}
