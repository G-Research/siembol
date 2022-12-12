package uk.co.gresearch.siembol.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
/**
 * An enum for representing a sleep time unit type
 *
 * <p>This enum is used for json (de)serialisation of a sleep time unit type used in a sleep evaluator.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #SECONDS
 * @see #MILLI_SECONDS
 */
@Attributes(title = "sleep time unit type", description = "Time unit type for sleep evaluator")
public enum SleepTimeUnitTypeDto {
        @JsonProperty("seconds") SECONDS("seconds", 1000L),
        @JsonProperty("milli_seconds") MILLI_SECONDS("milli_seconds", 1L);

        private final String name;
        private final long milliSeconds;
        SleepTimeUnitTypeDto(String name, long milliSeconds) {
            this.name = name;
            this.milliSeconds = milliSeconds;
        }

        public long convertToMs(long timeUnits) {
            return timeUnits * milliSeconds;
        }

        @Override
        public String toString() {
            return name;
        }
}
