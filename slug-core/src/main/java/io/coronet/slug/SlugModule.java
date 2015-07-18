package io.coronet.slug;

import io.coronet.bytes.Bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 *
 */
public interface SlugModule {

    void serializeTo(Object object, OutputStream stream) throws IOException;

    Object deserializeTo(InputStream stream, Type target) throws IOException;

    default Bytes serialize(Object object) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            serializeTo(object, buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Bytes.wrap(buffer.toByteArray());
    }


    default Object deserialize(Bytes bytes) throws IOException {
        return deserializeTo(bytes, null);
    }

    default Object deserialize(InputStream stream) throws IOException {
        return deserializeTo(stream, null);
    }

    default Object deserializeTo(Bytes bytes, Type target)
            throws IOException {

        try (InputStream stream = bytes.asInputStream()) {
            return deserializeTo(stream, target);
        }
    }
}
