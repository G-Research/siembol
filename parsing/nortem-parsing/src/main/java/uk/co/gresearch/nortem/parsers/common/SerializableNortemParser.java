package uk.co.gresearch.nortem.parsers.common;

import uk.co.gresearch.nortem.parsers.factory.ParserFactoryImpl;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static uk.co.gresearch.nortem.parsers.factory.ParserFactoryResult.StatusCode.OK;

public class SerializableNortemParser implements NortemParser, Serializable {
    private static final String WRONG_CONFIG_EXCEPTION_MSG = "Unable to create parser from config %s, Message: %s";
    private transient NortemParser parser;
    private String parserConfig;
    private String sourceType;

    public SerializableNortemParser(String parserConfig) throws Exception {
        this.parserConfig = parserConfig;
        ParserFactoryResult result = ParserFactoryImpl
                .createParserFactory()
                .create(parserConfig);

        if (result.getStatusCode() != OK || result.getAttributes().getNortemParser() == null) {
            throw new IllegalArgumentException(String.format(WRONG_CONFIG_EXCEPTION_MSG,
                    parserConfig,
                    result.getAttributes().getMessage()));
        }
        parser = result.getAttributes().getNortemParser();
        sourceType = result.getAttributes().getParserName();
    }

    @Override
    public List<Map<String, Object>> parse(byte[] message) {
        return parser.parse(message);
    }

    @Override
    public List<Map<String, Object>> parse(String metadata, byte[] message) {
        return parser.parse(metadata, message);
    }


    @Override
    public String getSourceType() {
        return sourceType;
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        os.writeUTF(parserConfig);
    }

    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        parserConfig = is.readUTF();
        try {
            ParserFactoryResult result = ParserFactoryImpl
                    .createParserFactory()
                    .create(parserConfig);
            parser = result.getAttributes().getNortemParser();
            sourceType = result.getAttributes().getParserName();
        } catch (Exception e) {
            throw new IOException();
        }
    }
}
