package io.coronet.slug.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A serializer that turns {@code List}s into JSON arrays, recursively
 * serializing the elements of the list.
 */
public final class ListSerializer implements Serializer<List<?>> {

    @Override
    public boolean canSerialize(Object value) {
        return (value instanceof List<?>);
    }

    @Override
    public void serialize(
            List<?> value,
            JsonGenerator generator,
            Serializers serializers) throws IOException {

        generator.writeStartArray();

        for (Object element : value) {
            serializers.serialize(element, generator);
        }

        generator.writeEndArray();
    }
}
