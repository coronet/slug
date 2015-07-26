package io.coronet.slug.json;

import io.coronet.bytes.Bytes;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * A deserializer for UUIDs stored as 16 bytes of binary data.
 */
public final class UuidDeserializer implements Deserializer {

    @Override
    public boolean canDeserialize(Object value, Type target) {
        if (target != UUID.class) {
            return false;
        }
        return (value instanceof String || value instanceof byte[]);
    }

    @Override
    public Object deserialize(
            Object value,
            Type target,
            Deserializers deserializers) {

        byte[] bytes;

        if (value instanceof byte[]) {
            bytes = (byte[]) value;
        } else {
            try {
                // TODO: Worth looking for a more efficient base64 codec?
                bytes = Base64.getDecoder().decode((String) value);
            } catch (IllegalArgumentException e) {
                // TODO: Log a warning.
                return value;
            }
        }

        if (bytes.length != 16) {
            return Bytes.wrap(bytes);
        }

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();

        return new UUID(high, low);
    }
}
