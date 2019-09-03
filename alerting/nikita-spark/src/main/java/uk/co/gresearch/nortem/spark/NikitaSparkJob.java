package uk.co.gresearch.nortem.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NikitaSparkJob implements Serializable {
    private final JavaRDD<String> rdd;
    private final NikitaSparkEngine nikita;
    private final int maxResult;

    public NikitaSparkJob(Builder builder) {
        this.rdd = builder.rdd;
        this.nikita = builder.nikita;
        this.maxResult = builder.maxResult;
    }

    NikitaSparkResult eval() {
        return rdd
                .filter(x -> !x.isEmpty())
                .map(x -> nikita.eval(x, maxResult))
                .filter(x -> !x.isEmpty())
                .fold(NikitaSparkResult.emptyResult(maxResult), (x , y) -> x.merge(y));
    }

    public static class Builder {
        private static final String MISSING_ARGUMENTS_MSG = "Missing arguments for nikita spark job";
        private static final String WRONG_DATE_MSG = "date_from should not be after date_to";
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
        private int maxResult = 100;
        private String rules;
        private String logPath;
        private String suffix = "snappy";
        private JavaSparkContext sc;
        private JavaRDD<String> rdd;
        private String sourceType;
        private String fromDate;
        private String toDate;
        private NikitaSparkEngine nikita;

        public Builder nikitaRules(String rules) {
            this.rules = rules;
            return this;
        }

        public Builder maxResult(int maxResult) {
            this.maxResult = maxResult;
            return this;
        }

        public Builder sparkContext(JavaSparkContext sc) {
            this.sc = sc;
            return this;
        }

        public Builder logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder fromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder toDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        public Builder rdd(JavaRDD<String> rdd) {
            this.rdd = rdd;
            return this;
        }

        @SuppressWarnings("unchecked")
        public NikitaSparkJob build() throws Exception {
            if (rules == null
                    || logPath == null
                    || sc == null
                    || sourceType == null
                    || toDate == null
                    || fromDate == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
            }

            nikita = new NikitaSparkEngine(rules);
            if (rdd == null) {
                List<String> paths = getPaths(logPath + sourceType, suffix, fromDate, toDate);
                List<JavaRDD<String>> dateRddList = paths.stream()
                        .map(x -> sc.textFile(x))
                        .collect(Collectors.toList());

                rdd = sc.union(dateRddList.toArray(new JavaRDD[dateRddList.size()]));
            }
            return new NikitaSparkJob(this);
        }

        private List<String> getPaths(String logPrefix, String suffix, String fromDate, String toDate) {
            List<String> paths = new ArrayList<>();
            LocalDate start = LocalDate.from(DATE_FORMATTER.parse(fromDate));
            LocalDate end = LocalDate.from(DATE_FORMATTER.parse(toDate));
            if (start.isAfter(end)) {
                throw new IllegalArgumentException(WRONG_DATE_MSG);
            }

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                paths.add(String.format("%s/%s/*.%s", logPrefix, DATE_FORMATTER.format(date), suffix));
            }
            return paths;
        }
    }
}
