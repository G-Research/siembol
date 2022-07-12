package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.function.BiPredicate;

@Attributes(title = "numeric compare operation", description = "Type of time computation")
public enum NumericCompareTypeDto {
    @JsonProperty("equal")
    EQUAL("equal", (x, y) -> x.compareTo(y) == 0),
    @JsonProperty("lesser_equal")
    LESSER_EQUAL("lesser_equal", (x, y) -> x.compareTo(y) <= 0),
    @JsonProperty("lesser")
    LESSER("lesser", (x, y) -> x.compareTo(y) < 0),
    @JsonProperty("greater")
    GREATER("greater", (x, y) -> x.compareTo(y) > 0),
    @JsonProperty("greater_equal")
    GREATER_EQUAL("greater_equal", (x, y) -> x.compareTo(y) >= 0);
    private final String name;
    private final BiPredicate<Double, Double> comparator;

    NumericCompareTypeDto(String name, BiPredicate<Double, Double> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    @Override
    public String toString() {
        return name;
    }

    public BiPredicate<Double, Double> getComparator() {
        return comparator;
    }
}
