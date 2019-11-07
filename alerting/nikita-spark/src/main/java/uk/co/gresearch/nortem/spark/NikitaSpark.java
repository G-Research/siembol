package uk.co.gresearch.nortem.spark;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public class NikitaSpark {
    private static final String OUT_MSG = "Matches total: %d\n, Exceptions total:%d\n, Matches: %s\n, Exceptions: %s\n";
    private static final String APP_NAME = "NikitaSpark";
    private static final String MISSING_ATTRIBUTES = "Missing testing attributes";
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || args[0] == null) {
            throw new IllegalArgumentException(MISSING_ATTRIBUTES);
        }

        String input = new String(Base64.getDecoder().decode(args[0]));
        NikitaSparkAttributes attributes = new ObjectMapper()
                .readerFor(NikitaSparkAttributes.class)
                .readValue(input);

        JavaSparkContext sc = new JavaSparkContext(new SparkConf()
                .setAppName(APP_NAME));

        NikitaSparkJob job = new NikitaSparkJob.Builder()
                .sparkContext(sc)
                .suffix(attributes.getSuffix())
                .logPath(attributes.getLogPath())
                .maxResult(attributes.getMaxResult())
                .nikitaRules(attributes.getRules())
                .fromDate(attributes.getFromDate())
                .toDate(attributes.getToDate())
                .sourceType(attributes.getSourceType())
                .build();

        NikitaSparkResult ret = job.eval();
        sc.close();

        System.out.printf(OUT_MSG,
                ret.getMatchesTotal(),
                ret.getExceptionsTotal(),
                String.join("\n", ret.getMatches()),
                String.join("\n", ret.getExceptions()));
    }
}
