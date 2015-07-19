package io.coronet.slug;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for slugs of a particular type. Implementations are generated at
 * runtime for registered slug types so we don't need to use reflection to
 * create new slugs.
 *
 * @param <T> the type of slug this factory can create
 * @see SlugBox#factoryFor(Class)
 */
public interface SlugFactory<T extends Slug<?>> {

    /**
     * Creates a new slug of this type wrapping the given map of attributes.
     *
     * @param map the map of attributes to wrap
     * @return a new slug
     * @see SlugBox#wrap(Class, Map)
     */
    T create(Map<String, Object> map);

    /**
     * Creates a new slug of this type wrapping an empty {@code HashMap}.
     *
     * @return a new slug
     * @see SlugBox#create(Class)
     */
    default T create() {
        return create(new HashMap<>());
    }
}
