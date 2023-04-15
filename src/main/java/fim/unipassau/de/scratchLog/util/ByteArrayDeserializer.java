/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.util;

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
