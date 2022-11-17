package uk.co.gresearch.siembol.parsers.transformations;

import java.util.Map;
import java.util.function.Function;
/**
 * An object for transformation
 *
 * <p>This functional interface is used for representing parsing transformations.
 * It is a functional interface that extends Function interface for
 * a function of a map of String to Object argument that returns a map of String to Object.
 *
 * @author  Marian Novotny
 *
 */
@FunctionalInterface
public interface Transformation extends Function<Map<String, Object>, Map<String, Object>> {}
