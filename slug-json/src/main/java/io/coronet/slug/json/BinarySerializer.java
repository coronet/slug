package io.coronet.slug.json;

import io.coronet.bytes.Bytes;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 */
public class BinarySerializer implements Serializer<Bytes> {

    @Override
    public boolean canSerialize(Object value) {
        return (value instanceof Bytes);
    }

    @Override
    public void serialize(
            Bytes value,
            JsonGenerator generator,
            Serializers serializers) throws IOException {

        try (InputStream stream = value.asInputStream()) {
            generator.writeBinary(stream, value.length());
        }
    }
}
