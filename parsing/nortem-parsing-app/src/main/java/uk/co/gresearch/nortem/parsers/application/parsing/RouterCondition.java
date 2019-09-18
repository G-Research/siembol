package uk.co.gresearch.nortem.parsers.application.parsing;

import java.io.Serializable;
import java.util.function.Function;

public interface RouterCondition extends Serializable, Function<String, Boolean> {}
