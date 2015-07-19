package io.coronet.slug.json;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A serializer that turns {@code Map}s into JSON objects, recursively
 * serializing entries in the map.
 */
public final class MapSerializer implements Serializer<Map<?, ?>> {

    @Override
    public boolean canSerialize(Object value) {
        // TODO: Verify that all keys are Strings here?
        return (value instanceof Map<?, ?>);
    }

    @Override
    public void serialize(
            Map<?, ?> value,
            JsonGenerator generator,
            Serializers serializers) throws IOException {

        generator.writeStartObject();

        for (Map.Entry<?, ?> entry : value.entrySet()) {
            Object key = entry.getKey();
            if (!(key instanceof String)) {
                throw new IllegalStateException(
                        "map key " + key + " is not a String");
            }

            if (entry.getValue() != null) {
                generator.writeFieldName((String) key);
                serializers.serialize(entry.getValue(), generator);
            }
        }

        generator.writeEndObject();
    }
}
