package uk.co.gresearch.siembol.common.result;
/**
 * An object that represents Siembol attributes
 *
 * <p>This bean object represents Siembol attributes used in a Siembol result.
 *
 * @author  Marian Novotny
 */
public class SiembolAttributes {
    private String jsonSchema;
    private String message;

    public String getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
