package io.coronet.slug.json;

import io.coronet.slug.Slug;
import io.coronet.slug.SlugBox;
import io.coronet.slug.SlugTypeRegistry;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * A deserializer that knows how to turn {@code Map}s into {@code Slug}s.
 */
public final class SlugDeserializer implements Deserializer {

    private final SlugBox box;
    private final SlugTypeRegistry registry;

    /**
     * Creates a new {@code SlugDeserializer} that will use the given
     * {@code SlugBox} to create slug instances.
     *
     * @param box the {@code SlugBox} to use
     */
    public SlugDeserializer(SlugBox box) {
        this(box, null);
    }

    /**
     * Creates a new {@code SlugDeserializer} that will use the given
     * {@code SlugBox} to create slug instances and the given
     * {@code SlugTypeRegistry} to figure out what Slug type to create.
     *
     * @param box the {@code SlugBox} to use
     * @param registry the {@code SlugTypeRegistry} to use, or null
     */
    public SlugDeserializer(SlugBox box, SlugTypeRegistry registry) {
        if (box == null) {
            throw new NullPointerException("box");
        }
        this.box = box;
        this.registry = registry;
    }

    @Override
    public boolean canDeserialize(Object value, Type target) {
        // If the value to be deserialized isn't a map, we're out of luck.
        if (!(value instanceof Map<?, ?>)) {
            return false;
        }

        // If the target type is something generic but we can resolve the
        // type based on an in-band hint, we're good to go.
        if (target == null || target == Object.class) {
            Object hint = ((Map<?, ?>) value).get("__type");
            if (hint instanceof String && registry != null) {
                return (registry.getType((String) hint) != null);
            }
        }

        // If the target type is a Slug, we're good to go.
        if (target instanceof Class<?>) {
            return Slug.class.isAssignableFrom((Class<?>) target);
        }

        return false;
    }

    @Override
    public Object deserialize(
            Object value,
            Type target,
            Deserializers deserializers) {

        Map<?, ?> input = (Map<?, ?>) value;

        Class<? extends Slug<?>> st;
        if (target == null || target == Object.class) {
            st = registry.getType((String) input.get("__type"));
        } else {
            @SuppressWarnings("unchecked")
            Class<? extends Slug<?>> c = (Class<? extends Slug<?>>) target;
            st = c;
        }

        Map<String, Type> members = box.getMembers(st);
        Map<String, Object> output =
                new HashMap<>((int) (input.size() / 0.75f) + 1);

        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = (String) deserializers.deserializeTo(
                    entry.getKey(),
                    String.class);

            Type valueType = members.get(key);
            Object v = deserializers.deserializeTo(entry.getValue(), valueType);

            output.put(key, v);
        }

        return box.create(st, output);
    }
}
