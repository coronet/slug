package io.coronet.slug.json;

import io.coronet.bytes.Bytes;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * A deserializer that turns {@code Strings} or {@code byte[]}s to immutable
 * {@code Bytes}. A {@code JsonParser} may return either for binary data,
 * depending on its underlying format. If the input is a {@code String}, it's
 * Base64 decoded.
 */
public final class BinaryDeserializer implements Deserializer {

    @Override
    public boolean canDeserialize(Object value, Type target) {
        if (target != Bytes.class) {
            return false;
        }
        return (value instanceof String || value instanceof byte[]);
    }

    @Override
    public Object deserialize(Object value, Type target) {
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

        return Bytes.wrap(bytes);
    }
}
