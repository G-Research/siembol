package uk.co.gresearch.siembol.configeditor.model;
/**
 * An object that represents additional config testers
 *
 * <p>This class represents additional config testers.
 *
 * @author  Marian Novotny
 */
public class AdditionalConfigTesters {
    private SparkHdfsTesterProperties sparkHdfs;

    public SparkHdfsTesterProperties getSparkHdfs() {
        return sparkHdfs;
    }

    public void setSparkHdfs(SparkHdfsTesterProperties sparkHdfs) {
        this.sparkHdfs = sparkHdfs;
    }
}
