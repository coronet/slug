package io.coronet.slug;

import java.util.Map;

public interface SlugFactory<T extends Slug<T>> {

    T create();

    T create(Map<String, Object> map);
}
