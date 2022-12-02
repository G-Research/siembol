package uk.co.gresearch.siembol.common.model.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.BaseEncoding;

import java.util.function.Function;
/**
 * An enum for representing a log encoding used in parsing testing
 *
 * <p>This enum is used for json (de)serialisation of a log encoding used in parsing testing.
 *
 * @author  Marian Novotny
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see #UTF8_STRING
 * @see #HEX_STRING
 */
public enum LogEncodingDto {
    @JsonProperty("utf8_string") UTF8_STRING("utf8_string",  x -> x.getBytes()),
    @JsonProperty("hex_string") HEX_STRING("hex_string", x -> BaseEncoding.base16().decode(x));
    private final String name;
    private final Function<String, byte[]> decoder;

    LogEncodingDto(String name,  Function<String, byte[]> decoder) {
        this.name = name;
        this.decoder = decoder;
    }

    @Override
    public String toString() {
        return name;
    }

    public byte[] decode(String log) {
        return decoder.apply(log);
    }
}
