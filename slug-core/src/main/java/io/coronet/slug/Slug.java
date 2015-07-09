package io.coronet.slug;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface Slug<T extends Slug<T>> {

    Class<T> type();

    boolean isImmutable();

    T makeImmutable();

    Object get(String member);

    T with(String member, Object value);

    Set<Map.Entry<String, Object>> entrySet();

    Map<String, Object> asMap();
}
