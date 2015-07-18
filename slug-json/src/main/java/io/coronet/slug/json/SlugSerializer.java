package io.coronet.slug.json;

import io.coronet.slug.Slug;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 */
public final class SlugSerializer implements Serializer<Slug<?>> {

    @Override
    public boolean canSerialize(Object value) {
        return (value instanceof Slug<?>);
    }

    @Override
    public void serialize(
            Slug<?> value,
            JsonGenerator generator,
            Serializers serializers) throws IOException {

        generator.writeStartObject();

        for (Map.Entry<String, Object> entry : value.entrySet()) {
            generator.writeFieldName(entry.getKey());
            serializers.serialize(entry.getValue(), generator);
        }

        generator.writeEndObject();
    }
}
