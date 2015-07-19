package io.coronet.slug;

import io.coronet.bytes.Bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * A {@code SlugModule} is the typical entry point for working with slugs. It
 * wraps a {@link SlugBox} (which can be used for creating slugs ex nihlo),
 * and layers on the ability to serialize and deserialize slugs from a
 * particular format.
 */
public interface SlugModule {

    /**
     * Retrieves the {@code SlugBox} that this module will use to create slugs.
     *
     * @return the wrapped {@code SlugBox}
     */
    SlugBox getSlugBox();

    /**
     * Retrieves the {@code SlugTypeRegistry} that this module will use to
     * resolve in-line types, or null if no type registry is associated.
     *
     * @return the wrapped {@code SlugTypeRegistry}
     */
    default SlugTypeRegistry getTypeRegistry() {
        return null;
    }

    /**
     * Serializes the given object and writes it to the given
     * {@code OutputStream}.
     *
     * @param object the object to serialize
     * @param stream the stream to serialize it to
     * @throws NullPointerException if {@code object} or {@code stream} are null
     * @throws IOException on error writing to the stream
     */
    void serializeTo(Object object, OutputStream stream) throws IOException;

    /**
     * Attempts to deserialize an instance of the given type from the given
     * {@code InputStream}. The returned value may or may not actually be an
     * instance of the requested type, depending on what's found in the
     * {@code InputStream}. It's more of a "guideline."
     *
     * @param stream the stream to read from
     * @param target the target type to deserialize to
     * @return the deserialized object
     * @throws NullPointerException if {@code stream} is null
     * @throws IOException on error reading from the stream or if the
     *             data in the stream cannot be parsed
     */
    Object deserializeTo(InputStream stream, Type target) throws IOException;

    /**
     * Serializes the given object to an in-memory byte array. Implemented by
     * calling {@link #serializeTo(Object, OutputStream)} with a
     * {@code ByteArrayOutputStream} and extracting the resulting bytes.
     *
     * @param object the object to serialize
     * @return the serialized representation of the object
     * @throws NullPointerException if {@code object} is null
     */
    default Bytes serialize(Object object) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            serializeTo(object, buffer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return Bytes.wrap(buffer.toByteArray());
    }

    /**
     * Deserializes an object from the given sequence of bytes. Implemented
     * by calling {@link #deserializeTo(Bytes, Type)} with a null {@code target}
     * type - you'll probably get a {@code Map<String, Object>}.
     *
     * @param bytes the byte array to deserialize from
     * @return the deserialized object
     * @throws NullPointerException if {@code bytes} is null
     * @throws IOException if the given data cannot be parsed
     */
    default Object deserialize(Bytes bytes) throws IOException {
        return deserializeTo(bytes, null);
    }

    /**
     * Deserializes an object from the given {@code InputStream}. Implemented
     * by calling {@link #deserializeTo(InputStream, Type)} with a null
     * {@code target} type - you'll probably get a {@code Map<String, Object>}.
     *
     * @param stream the stream to read from
     * @return the deserialized object
     * @throws NullPointerException if {@code stream} is null
     * @throws IOException on error reading from the stream or if the data it
     *             contains cannot be parsed
     */
    default Object deserialize(InputStream stream) throws IOException {
        return deserializeTo(stream, null);
    }

    /**
     * Attempts to deserialize an instance of the given type from the given
     * byte array. The returned value may or may not actually be an
     * instance of the requested type, depending on what's found in the
     * input data. It's more of a "guideline."
     *
     * @param bytes the byte array to deserialize from
     * @param target the target type to deserialize to
     * @return the deserialized object
     * @throws NullPointerException if {@code bytes} is null
     * @throws IOException if the data cannot be parsed
     */
    default Object deserializeTo(Bytes bytes, Type target)
            throws IOException {

        try (InputStream stream = bytes.asInputStream()) {
            return deserializeTo(stream, target);
        }
    }
}
