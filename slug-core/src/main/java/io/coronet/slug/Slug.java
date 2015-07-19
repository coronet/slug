package io.coronet.slug;

import java.util.Map;
import java.util.Set;

/**
 * A slug is an interface defining a loose schema for a message. A slug is
 * implemented as a {@code Map<String, Object>} with loosely-typed getters and
 * setters for "known" members defined by an interface:
 * <p>
 * <code>
 * public interface Example extends Slug&lt;Example&gt; {
 *
 *     String getName();
 *     void setName(String name);
 *
 *     Integer getAge();
 *     Example setAge(Integer age);
 * }
 * </code>
 * <p>
 * Known members of the Slug may be accessed via the getters and setters.
 * Both known and unknown members may be accessed via the raw get and set
 * methods, or via the slug's {@code entrySet()}.
 * <p>
 * <code>
 * Example example = ...;
 *
 * example.setName("David Murray");
 * example.set("Job", "Cat Dad");
 *
 * example.getName();   // "David Murray"
 * example.get("Name"); // "David Murray"
 *
 * for (Map.Entry<String, String> entry : example.entrySet()) {
 *     ...
 * }
 * </code>
 * <p>
 * Slug interfaces are implemented at runtime by a {@code SlugBox} so you don't
 * have to waste time writing boilerplate.
 * <p>
 * <code>
 * SlugBox box = ...;
 * Example slug = box.create(Example.class);
 * </code>
 * <p>
 * Slugs are serializable and deserializable via a {@code SlugModule}; all
 * members, known and unknown, are serialized and deserialized. Different
 * modules provide serializers for different formats.
 * <p>
 * <code>
 * SlugModule module = ...;
 * Example slug = (Example) module.deserializeTo(bytes, Example.class);
 *
 * module.serializeTo(slug, stream);
 * </code>
 *
 * @param <T> the actual interface type of this Slug
 */
public interface Slug<T extends Slug<T>> {

    /**
     * Returns the interface type of this Slug; this is typically more useful
     * than the {@code getClass()} method, which will return the
     * runtime-generated implementation type.
     *
     * @return the type of this slug
     */
    Class<T> type();

    /**
     * Returns true if this slug is immutable, false otherwise. Slugs may be
     * made immutable by calling the {@link #makeImmutable} method, after which
     * they're safe to share across threads.
     *
     * @return true if this slug is immutable
     */
    boolean isImmutable();

    /**
     * Makes this slug immutable. Attempting to modify an immutable slug will
     * result in an {@code UnsupportedOperationException} being thrown.
     *
     * @return this slug
     */
    T makeImmutable();

    /**
     * Returns the member of the given name, if it's defined. Returns null if
     * the member is not defined.
     *
     * @param member the member to get
     * @return the current value of the member
     * @throws NullPointerException if {@code member} is null
     */
    Object get(String member);

    /**
     * Sets the member of the given name. Setting a member to {@code null}
     * is equivalent to removing it from the slug.
     *
     * @param member the name of the member to set
     * @param value the new value of the member
     * @return this slug
     * @throws NullPointerException if {@code member} is null
     */
    T set(String member, Object value);

    /**
     * Returns a set of all the entries in this slug. The key of each
     * entry is the member name, the value is the corresponding value for
     * the given member.
     *
     * @return the entry set for this slug
     */
    Set<Map.Entry<String, Object>> entrySet();

    /**
     * Returns a view of this slug as a Java {@code Map}. The map will be
     * immutable if and only if this slug is immutable; modifications to the
     * map, if allowed, will affect the slug.
     *
     * @return a map view of this slug
     */
    Map<String, Object> asMap();
}
