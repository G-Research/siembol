package uk.co.gresearch.siembol.enrichments.table;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
/**
 * An object that provides metadata about an enrichment table
 *
 * <p>This class represents a metadata of a enrichment table such as the number of row, the number of columns,
 * table size and number of values.
 *
 * @author  Marian Novotny
 *
 */
public class TableMetadata {
    public static final String TABLE_METADATA_KEY = "table_metadata";
    public static final String LAST_UPDATE_FIELD_NAME = "last_update";
    public static final String NUMBER_OF_ROWS_FIELD_NAME = "number_of_rows";
    public static final String NUMBER_OF_FIELDS_FIELD_NAME = "number_of_fields";
    public static final String NUMBER_OF_VALUES_FIELD_NAME = "number_of_values";
    public static final String SIZE_FIELD_NAME = "table_size";

    private final long lastUpdate;
    private int numberOfRows;
    private int numberOfFields;
    private int numberOfValues;
    private int size;

    public TableMetadata() {
        this.lastUpdate = System.currentTimeMillis();
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public void setNumberOfValues(int numberOfValues) {
        this.numberOfValues = numberOfValues;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Pair<String, String>> getValues() {
        return List.of(
                ImmutablePair.of(LAST_UPDATE_FIELD_NAME, String.valueOf(lastUpdate)),
                ImmutablePair.of(NUMBER_OF_ROWS_FIELD_NAME, String.valueOf(numberOfRows)),
                ImmutablePair.of(NUMBER_OF_FIELDS_FIELD_NAME, String.valueOf(numberOfFields)),
                ImmutablePair.of(NUMBER_OF_VALUES_FIELD_NAME, String.valueOf(numberOfValues)),
                ImmutablePair.of(SIZE_FIELD_NAME, String.valueOf(size))
        );
    }
}
