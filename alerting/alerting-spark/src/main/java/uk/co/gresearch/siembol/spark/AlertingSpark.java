package uk.co.gresearch.siembol.spark;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public class AlertingSpark {
    private static final String OUT_MSG = "Matches total: %d\n, Exceptions total:%d\n, Matches: %s\n, Exceptions: %s\n";
    private static final String APP_NAME = "AlertingSpark";
    private static final String MISSING_ATTRIBUTES = "Missing testing attributes";
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || args[0] == null) {
            throw new IllegalArgumentException(MISSING_ATTRIBUTES);
        }

        String input = new String(Base64.getDecoder().decode(args[0]));
        AlertingSparkAttributes attributes = new ObjectMapper()
                .readerFor(AlertingSparkAttributes.class)
                .readValue(input);

        JavaSparkContext sc = new JavaSparkContext(new SparkConf()
                .setAppName(APP_NAME));

        AlertingSparkJob job = new AlertingSparkJob.Builder()
                .sparkContext(sc)
                .suffix(attributes.getSuffix())
                .logPath(attributes.getLogPath())
                .maxResult(attributes.getMaxResult())
                .alertingRules(attributes.getRules())
                .fromDate(attributes.getFromDate())
                .toDate(attributes.getToDate())
                .sourceType(attributes.getSourceType())
                .build();

        AlertingSparkResult ret = job.eval();
        sc.close();

        System.out.printf(OUT_MSG,
                ret.getMatchesTotal(),
                ret.getExceptionsTotal(),
                String.join("\n", ret.getMatches()),
                String.join("\n", ret.getExceptions()));
    }
}
