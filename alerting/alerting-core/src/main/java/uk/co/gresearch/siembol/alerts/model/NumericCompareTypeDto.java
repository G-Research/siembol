package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.alerts.common.AlertingResult;

import java.util.function.BiPredicate;
/**
 * An enum for representing a comparing type used in numeric matcher
 *
 * <p>This enum is used for json (de)serialisation of comparing type and providing a comparator for the type.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see MatcherDto
 *
 */
@Attributes(title = "numeric compare type", description = "Type of numeric comparison")
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

    /**
     * Provides the comparator predicate for the numeric compare type.
     *
     * @return      the comparator for the compare type
     */
    public BiPredicate<Double, Double> getComparator() {
        return comparator;
    }
}
