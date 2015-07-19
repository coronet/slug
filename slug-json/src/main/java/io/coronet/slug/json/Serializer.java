package io.coronet.slug.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A strategy for serializing user-facing objects to JSON. Implement me to
 * provide custom serialization for data types that don't map directly to
 * JSON types.
 */
public interface Serializer<T> {

    /**
     * Checks whether this serializer knows how to serialize the given value.
     * This should almost certainly assert that the value is an instance of
     * {@code T}, but may do additional checks as well.
     *
     * @param value the value to serialize
     * @return true if this serializer knows how to serialize it
     */
    boolean canSerialize(Object value);

    /**
     * Serializes the given value to the given {@code JsonGenerator}.
     *
     * @param value the value to serialize
     * @param generator the {@code JsonGenerator} to serialize it to
     * @param serializers the set of serializers to use for recursive
     *            serialization of collection elements
     * @throws IOException on error writing to the {@code generator}
     */
    void serialize(
            T value,
            JsonGenerator generator,
            Serializers serializers) throws IOException;
}
