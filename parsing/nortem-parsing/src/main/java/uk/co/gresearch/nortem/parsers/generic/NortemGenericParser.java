package uk.co.gresearch.nortem.parsers.generic;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.parsers.common.NortemParser;
import uk.co.gresearch.nortem.parsers.common.ParserFields;
import uk.co.gresearch.nortem.parsers.extractors.ParserExtractor;
import uk.co.gresearch.nortem.parsers.transformations.Transformation;
import uk.co.gresearch.nortem.parsers.transformations.TransformationsLibrary;

import java.lang.invoke.MethodHandles;
import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class NortemGenericParser implements NortemParser {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PARSING_ERROR_MSG = "Unable to parse message: %s, exception: %s";

    private final List<ParserExtractor> extractors;
    private final List<Transformation> transformations;

    public NortemGenericParser(List<ParserExtractor> extractors, List<Transformation> transformations) {
        this.extractors = extractors;
        this.transformations = transformations;
    }

    @Override
    public List<Map<String, Object>> parse(byte[] bytes) {
        String originalMessage = null;
        try {
            originalMessage = new String(bytes, UTF_8);

            Map<String, Object> parsed = new HashMap<>();
            parsed.put(ParserFields.ORIGINAL.toString(), originalMessage);
            parsed.put(ParserFields.TIMESTAMP.toString(), System.currentTimeMillis());

            if (extractors != null) {
                parsed = ParserExtractor.extract(extractors, parsed);
            }

            if (transformations != null) {
                parsed = TransformationsLibrary.transform(transformations, parsed);
            }

            return Arrays.asList(parsed);
        } catch (Exception e) {
            String errorMessage = String.format(PARSING_ERROR_MSG, originalMessage, ExceptionUtils.getStackTrace(e));
            LOG.error(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
    }
}
