package io.coronet.slug;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A registry of slug types, binding abstract implementation-independent
 * content type names to the local {@code Slug} interfaces that represent
 * them.
 */
public final class SlugTypeRegistry {

    /**
     * Creates a new builder for a {@code SlugTypeRegistry}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private final Map<Class<? extends Slug<?>>, String> names;
    private final Map<String, Class<? extends Slug<?>>> types;

    private SlugTypeRegistry(Builder builder) {
        this.names = builder.names;
        this.types = builder.types;
    }

    /**
     * Gets the (primary) name of the given type, for use during serialization
     * in formats that include type information in-band. Returns null if no
     * name has been registered for the given slug type.
     *
     * @param type the slug interface type
     * @return the stable, implementation-independent type name
     */
    public String getName(Class<? extends Slug<?>> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        return names.get(type);
    }

    /**
     * Gets the local Slug interface representating the the given stable,
     * implementation-independent type name. Returns null if no type has been
     * registered with the given name.
     *
     * @param name the table, implementation-independent type name
     * @return the corresponding local slug interface
     */
    public Class<? extends Slug<?>> getType(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return types.get(name);
    }

    @Override
    public String toString() {
        return types.toString();
    }

    /**
     * A fluent builder for {@code SlugTypeRegistry}s.
     */
    public static final class Builder {

        private Map<Class<? extends Slug<?>>, String> names;
        private Map<String, Class<? extends Slug<?>>> types;
        private boolean copyOnWrite;

        public Builder() {
            names = new HashMap<>();
            types = new HashMap<>();
            copyOnWrite = false;
        }

        /**
         * Registers a name/type pair with this registry.
         *
         * @param name the stable, implementation-independent type name
         * @param type the local slug type to bind to the given name
         * @return this builder
         */
        public Builder with(String name, Class<? extends Slug<?>> type) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (type == null) {
                throw new NullPointerException("type");
            }

            if (types.containsKey(name)) {
                throw new IllegalArgumentException(
                        "name " + name + " already registered to "
                        + types.get(name));
            }
            if (names.containsKey(type)) {
                throw new IllegalArgumentException(
                        "type " + type + " already registered as "
                        + names.get(type));
            }

            if (copyOnWrite) {
                names = new HashMap<>(names);
                types = new HashMap<>(types);
                copyOnWrite = false;
            }

            names.put(type, name);
            types.put(name, type);

            return this;
        }

        /**
         * Builds a new immutable {@code SlugTypeRegistry} from the current
         * state of this builder.
         *
         * @return a new {@code SlugTypeRegistry}
         */
        public SlugTypeRegistry build() {
            copyOnWrite = true;
            names = Collections.unmodifiableMap(names);
            types = Collections.unmodifiableMap(types);
            return new SlugTypeRegistry(this);
        }
    }
}
