package io.coronet.slug.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A set of {@code Serializer}s and a bit of logic to determine which one to
 * use to serialize a particular object.
 */
public final class Serializers {

    /**
     * Creates a new builder for a set of {@code Serializers}. The builder
     * initially wraps a completely empty set of serializers. Use me if you
     * want to completely exclude one or more of the standard serializers.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder initialized with the "standard" set of serializers.
     * The standard serializers are registered with the lowest priority, so any
     * custom serializers added afterwards will take precedence.
     *
     * @return a new standard builder
     */
    public static Builder standard() {
        return new Builder()
                .with(new ScalarSerializer())
                .with(new BinarySerializer())
                .with(new ListSerializer())
                .with(new MapSerializer())
                .with(new SlugSerializer());
    }

    private final List<Serializer<?>> serializers;

    private Serializers(List<Serializer<?>> serializers) {
        this.serializers = serializers;
    }

    /**
     * Gets an appropriate serializer for the given object. Implemented by
     * calling the {@link Serializer#canSerialize(Object)} method of each
     * registered serializer (in the reverse order they were registered) and
     * returning the first one that matches. Throws an
     * {@code IllegalStateException} if no matching serializer is found.
     *
     * @param value the value being serialized
     * @return an appropriate serializer
     * @throws IllegalStateException if no matching serializer is found
     */
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

    /**
     * A convenience method that calls {@link #getSerializer(Object)} and then
     * uses the resulting serializer to serialize the given value.
     *
     * @param value the value to serialize
     * @param generator the {@code JsonGenerator} to serialize it to
     * @throws IllegalStateException if no matching serialize is found
     * @throws IOException on error writing to the {@code generator}
     */
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

    /**
     * Creates a new builder initialized with a copy of this set of serializers.
     *
     * @return a new builder
     */
    public Builder copy() {
        return new Builder(serializers);
    }

    /**
     * A fluent builder for {@code Serializers}.
     */
    public static final class Builder {

        private List<Serializer<?>> serializers;
        private boolean copyOnWrite;

        /**
         * @see Serializers#builder()
         */
        public Builder() {
            serializers = new ArrayList<>();
            copyOnWrite = false;
        }

        private Builder(List<Serializer<?>> serializers) {
            this.serializers = serializers;
            this.copyOnWrite = true;
        }

        /**
         * Adds a new {@code Serializer} to this set
         *
         * @param s the serializer to add
         * @return this builder
         */
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

        /**
         * Creates an immutable set of serializers from the current state of
         * this builder.
         *
         * @return a new set of {@code Serializers}
         */
        public Serializers build() {
            copyOnWrite = true;
            serializers = Collections.unmodifiableList(serializers);
            return new Serializers(serializers);
        }
    }
}
