package uk.co.gresearch.siembol.response.evaluators.timeexclusion;

public class TimeExclusionEvaluatorFactoryTest {
    private final String attributes = """
            {
              "timestamp_field": "timestamp",
              "time_zone": "Europe/London",
              "months_of_year_pattern": ".*",
              "days_of_week_pattern": "6|7",
              "hours_of_day_pattern": "7|8|9"
            }
            """;
}
