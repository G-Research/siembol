package uk.co.gresearch.siembol.parsers.transformations;

public interface FieldFilter {
    boolean match(String str);
}
