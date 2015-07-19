package io.coronet.slug.json;

import java.lang.reflect.Type;

/**
 * A strategy for deserializing raw objects read from a JSON input into
 * the expected user-facing type. Implement me to provide custom
 * deserialization for data types that don't map directly to JSON types.
 */
public interface Deserializer {

    /**
     * Checks whether this deserializer knows how to deserialize the given
     * raw object into an instance of the target type.
     *
     * @param value the raw value being deserialized
     * @param target the target type
     * @return true if this deserializer can perform the deserialization
     */
    boolean canDeserialize(Object value, Type target);

    /**
     * Deserializes the given raw value to an instance of the requested target
     * type. The behavior of this method is undefined if {@code canDeserialize}
     * returns false - it will probably throw a gnarly exception so you
     * shouldn't call it.
     *
     * @param value the raw value to deserialize
     * @param target the target type
     * @param deserializers the set of deserializers to use for recursive
     *            deserialization of complex elements
     * @return the deserialized value
     */
    Object deserialize(Object value, Type target, Deserializers deserializers);
}
