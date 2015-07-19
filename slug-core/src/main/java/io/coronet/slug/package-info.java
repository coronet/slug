/**
 * Slug is a framework for generating flexible data types. You define a loose
 * schema for a set of data by writing an interface for it:
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
 * A {@link SlugBox} will generate an implementation of this interface at
 * runtime that's backed by a {@code Map}, allowing easy access to the data in
 * the map via the getters and setters defined in the interface, but also
 * allowing you to store and access additional members directly via
 * {@link Slug#get(String)} and {@link Slug#set(String, Object)}, allowing
 * flexibility as schemas evolve over time.
 * <p>
 * Implementations of the {@link SlugModule} interface provide strategies for
 * serializing and deserializing slugs from various wire formats. Slug's
 * loosely-typed model allows it to represent and round-trip data that wasn't
 * part of the schema at the time it was baked, yielding greater flexibility
 * when rolling out upgrades across a distributed system.
 *
 * @author David Murray &lt;fernomac@coronet.io&gt;
 */
package io.coronet.slug;
