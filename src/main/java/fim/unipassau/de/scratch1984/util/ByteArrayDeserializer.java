package fim.unipassau.de.scratch1984.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Base64;

/**
 * Custom deserialization class used to deserialize base64 encoded strings to byte arrays.
 */
public class ByteArrayDeserializer extends StdDeserializer<byte[]> {

    /**
     * The default constructor for the deserializer.
     */
    public ByteArrayDeserializer() {
        super(byte[].class);
    }

    /**
     * Deserializes the base64 encoded content into a byte array and returns it.
     *
     * @param jsonParser The parser containing the encoded string.
     * @param deserializationContext The deserialization context.
     * @return The decoded byte array.
     * @throws IOException if the deserialization failed.
     */
    @Override
    public byte[] deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
            throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String base64 = node.asText();
        return Base64.getDecoder().decode(base64);
    }

}
