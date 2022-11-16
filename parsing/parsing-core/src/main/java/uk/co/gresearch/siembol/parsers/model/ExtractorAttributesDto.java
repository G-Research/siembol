package uk.co.gresearch.siembol.parsers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import uk.co.gresearch.siembol.common.constants.SiembolMessageFields;

import java.util.Arrays;
import java.util.List;
/**
 * A data transfer object for representing an attributes of an extractor
 *
 * <p>This class is used for json (de)serialisation of attributes and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see ColumnFilterDto
 */
@Attributes(title = "extractor attributes", description = "Attributes for extractor and related functions")
public class ExtractorAttributesDto {
    @JsonProperty("should_overwrite_fields")
    @Attributes(description = "Extractor will overwrite an existing field with the same name")
    private Boolean shouldOverwrite = false;
    @JsonProperty("should_remove_field")
    @Attributes(description = "Extractor will remove field after the extraction")
    private Boolean shouldRemoveField = false;
    @JsonProperty("remove_quotes")
    @Attributes(description = "Extractor removes quotes in the extracted values")
    private Boolean removeQuotes = true;

    @JsonProperty("skip_empty_values")
    @Attributes(description = "Skipping extracted empty strings")
    private Boolean skipEmptyValues = false;

    @JsonProperty("thrown_exception_on_error")
    @Attributes(description = "Extractor throws exception on error (recommended for testing), otherwise it skips the further processing")
    private Boolean thrownExceptionOnError = false;

    @JsonProperty("regular_expressions")
    @Attributes(description = "List of regular expressions", minItems = 1)
    private List<String> regularExpressions;
    @JsonProperty("should_match_pattern")
    @Attributes(description = "At least one pattern should match otherwise the extractor throws an exception")
    private Boolean shouldMatchPattern = false;
    @JsonProperty("dot_all_regex_flag")
    @Attributes(description = "The regular expression '.' matches any character - including a line terminator")
    private Boolean dotAllRegexFlag = true;

    @JsonProperty("word_delimiter")
    @Attributes(description = "Word delimiter used for splitting words")
    private String wordDelimiter = " ";
    @JsonProperty("key_value_delimiter")
    @Attributes(description = "Key-value delimiter used for splitting key value pairs")
    private String keyValueDelimiter = "=";

    @JsonProperty("escaped_character")
    @Attributes(description = "Escaped character for escaping quotes, delimiters, brackets")
    private Character escapedCharacter = '\\';
    @Attributes(description = "Handling quotes during parsing, e.g., string searching")
    @JsonProperty("quota_value_handling")
    private Boolean quotaHandling = true;
    @Attributes(description = "Strategy for key value extraction: key-value delimiter is found first and then the word delimiter is searched back")
    @JsonProperty("next_key_strategy")
    private Boolean nextKeyStrategy = false;
    @Attributes(description = "Handling escaping during parsing, e.g., string searching")
    @JsonProperty("escaping_handling")
    private Boolean escapingHandling = true;

    @JsonProperty("rename_duplicate_keys")
    @Attributes(description = "Rename duplicate keys instead of overwriting or ignoring fields with the same keys")
    private Boolean renameDuplicateKeys = true;


    @JsonProperty("column_names")
    @Attributes(description = "Specification for selecting right column names")
    private List<ColumnNamesDto> columnNames;
    @Attributes(description = "The name that can be used for skipping fields during extraction")
    @JsonProperty("skipping_column_name")
    private String skippingColumnName = "_";


    @JsonProperty("path_prefix")
    @Attributes(description = "The prefix added to extracted field name")
    private String pathPrefix = "";

    @JsonProperty("nested_separator")
    @Attributes(description = "The separator added during unfolding nested json objects")
    private String nestedSeparator = ":";

    @JsonProperty("regex_select_config")
    @Attributes(description = "The specification of regex_select extractor")
    private RegexSelectDto regexSelectConfig;

    @JsonProperty("time_formats")
    @Attributes(description = "The specification of time formats", minItems = 1)
    private List<TimeFormatDto> timeFormats;


    @JsonProperty("timestamp_field")
    @Attributes(description = "The field used in formatting timestamp")
    private String timestampField = SiembolMessageFields.TIMESTAMP.toString();

    @JsonProperty("string_replace_target")
    @Attributes(description = "Target that will be replaced by replacement")
    private String stringReplaceTarget;
    @JsonProperty("string_replace_replacement")
    @Attributes(description = "Replacement that will replace target")
    private String stringReplaceReplacement;

    @JsonProperty("conversion_exclusions")
    @Attributes(description = "List of fields excluded from string converting", minItems = 1)
    private List<String> conversionExclusions = Arrays.asList(SiembolMessageFields.TIMESTAMP.toString());

    @JsonProperty("at_least_one_query_result")
    @Attributes(description = "At least one query should store its result otherwise the extractor throws an exception")
    private Boolean atLeastOneQueryResult = false;

    @JsonProperty("json_path_queries")
    @Attributes(description = "List of json path queries", minItems = 1)
    private List<JsonPathQueryDto> jsonPathQueries;

    public Boolean getShouldOverwrite() {
        return shouldOverwrite;
    }

    public void setShouldOverwrite(Boolean shouldOverwrite) {
        this.shouldOverwrite = shouldOverwrite;
    }

    public Boolean getShouldRemoveField() {
        return shouldRemoveField;
    }

    public void setShouldRemoveField(Boolean shouldRemoveField) {
        this.shouldRemoveField = shouldRemoveField;
    }

    public List<String> getRegularExpressions() {
        return regularExpressions;
    }

    public void setRegularExpressions(List<String> regularExpressions) {
        this.regularExpressions = regularExpressions;
    }

    public String getWordDelimiter() {
        return wordDelimiter;
    }

    public void setWordDelimiter(String wordDelimiter) {
        this.wordDelimiter = wordDelimiter;
    }

    public String getKeyValueDelimiter() {
        return keyValueDelimiter;
    }

    public void setKeyValueDelimiter(String keyValueDelimiter) {
        this.keyValueDelimiter = keyValueDelimiter;
    }

    public Character getEscapedCharacter() {
        return escapedCharacter;
    }

    public void setEscapedCharacter(Character escapedCharacter) {
        this.escapedCharacter = escapedCharacter;
    }

    public Boolean getQuotaHandling() {
        return quotaHandling;
    }

    public void setQuotaHandling(Boolean quotaHandling) {
        this.quotaHandling = quotaHandling;
    }

    public Boolean getNextKeyStrategy() {
        return nextKeyStrategy;
    }

    public void setNextKeyStrategy(Boolean nextKeyStrategy) {
        this.nextKeyStrategy = nextKeyStrategy;
    }

    public Boolean getEscapingHandling() {
        return escapingHandling;
    }

    public void setEscapingHandling(Boolean escapingHandling) {
        this.escapingHandling = escapingHandling;
    }

    public Boolean getRemoveQuotes() {
        return removeQuotes;
    }

    public void setRemoveQuotes(Boolean removeQuotes) {
        this.removeQuotes = removeQuotes;
    }

    public Boolean getRenameDuplicateKeys() {
        return renameDuplicateKeys;
    }

    public void setRenameDuplicateKeys(Boolean renameDuplicateKeys) {
        this.renameDuplicateKeys = renameDuplicateKeys;
    }

    public List<ColumnNamesDto> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<ColumnNamesDto> columnNames) {
        this.columnNames = columnNames;
    }

    public Boolean getThrownExceptionOnError() {
        return thrownExceptionOnError;
    }

    public void setThrownExceptionOnError(Boolean thrownExceptionOnError) {
        this.thrownExceptionOnError = thrownExceptionOnError;
    }

    public Boolean getShouldMatchPattern() {
        return shouldMatchPattern;
    }

    public void setShouldMatchPattern(Boolean shouldMatchPattern) {
        this.shouldMatchPattern = shouldMatchPattern;
    }

    public Boolean getDotAllRegexFlag() {
        return dotAllRegexFlag;
    }

    public void setDotAllRegexFlag(Boolean dotAllRegexFlag) {
        this.dotAllRegexFlag = dotAllRegexFlag;
    }

    public List<TimeFormatDto> getTimeFormats() {
        return timeFormats;
    }

    public void setTimeFormats(List<TimeFormatDto> timeFormats) {
        this.timeFormats = timeFormats;
    }

    public Boolean getSkipEmptyValues() {
        return skipEmptyValues;
    }

    public void setSkipEmptyValues(Boolean skipEmptyValues) {
        this.skipEmptyValues = skipEmptyValues;
    }

    public String getSkippingColumnName() {
        return skippingColumnName;
    }

    public void setSkippingColumnName(String skippingColumnName) {
        this.skippingColumnName = skippingColumnName;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String getNestedSeparator() {
        return nestedSeparator;
    }

    public void setNestedSeparator(String nestedSeparator) {
        this.nestedSeparator = nestedSeparator;
    }

    public String getStringReplaceTarget() {
        return stringReplaceTarget;
    }

    public void setStringReplaceTarget(String stringReplaceTarget) {
        this.stringReplaceTarget = stringReplaceTarget;
    }

    public String getStringReplaceReplacement() {
        return stringReplaceReplacement;
    }

    public void setStringReplaceReplacement(String stringReplaceReplacement) {
        this.stringReplaceReplacement = stringReplaceReplacement;
    }

    public RegexSelectDto getRegexSelectConfig() {
        return regexSelectConfig;
    }

    public void setRegexSelectConfig(RegexSelectDto regexSelectConfig) {
        this.regexSelectConfig = regexSelectConfig;
    }

    public String getTimestampField() {
        return timestampField;
    }

    public void setTimestampField(String timestampField) {
        this.timestampField = timestampField;
    }

    public List<String> getConversionExclusions() {
        return conversionExclusions;
    }

    public void setConversionExclusions(List<String> conversionExclusions) {
        this.conversionExclusions = conversionExclusions;
    }

    public Boolean getAtLeastOneQueryResult() {
        return atLeastOneQueryResult;
    }

    public void setAtLeastOneQueryResult(Boolean atLeastOneQueryResult) {
        this.atLeastOneQueryResult = atLeastOneQueryResult;
    }

    public List<JsonPathQueryDto> getJsonPathQueries() {
        return jsonPathQueries;
    }

    public void setJsonPathQueries(List<JsonPathQueryDto> jsonPathQueries) {
        this.jsonPathQueries = jsonPathQueries;
    }
}
