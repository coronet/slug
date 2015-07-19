/**
 * An implementation of {@link io.coronet.slug.SlugModule} that serializes to
 * and deserializes from JSON (and similar formats) using Jackson.
 * <p>
 * <code>
 * JsonFactory factory = ...;
 * SlugModule module = JsonSlugModule.builder()
 *     .withJsonFactory(factory)
 *     .build();
 *
 * Example example = (Example) module.deserializeTo(data, Example.class);
 * </code>
 *
 * @author David Murray &lt;fernomac@coronet.io&gt;
 */
package io.coronet.slug.json;