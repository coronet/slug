package io.coronet.slug.json;

import java.lang.reflect.Type;

/**
 *
 */
public interface Deserializer {

    boolean canDeserialize(Object value, Type target);

    Object deserialize(Object value, Type target);
}
