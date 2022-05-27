package uk.co.gresearch.siembol.spark;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.gresearch.siembol.common.model.AlertingSparkTestingAttributesDto;

import java.util.Base64;

public class AlertingSpark {
    private static final String APP_NAME = "SiembolAlertingSpark";
    private static final String MISSING_ATTRIBUTES = "Missing testing attributes";
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || args[0] == null) {
            throw new IllegalArgumentException(MISSING_ATTRIBUTES);
        }

        String input = new String(Base64.getDecoder().decode(args[0]));
        AlertingSparkTestingAttributesDto attributes = new ObjectMapper()
                .readerFor(AlertingSparkTestingAttributesDto.class)
                .readValue(input);

        JavaSparkContext sc = new JavaSparkContext(new SparkConf()
                .setAppName(APP_NAME));

        AlertingSparkJob job = new AlertingSparkJob.Builder()
                .sparkContext(sc)
                .maxResultSize(attributes.getMaxResultSize())
                .alertingRules(attributes.getRules())
                .filesPaths(attributes.getFilesPaths())
                .build();

        AlertingSparkResult ret = job.eval();
        sc.close();

        System.out.print(ret.toString());
    }
}
