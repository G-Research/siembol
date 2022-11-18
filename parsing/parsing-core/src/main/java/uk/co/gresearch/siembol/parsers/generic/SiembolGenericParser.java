package uk.co.gresearch.siembol.parsers.generic;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.siembol.parsers.common.SiembolParser;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;
import uk.co.gresearch.siembol.parsers.extractors.ParserExtractor;
import uk.co.gresearch.siembol.parsers.transformations.Transformation;
import uk.co.gresearch.siembol.parsers.transformations.TransformationsLibrary;

import java.lang.invoke.MethodHandles;
import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * An object for generic parsing
 *
 * <p>This class is an implementation of SiembolParser interface.
 * It is used for parsing a log by creating two fields:
 * - 'original_string' field with the input message,
 * - 'timestamp' field with the current time in milliseconds.
 * It evaluates the chain of extractors and transformations if registered.
 * @author  Marian Novotny
 * @see SiembolParser
 *
 */
public class SiembolGenericParser implements SiembolParser {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PARSING_ERROR_MSG = "Unable to parse message: %s, exception: %s";

    private final List<ParserExtractor> extractors;
    private final List<Transformation> transformations;

    /**
     * Creates a siembol generic parser
     *
     * @param extractors a list of parser extractors
     * @param transformations a list of parser transformations
     * @see ParserExtractor
     * @see Transformation
     *
     */
    public SiembolGenericParser(List<ParserExtractor> extractors, List<Transformation> transformations) {
        this.extractors = extractors;
        this.transformations = transformations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> parse(byte[] bytes) {
        String originalMessage = null;
        try {
            originalMessage = new String(bytes, UTF_8);

            Map<String, Object> parsed = new HashMap<>();
            parsed.put(SiembolMessageFields.ORIGINAL.toString(), originalMessage);
            parsed.put(SiembolMessageFields.TIMESTAMP.toString(), System.currentTimeMillis());

            if (extractors != null) {
                parsed = ParserExtractor.extract(extractors, parsed);
            }

            if (transformations != null) {
                parsed = TransformationsLibrary.transform(transformations, parsed);
            }

            return parsed.isEmpty() ? new ArrayList<>() : Arrays.asList(parsed);
        } catch (Exception e) {
            String errorMessage = String.format(PARSING_ERROR_MSG, originalMessage, ExceptionUtils.getStackTrace(e));
            LOG.debug(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
    }
}
