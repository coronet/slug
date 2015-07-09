package io.coronet.slug;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface SlugFactory<T extends Slug<T>> {

    default T create() {
        return create(new HashMap<>());
    }

    T create(Map<String, Object> map);
}
