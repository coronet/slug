package io.coronet.slug.json;

import io.coronet.slug.Slug;
import io.coronet.slug.SlugTypeRegistry;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A serializer that knows how to turn Slugs into JSON objects.
 */
public final class SlugSerializer implements Serializer<Slug<?>> {

    private final SlugTypeRegistry registry;

    /**
     * Creates a new {@code SlugSerializer} with no backing type registry. No
     * in-band type information will be emitted.
     */
    public SlugSerializer() {
        this(null);
    }

    /**
     * Creates a new {@code SlugSerializer} with the given type registry. If
     * non-null, the registry will be used to provide in-band type information
     * for registered types in the form of a __type member of type String.
     *
     * @param registry the registry to wrap, or null
     */
    public SlugSerializer(SlugTypeRegistry registry) {
        this.registry = registry;
    }

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

        // If we've got a registry and the slug has no explicit __type
        // member, add a synthetic one based on the registered type name for
        // its type.
        if (registry != null && value.get("__type") == null) {
            String hint = registry.getName(value.type());
            if (hint != null) {
                generator.writeFieldName("__type");
                generator.writeString(hint);
            }
        }

        // Serialize the other members of the slug.
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            if (entry.getValue() != null) {
                generator.writeFieldName(entry.getKey());
                serializers.serialize(entry.getValue(), generator);
            }
        }

        generator.writeEndObject();
    }
}
