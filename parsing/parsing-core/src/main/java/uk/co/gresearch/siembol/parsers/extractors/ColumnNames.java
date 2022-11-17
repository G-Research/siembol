package uk.co.gresearch.siembol.parsers.extractors;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
/**
 * An object that represents structure for column names
 *
 * <p>This object represents helper structure for representing column names used in CSV parser extractor.
 *
 * @author  Marian Novotny
 */
public class ColumnNames {
    private final ArrayList<String> columnNames;
    private final AbstractMap.SimpleEntry<Integer, String> filter;

    public ColumnNames(List<String> columnNames) {
        this(columnNames, null);
    }

    public ColumnNames(List<String> columnNames,
                       AbstractMap.SimpleEntry<Integer, String> filter) {
        this.columnNames = new ArrayList<>(columnNames);
        if (filter != null &&
                (filter.getKey() >= columnNames.size()
                        || filter.getValue() == null)) {
            throw new IllegalArgumentException("Wrong column names filter");
        }
        this.filter = filter;
    }

    public boolean matched(ArrayList<Object> values) {
        return columnNames.size() == values.size()
                && (filter == null || (
                        filter.getKey() < values.size()
                                && filter.getValue().equals(values.get(filter.getKey()))));
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public static ArrayList<String> getNames(List<ColumnNames> columnNamesList,
                                             ArrayList<Object> values) {
        for (ColumnNames names : columnNamesList) {
            if (names.matched(values)) {
                return names.getColumnNames();
            }
        }
        return new ArrayList<>();
    }
}
