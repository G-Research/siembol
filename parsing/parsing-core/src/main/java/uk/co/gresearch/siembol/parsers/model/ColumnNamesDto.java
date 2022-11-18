package uk.co.gresearch.siembol.parsers.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;

import java.util.List;
/**
 * A data transfer object for representing column names in a csv extractor
 *
 * <p>This class is used for json (de)serialisation of column names and
 * for generating json schema from this class using annotations.
 *
 * @author  Marian Novotny
 * @see com.github.reinert.jjschema.Attributes
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see ColumnFilterDto
 */
@Attributes(title = "column names", description = "Names of fields along with a filter")
public class ColumnNamesDto {
    @JsonProperty("column_filter")
    @Attributes(description = "Filter for applying the names. The size of names array is used when filter is not provided")
    private ColumnFilterDto columnFilter;
    @Attributes(required = true,
            description = "Names of fields according to columns order, use a skip_character '_' if you do not want to include the column")
    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public ColumnFilterDto getColumnFilter() {
        return columnFilter;
    }

    public void setColumnFilter(ColumnFilterDto columnFilter) {
        this.columnFilter = columnFilter;
    }
}
