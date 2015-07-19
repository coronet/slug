package io.coronet.slug;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract base for runtime-generated slug implementations.
 *
 * @param <T> the interface type of this slug
 */
public class AbstractSlug<T extends Slug<T>> implements Slug<T> {

    private final Class<T> type;
    private Map<String, Object> map;
    private boolean immutable;

    /**
     * Creates a new slug wrapping the given set of members.
     *
     * @param type the interface type of this slug
     * @param map the initial set of members for this slug
     */
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
    public T set(String member, Object value) {
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
        return type.getName() + "::" + map.toString();
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
