package uk.co.gresearch.nortem.parsers.transformations;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface Transformation extends Function<Map<String, Object>, Map<String, Object>> {};
