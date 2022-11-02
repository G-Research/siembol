package uk.co.gresearch.siembol.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
/**
 * An object for a spark job that evaluates events using an alerting spark engine
 *
 * <p>This class implements Serializable interface.
 *  It uses initialised AlertingSparkEngine instance to evaluate
 *  resilient distributed dataset (RDD) of json strings of events using the MapReduce technique.
 *  AlertingSparkEngine, RDD of events and a Spark context  are provided by the builder in the constructor.
 *
 * @author Marian Novotny
 * @see AlertingSparkEngine
 * @see JavaRDD
 * @see Builder
 * @see AlertingSparkResult
 *
 */
public class AlertingSparkJob implements Serializable {
    private static final long serialVersionUID = 1L;
    private final JavaRDD<String> rdd;
    private final AlertingSparkEngine alertingSparkEngine;
    private final int maxResult;

    public AlertingSparkJob(Builder builder) {
        this.rdd = builder.rdd;
        this.alertingSparkEngine = builder.alertingSparkEngine;
        this.maxResult = builder.maxResult;
    }

    AlertingSparkResult eval() {
        return rdd
                .filter(x -> !x.isEmpty())
                .map(x -> alertingSparkEngine.eval(x, maxResult))
                .filter(x -> !x.isEmpty())
                .fold(AlertingSparkResult.emptyResult(maxResult), AlertingSparkResult::merge);
    }

    /**
     * An object for construction AlertingSparkJob instance
     *
     * <p>This class uses Builder pattern.
     *  It initialises AlertingSparkEngine from rules, RDD of events from files paths and a Spark context.
     *
     * @author Marian Novotny
     * @see AlertingSparkEngine
     * @see JavaRDD
     * @see AlertingSparkJob
     * @see JavaSparkContext
     *
     */
    public static class Builder {
        private static final String MISSING_ARGUMENTS_MSG = "Missing arguments for alerts spark job";
        private static final String EMPTY_FILES_PATHS_MSG = "Files paths are empty";
        private int maxResult = 100;
        private String rules;

        private JavaSparkContext sc;
        private JavaRDD<String> rdd;

        private List<String> filesPaths;
        private AlertingSparkEngine alertingSparkEngine;

        public Builder alertingRules(String rules) {
            this.rules = rules;
            return this;
        }

        public Builder maxResultSize(int maxResult) {
            this.maxResult = maxResult;
            return this;
        }

        public Builder filesPaths(List<String> filesPaths) {
            this.filesPaths = filesPaths;
            return this;
        }

        public Builder sparkContext(JavaSparkContext sc) {
            this.sc = sc;
            return this;
        }

        Builder rdd(JavaRDD<String> rdd) {
            this.rdd = rdd;
            return this;
        }


        @SuppressWarnings({"unchecked", "rawtypes"})
        public AlertingSparkJob build() throws Exception {
            if (rules == null || sc == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
            }

            alertingSparkEngine = new AlertingSparkEngine(rules);
            if (rdd == null) {
                if (filesPaths == null || filesPaths.isEmpty()) {
                    throw new IllegalArgumentException(EMPTY_FILES_PATHS_MSG);
                }

                List<JavaRDD<String>> dateRddList = filesPaths.stream()
                        .map(x -> sc.textFile(x))
                        .collect(Collectors.toList());

                rdd = sc.union(dateRddList.toArray(new JavaRDD[dateRddList.size()]));
            }

            return new AlertingSparkJob(this);
        }
    }
}
