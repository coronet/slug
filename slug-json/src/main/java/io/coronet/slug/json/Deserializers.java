package io.coronet.slug.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public final class Deserializers {

    public static Builder builder() {
        return new Builder();
    }

    private final List<Deserializer> deserializers;

    private Deserializers(List<Deserializer> deserializers) {
        this.deserializers = deserializers;
    }

    public Deserializer getDeserializer(Object value, Type target) {
        for (int i = deserializers.size() - 1; i >= 0; --i) {
            Deserializer deserializer = deserializers.get(i);
            if (deserializer.canDeserialize(value, target)) {
                return deserializer;
            }
        }

        return null;
    }

    public Object deserializeTo(Object value, Type target) {
        Deserializer deserializer = getDeserializer(value, target);
        if (deserializer == null) {
            return value;
        }
        return deserializer.deserialize(value, target);
    }

    public Builder copy() {
        return new Builder(deserializers);
    }

    public static final class Builder {

        private List<Deserializer> deserializers;
        private boolean copyOnWrite;

        public Builder() {
            deserializers = new ArrayList<>();
            copyOnWrite = false;
        }

        private Builder(List<Deserializer> deserializers) {
            this.deserializers = deserializers;
            this.copyOnWrite = true;
        }

        public Builder with(Deserializer d) {
            if (d == null) {
                throw new NullPointerException("d");
            }

            if (copyOnWrite) {
                deserializers = new ArrayList<>(deserializers);
                copyOnWrite = false;
            }

            deserializers.add(d);
            return this;
        }

        public Deserializers build() {
            copyOnWrite = true;
            deserializers = Collections.unmodifiableList(deserializers);
            return new Deserializers(deserializers);
        }
    }
}
