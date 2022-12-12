package uk.co.gresearch.siembol.response.evaluators.markdowntable;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.table.TableRow;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import uk.co.gresearch.siembol.common.utils.EvaluationLibrary;
import uk.co.gresearch.siembol.common.utils.FieldFilter;
import uk.co.gresearch.siembol.common.utils.PatternFilter;
import uk.co.gresearch.siembol.response.common.*;

import java.util.*;
import java.util.function.Function;

import static uk.co.gresearch.siembol.response.common.RespondingResult.StatusCode.OK;
/**
 * An object for evaluating response alerts
 *
 * <p>This class implements Evaluable interface, and it is used in a response rule.
 * The table formatter evaluator generates a string with a Markdown table from a json object or an array from the alert.
 *
 * @author  Marian Novotny
 * @see Evaluable
 */
public class TableFormatter implements Evaluable {
    private static final String MISSING_VALUE = "";
    private static final String TABLE_FORMAT_MESSAGE = "%s\n%s";
    private static final int TABLE_NAME_SIZE = 4;
    private final String tableName;
    private final String fieldName;
    private final Function<ResponseAlert, String> rowFormatter;

    TableFormatter(Builder builder) {
        this.tableName = builder.tableName;
        this.fieldName = builder.fieldName;
        this.rowFormatter = builder.rowFormatter;
    }

    private String formatTable(ResponseAlert alert) {
        String tableString = rowFormatter.apply(alert);
        String currentName = EvaluationLibrary.substitute(alert, tableName).get();
        String tableHeading = new Heading(currentName, TABLE_NAME_SIZE).toString();
        return String.format(TABLE_FORMAT_MESSAGE, tableHeading, tableString);
    }

    private static String formatObject(ResponseAlert responseAlert, String firstColumn,
                                       String secondColumn, FieldFilter fieldFilter) {

        Table.Builder tableBuilder = new Table.Builder()
                .withAlignment(Table.ALIGN_CENTER)
                .addRow(firstColumn, secondColumn);

        responseAlert.keySet().stream()
                .filter(fieldFilter::match)
                .sorted()
                .forEach(x -> tableBuilder.addRow(x, responseAlert.get(x).toString()));

        return tableBuilder.build().toString();
    }

    @SuppressWarnings("unchecked")
    private static String formatArray(ResponseAlert responseAlert, String arrayField, FieldFilter fieldFilter) {
        Table.Builder tableBuilder = new Table.Builder();
        List<Map<String, Object>> arrayObj;
        try {
            arrayObj = responseAlert.get(arrayField) instanceof List<?>
                    ? (List<Map<String, Object>>)responseAlert.get(arrayField)
                    : new ArrayList<>();
        } catch (Exception e) {
            arrayObj = new ArrayList<>();
        }

        Set<String> columnsSet = new HashSet<>();
        arrayObj.forEach(x -> columnsSet.addAll(x.keySet()));
        columnsSet.removeIf(field -> !fieldFilter.match(field));
        List<String> columnsList = new ArrayList<>(columnsSet);
        Collections.sort(columnsList);

        if (!columnsList.isEmpty()) {
            tableBuilder.withAlignment(Table.ALIGN_CENTER).addRow(new TableRow<>(columnsList));
            for (Map<String, Object> map : arrayObj) {
                List<String> row = new ArrayList<>();
                for (String field : columnsList) {
                    row.add(map.containsKey(field) ? map.get(field).toString() : MISSING_VALUE);
                }
                tableBuilder.addRow(new TableRow<>(row));
            }
        }

        return tableBuilder.build().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RespondingResult evaluate(ResponseAlert alert) {
        String formattedTable = formatTable(alert);
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        alert.put(fieldName, formattedTable);
        attributes.setAlert(alert);
        attributes.setResult(ResponseEvaluationResult.MATCH);
        return new RespondingResult(OK, attributes);
    }

    public static class Builder {
        private static final String MISSING_ARGUMENT_MSG = "missing table formatter arguments";
        private static final String UNSUPPORTED_ARGUMENT_MSG = "Unsupported combination of arguments in tableformatter";
        private String tableName;
        private String fieldName;
        private String arrayFieldName;
        private String firstColumnName;
        private String secondColumnName;
        private FieldFilter fieldFilter = x -> true;
        private Function<ResponseAlert, String> rowFormatter;

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder arrayFieldName(String arrayFieldName) {
            if (firstColumnName != null || secondColumnName != null) {
                throw new IllegalArgumentException(UNSUPPORTED_ARGUMENT_MSG);
            }
            this.arrayFieldName = arrayFieldName;
            return this;
        }

        public Builder columnNames(String firstColumnName, String secondColumnName) {
            if (arrayFieldName != null) {
                throw new IllegalArgumentException(UNSUPPORTED_ARGUMENT_MSG);
            }
            this.firstColumnName = firstColumnName;
            this.secondColumnName = secondColumnName;
            return this;
        }

        public Builder patternFilter(List<String> includingFields, List<String> excludingFields) {
            fieldFilter = PatternFilter.create(includingFields, excludingFields);
            return this;
        }

        public TableFormatter build() {
            if (tableName == null
                    || fieldName == null
                    || (arrayFieldName == null && (firstColumnName == null || secondColumnName == null))) {
                throw new IllegalArgumentException(MISSING_ARGUMENT_MSG);
            }

            rowFormatter = arrayFieldName != null
                    ? x -> formatArray(x, arrayFieldName, fieldFilter)
                    : x -> formatObject(x, firstColumnName, secondColumnName, fieldFilter);
            return new TableFormatter(this);
        }
    }
}
