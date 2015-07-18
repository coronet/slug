package io.coronet.slug.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 */
public final class Serializers {

    public static Builder builder() {
        return new Builder();
    }

    private final List<Serializer<?>> serializers;

    public Serializers(List<Serializer<?>> serializers) {
        this.serializers = serializers;
    }

    public Serializer<Object> getSerializer(Object value) {

        for (int i = serializers.size() - 1; i >= 0; --i) {
            Serializer<?> serializer = serializers.get(i);
            if (serializer.canSerialize(value)) {
                @SuppressWarnings("unchecked")
                Serializer<Object> erased = (Serializer<Object>) serializer;
                return erased;
            }
        }

        throw new IllegalStateException(
                "Don't know how to serialize value " + value + " of type "
                + value.getClass());
    }

    public void serialize(Object value, JsonGenerator generator)
            throws IOException {

        if (value == null) {
            throw new NullPointerException("value");
        }
        if (generator == null) {
            throw new NullPointerException("generator");
        }

        Serializer<Object> serializer = getSerializer(value);
        serializer.serialize(value, generator, this);
    }

    public Builder copy() {
        return new Builder(serializers);
    }

    public static final class Builder {

        private List<Serializer<?>> serializers;
        private boolean copyOnWrite;

        public Builder() {
            serializers = new ArrayList<>();
            copyOnWrite = false;
        }

        private Builder(List<Serializer<?>> serializers) {
            this.serializers = serializers;
            this.copyOnWrite = true;
        }

        public Builder with(Serializer<?> s) {
            if (s == null) {
                throw new NullPointerException("s");
            }

            if (copyOnWrite) {
                serializers = new ArrayList<>(serializers);
                copyOnWrite = false;
            }

            serializers.add(s);
            return this;
        }

        public Serializers build() {
            copyOnWrite = true;
            serializers = Collections.unmodifiableList(serializers);
            return new Serializers(serializers);
        }
    }
}
