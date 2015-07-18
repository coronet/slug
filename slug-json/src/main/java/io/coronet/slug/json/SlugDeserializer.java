package io.coronet.slug.json;

import io.coronet.slug.Slug;
import io.coronet.slug.SlugBox;

import java.lang.reflect.Type;
import java.util.Map;

/**
 *
 */
public class SlugDeserializer implements Deserializer {

    private final SlugBox box;

    public SlugDeserializer(SlugBox box) {
        if (box == null) {
            throw new NullPointerException("box");
        }
        this.box = box;
    }

    @Override
    public boolean canDeserialize(Object value, Type target) {
        if (!(value instanceof Map<?, ?>)) {
            return false;
        }
        if (target instanceof Class<?>) {
            return Slug.class.isAssignableFrom((Class<?>) target);
        }
        return false;
    }

    @Override
    public Object deserialize(Object value, Type target) {
        // Input is JSON, so key will always be a String.
        @SuppressWarnings("unchecked")
        Map<String, ?> input = (Map<String, ?>) value;

        @SuppressWarnings("unchecked")
        Class<? extends Slug<?>> st = (Class<? extends Slug<?>>) target;

        return box.create(st, input);
    }
}
