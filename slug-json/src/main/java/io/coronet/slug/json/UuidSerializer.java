package io.coronet.slug.json;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A serializer that stores UUIDs as 16 bytes of binary data.
 */
public final class UuidSerializer implements Serializer<UUID> {

    @Override
    public boolean canSerialize(Object value) {
        return (value instanceof UUID);
    }

    @Override
    public void serialize(
            UUID value,
            JsonGenerator generator,
            Serializers serializers) throws IOException {

        long high = value.getMostSignificantBits();
        long low = value.getLeastSignificantBits();

        byte[] bytes = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(bytes);

        bb.putLong(high);
        bb.putLong(low);

        generator.writeBinary(bytes);
    }
}
