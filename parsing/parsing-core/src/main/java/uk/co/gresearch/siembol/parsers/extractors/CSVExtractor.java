package uk.co.gresearch.siembol.parsers.extractors;

import com.google.common.base.Splitter;

import java.util.*;
import java.util.stream.Collectors;
/**
 * An object for extracting fields using CSV extracting
 *
 * <p>This derived class of ParserExtractor provides functionality for CSV (Comma Separated Values) extracting.
 * It uses column names list for adding field names supporting to skip some columns.
 * It supports handling quotes.
 *
 * @author  Marian Novotny
 * @see ParserExtractor
 */
public class CSVExtractor extends ParserExtractor {
    private static final String UNKNOWN_COLUMN_NAME_PREFIX = "unknown";
    private static final String EMPTY_STRING = "";
    private static final char QUOTE = '"';

    private final String wordDelimiter;
    private final ArrayList<ColumnNames> columnNamesList;
    private final String skippingColumnName;

    private CSVExtractor(Builder<?> builder) {
        super(builder);
        this.wordDelimiter = builder.wordDelimiter;
        this.columnNamesList = new ArrayList<>(builder.columnNamesList);
        this.skippingColumnName = builder.skippingColumnName;
    }

    protected ArrayList<Object> getValues(String message, char delimiter) {
        ArrayList<Object> values = new ArrayList<>();
        int offset = 0;
        Optional<Character> quote = Optional.of(QUOTE);

        while (offset < message.length()) {
            int delimiterOffset = ParserExtractorLibrary.indexOf(
                    message, delimiter, offset, quote, Optional.empty());
            if (delimiterOffset == -1) {
                delimiterOffset = message.length();
            }

            Object value = getValue(message.substring(offset, delimiterOffset));
            values.add(value);
            offset = delimiterOffset + 1;
        }

        if (!message.isEmpty()
                && message.charAt(message.length() - 1) == delimiter) {
            //NOTE: if the last character is delimiter we would like to add the last empty column
            values.add(EMPTY_STRING);
        }
        return values;
    }

    protected ArrayList<Object> getValues(String message, String delimiter) {
        ArrayList<Object> values = new ArrayList<>();
        Iterable<String> tmp = Splitter.on(delimiter).split(message);
        for (String strValue : tmp) {
            Object value = getValue(strValue);
            values.add(value);
        }

        return values;
    }

    /**
     * Extracts a message string using CSV parsing
     *
     * @param message input message to be extracted
     * @return map of string to object with extracted fields
     */
    @Override
    protected Map<String, Object> extractInternally(String message) {
        Map<String, Object> ret = new HashMap<>();

        ArrayList<Object> values = wordDelimiter.length() == 1
                ? getValues(message, wordDelimiter.charAt(0))
                : getValues(message, wordDelimiter);

        ArrayList<String> currentNames = ColumnNames.getNames(columnNamesList, values);
        if (currentNames.size() < values.size()) {
            if (shouldThrowExceptionOnError()) {
                throw new IllegalStateException("Unknown column names");
            }
            for (int i = currentNames.size(); i < values.size(); i++) {
                currentNames.add(String.format("%s_%s_%d",
                        UNKNOWN_COLUMN_NAME_PREFIX,
                        getName(),
                        i + 1));
            }
        }

        for (int i = 0; i < values.size(); i++) {
            if (!skippingColumnName.equals(currentNames.get(i))) {
                if (!shouldSkipEmptyValues() || !EMPTY_STRING.equals(values.get(i))) {
                    ret.put(currentNames.get(i), values.get(i));
                }
            }
        }

        return ret;
    }

    private static boolean checkNames(List<String> names, String skippingColumnName) {
        List<String> importantNames = names
                .stream()
                .filter(x -> !skippingColumnName.equals(x))
                .collect(Collectors.toList());

        Set<String> namesSet = new HashSet<>(importantNames);
        if (namesSet.isEmpty()) {
            throw new IllegalArgumentException("Empty column names");
        }

        if (namesSet.size() != importantNames.size()) {
            throw new IllegalArgumentException("Column names should be unique");
        }

        return true;
    }

    /**
     * Creates a CSV extractor builder instance
     *
     * @return CSV builder instance
     */
    public static Builder<CSVExtractor> builder() {

        return new Builder<>() {
            @Override
            public CSVExtractor build() {
                if (this.columnNamesList == null ||
                        this.columnNamesList.isEmpty()) {
                    throw new IllegalArgumentException("Empty column names");
                }
                for (ColumnNames columnNames : columnNamesList) {
                    if (!checkNames(columnNames.getColumnNames(), skippingColumnName)) {
                        throw new IllegalArgumentException("Wrong column names");
                    }
                }

                return new CSVExtractor(this);
            }
        };
    }

    /**
     * A builder for CSV parser extractor
     *
     * <p>This class is using Builder pattern.
     *
     * @author  Marian Novotny
     */
    public static abstract class Builder<T extends CSVExtractor>
            extends ParserExtractor.Builder<T> {
        protected String skippingColumnName = "_";
        protected String wordDelimiter = ",";
        protected List<ColumnNames> columnNamesList = new ArrayList<>();

        /**
         * Sets word delimiter for delimiting a row value (',' by default)
         *
         * @param wordDelimiter word delimiter for delimiting a row value
         * @return this builder
         */
        public CSVExtractor.Builder<T> wordDelimiter(String wordDelimiter) {
            this.wordDelimiter = wordDelimiter;
            return this;
        }

        /**
         * Sets column names
         *
         * @param columnNamesList list of column names
         * @return this builder
         */
        public CSVExtractor.Builder<T> columnNames(List<ColumnNames> columnNamesList) {
            if (columnNamesList == null
                    || columnNamesList.isEmpty()) {
                throw new IllegalArgumentException("Column names should not be empty");
            }

            this.columnNamesList = columnNamesList;
            return this;
        }

        /**
         * Sets skipping column name string
         *
         * @param skippingColumnName name of the column that will be skipped during extracting
         * @return this builder
         */
        public CSVExtractor.Builder<T> skippingColumnName(String skippingColumnName) {
            if (skippingColumnName == null) {
                throw new IllegalArgumentException("The skipping column name should not be null");
            }
            this.skippingColumnName = skippingColumnName;
            return this;
        }
    }
}
