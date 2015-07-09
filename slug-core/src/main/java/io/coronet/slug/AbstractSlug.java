package io.coronet.slug;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 */
public class AbstractSlug<T extends Slug<T>> implements Slug<T> {

    private final Class<T> type;
    private Map<String, Object> map;
    private boolean immutable;

    protected AbstractSlug(Class<T> type, Map<String, Object> map) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (map == null) {
            throw new NullPointerException("map");
        }

        this.type = type;
        this.map = map;
        this.immutable = false;
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T makeImmutable() {
        map = Collections.unmodifiableMap(map);
        immutable = true;
        return (T) this;
    }

    @Override
    public Object get(String member) {
        return map.get(member);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T with(String member, Object value) {
        if (member == null) {
            throw new NullPointerException("member");
        }

        if (value == null) {
            map.remove(member);
        } else {
            map.put(member, value);
        }

        return (T) this;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Map<String, Object> asMap() {
        return map;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Slug<?>)) {
            return false;
        }

        Slug<?> that = (Slug<?>) obj;

        return this.entrySet().equals(that.entrySet());
    }
}
