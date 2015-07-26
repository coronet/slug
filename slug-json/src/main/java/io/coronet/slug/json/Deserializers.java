package io.coronet.slug.json;

import io.coronet.slug.SlugBox;
import io.coronet.slug.SlugTypeRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A set of {@link Deserializer}s and a bit of logic to decide which one to
 * use to deserialize a particular object.
 */
public final class Deserializers {

    /**
     * Creates a new builder for a set of {@code Deserializers}. The builder
     * initially wraps a completely empty set of deserializers. Use me if you
     * want to exclude one or more of the standard deserializers.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder initialized with the "standard" set of
     * deserializers, including a {@link SlugDeserializer} that will use the
     * given {@code SlugBox}. The standard deserializers are registered with
     * the lowest priority so any custom deserializers added afterwards will
     * take precedence.
     *
     * @param box the {@code SlugBox} to use for deserializing slugs
     * @return a new standard builder
     */
    public static Builder standard(SlugBox box) {
        return standard(box, null);
    }

    /**
     * Creates a new builder initialized with the "standard" set of
     * deserializers, including a {@link SlugDeserializer} that will use the
     * given {@code SlugBox} and {@code SlugTypeRegistry}. The standard
     * deserializers are registered with the lowest priority so any custom
     * deserializers added afterwards will take precedence.
     *
     * @param box the {@code SlugBox} to use for deserializing slugs
     * @param registry the {@code SlugTypeRegistry} to use, or null
     * @return a new standard builder
     */
    public static Builder standard(
            SlugBox box,
            SlugTypeRegistry registry) {

        return new Builder()
                .with(new ScalarDeserializer())
                .with(new BinaryDeserializer())
                .with(new UuidDeserializer())
                .with(new ListDeserializer())
                .with(new MapDeserializer())
                .with(new SlugDeserializer(box, registry));
    }

    private final List<Deserializer> deserializers;

    private Deserializers(List<Deserializer> deserializers) {
        this.deserializers = deserializers;
    }

    /**
     * Gets an appropriate deserializer for the given object and target type,
     * if one can be found. Implemented by calling the
     * {@link Deserializer#canDeserialize(Object, Type)} method of each
     * registered deserializer (in the reverse order that they were registered),
     * returning the first one that matches the input or null if no match
     * is found.
     *
     * @param value the raw value being deserialized
     * @param target the target type we're trying to deserialize to
     * @return an appropriate deserializer, or null if none is found
     */
    public Deserializer getDeserializer(Object value, Type target) {
        for (int i = deserializers.size() - 1; i >= 0; --i) {
            Deserializer deserializer = deserializers.get(i);
            if (deserializer.canDeserialize(value, target)) {
                return deserializer;
            }
        }

        return null;
    }

    /**
     * A convenience method that calls {@link #getDeserializer(Object, Type)}
     * and invokes {@link Deserializer#deserialize(Object, Type)} if an
     * appropriate deserializer is found. If no deserializer is found, the
     * input value is returned unmodified.
     *
     * @param value the raw value being deserialized
     * @param target the target type we're trying to deserialize to
     * @return the deserialized value (possibly the input value if no
     *             deserializer is found)
     */
    public Object deserializeTo(Object value, Type target) {
        Deserializer deserializer = getDeserializer(value, target);
        if (deserializer == null) {
            return value;
        }
        return deserializer.deserialize(value, target, this);
    }

    /**
     * Creates a mutable builder whose initial state matches this set of
     * {@code Deserializers}.
     *
     * @return a new builder
     */
    public Builder copy() {
        return new Builder(deserializers);
    }

    /**
     * A fluent builder for {@code Deserializers}.
     */
    public static final class Builder {

        private List<Deserializer> deserializers;
        private boolean copyOnWrite;

        /**
         * Creates a new, default builder.
         *
         * @see Deserializers#builder()
         */
        public Builder() {
            deserializers = new ArrayList<>();
            copyOnWrite = false;
        }

        private Builder(List<Deserializer> deserializers) {
            this.deserializers = deserializers;
            this.copyOnWrite = true;
        }

        /**
         * Adds a new {@code Deserializer} to this set.
         *
         * @param d the deserializer to add
         * @return this builder
         */
        public Builder with(Deserializer d) {
            if (d == null) {
                throw new NullPointerException("d");
            }

            if (copyOnWrite) {
                deserializers = new ArrayList<>(deserializers);
                copyOnWrite = false;
            }

            deserializers.add(d);
            return this;
        }

        /**
         * Creates an immutable set of deserializers from the current state of
         * this builder.
         *
         * @return a new {@code Deserializers} set
         */
        public Deserializers build() {
            copyOnWrite = true;
            deserializers = Collections.unmodifiableList(deserializers);
            return new Deserializers(deserializers);
        }
    }
}
