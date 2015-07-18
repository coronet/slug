package io.coronet.slug.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 */
public interface Serializer<T> {

    boolean canSerialize(Object value);

    void serialize(
            T value,
            JsonGenerator generator,
            Serializers serializers) throws IOException;
}
