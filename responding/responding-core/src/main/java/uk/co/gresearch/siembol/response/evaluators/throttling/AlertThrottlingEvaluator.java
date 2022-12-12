package uk.co.gresearch.siembol.response.evaluators.throttling;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.response.common.Evaluable;
import uk.co.gresearch.siembol.response.common.RespondingResult;
import uk.co.gresearch.siembol.response.common.ResponseAlert;
import uk.co.gresearch.siembol.response.common.ResponseEvaluationResult;
import uk.co.gresearch.siembol.response.model.AlertThrottlingEvaluatorAttributesDto;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
/**
 * An object for evaluating response alerts
 *
 * <p>This class implements Evaluable interface, and it is used in a response rule.
 * The alert throttling evaluator may throttle the alert based on the suppression key and the time window.
 *
 * @author  Marian Novotny
 * @see Evaluable
 */
public class AlertThrottlingEvaluator implements Evaluable {
    private static final String SHARED_VALUE = "";
    private static final int MAX_CACHE_SIZE = 1000;
    private final String suppressionKey;
    private final LoadingCache<String, String> cache;

    public AlertThrottlingEvaluator(AlertThrottlingEvaluatorAttributesDto attributesDto) {
        this.suppressionKey = attributesDto.getSuppressingKey();
        long timeWindowInMs = attributesDto.getTimeUnitType().convertToMs(attributesDto.getSuppressionTime());
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(timeWindowInMs, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return key.toLowerCase();
                    }
                });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        Optional<String> currentKey = EvaluationLibrary.substitute(alert, suppressionKey);
        if (currentKey.isEmpty()) {
            return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
        }

        if (cache.asMap().putIfAbsent(currentKey.get().toLowerCase(), SHARED_VALUE) == null) {
            return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.MATCH, alert);
        }

        return RespondingResult.fromEvaluationResult(ResponseEvaluationResult.FILTERED, alert);
    }
}
