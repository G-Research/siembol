package uk.co.gresearch.siembol.parsers.extractors;

/**
 * An object that represents structure for handling key value indices
 *
 * <p>This object represents helper structure for handling key value indices used in KeyValueExtractor
 *
 * @author  Marian Novotny
 * @see KeyValueExtractor.Builder
 */
public class KeyValueIndices {
    @FunctionalInterface
    public interface IndexOf {
        KeyValueIndices apply(String str, int from);
    }

    private final int keyIndex;
    private final int valueIndex;

    public KeyValueIndices(int keyIndex, int valueIndex, int stringLength) {
        this.keyIndex = keyIndex;
        this.valueIndex = valueIndex <= keyIndex ? stringLength : valueIndex;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public boolean isValid() {
        return keyIndex > 0;
    }

    static public KeyValueIndices invalid() {
        return new KeyValueIndices(-1, -1, -1);
    }
}
