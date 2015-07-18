package io.coronet.slug;

import java.util.HashMap;
import java.util.Map;

public interface SlugFactory<T extends Slug<?>> {

    default T create() {
        return create(new HashMap<>());
    }

    T create(Map<String, Object> map);
}
