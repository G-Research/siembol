package uk.co.gresearch.nortem.parsers.transformations;

public interface FieldFilter {
    boolean match(String str);
}
