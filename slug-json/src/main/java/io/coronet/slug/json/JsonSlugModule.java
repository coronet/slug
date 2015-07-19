package io.coronet.slug.json;

import io.coronet.slug.SlugBox;
import io.coronet.slug.SlugModule;
import io.coronet.slug.SlugTypeRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * A {@code SlugModule} that serializes to and deserializes from JSON (or
 * similar formats) using Jackson.
 */
public final class JsonSlugModule implements SlugModule {

    /**
     * Creates a new builder for {@code JsonSlugModule}s.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private final SlugBox box;
    private final SlugTypeRegistry registry;
    private final Serializers serializers;
    private final Deserializers deserializers;
    private final JsonFactory factory;

    private JsonSlugModule(Builder builder) {
        SlugBox b = builder.box;
        if (b == null) {
            b = new SlugBox();
        }

        SlugTypeRegistry r = builder.registry;

        Serializers s = builder.serializers;
        if (s == null) {
            s = Serializers.standard(r).build();
        }

        Deserializers d = builder.deserializers;
        if (d == null) {
            d = Deserializers.standard(b, r).build();
        }

        JsonFactory f = builder.factory;
        if (f == null) {
            f = new JsonFactory();

            f.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
            f.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

            f.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
            f.enable(JsonParser.Feature.ALLOW_COMMENTS);
            f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
            f.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
            f.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            f.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
            f.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
            f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        }

        this.box = b;
        this.registry = r;
        this.serializers = s;
        this.deserializers = d;
        this.factory = f;
    }

    @Override
    public SlugBox getSlugBox() {
        return box;
    }

    @Override
    public SlugTypeRegistry getTypeRegistry() {
        return registry;
    }

    @Override
    public void serializeTo(Object object, OutputStream stream)
            throws IOException {

        try (JsonGenerator generator = factory.createGenerator(stream)) {
            serializeTo(object, generator);
        }
    }

    /**
     * Serializes an object to the given {@code JsonGenerator}.
     *
     * @param object the object to serialize
     * @param generator the generator to write it to
     * @throws NullPointerException if {@code object} or {@code generator} is
     *             null
     * @throws IOException on error writing to the generator
     */
    public void serializeTo(Object object, JsonGenerator generator)
            throws IOException {

        serializers.serialize(object, generator);
    }

    @Override
    public Object deserializeTo(InputStream stream, Type target)
            throws IOException {

        try (JsonParser parser = factory.createParser(stream)) {
            return deserializeTo(parser, target);
        }
    }

    /**
     * Attempts to deserialize an instance of the given type from the given
     * {@code JsonParser}. The returned value may or may not actually be an
     * instance of the requested type, depending on what's found in the
     * parser. It's more of a "guideline."
     *
     * @param parser the JSON parser to read from
     * @param target the target type to deserialize to
     * @return the deserialized object
     * @throws NullPointerException if {@code parser} is null
     * @throws IOException on error reading from the parser or if the input
     *             data is malformed
     */
    public Object deserializeTo(JsonParser parser, Type target)
            throws IOException {

        if (parser == null) {
            throw new NullPointerException("parser");
        }

        if (parser.getCurrentToken() == null) {
            parser.nextToken();
        }

        Object result = deserializeRaw(parser);
        result = deserializers.deserializeTo(result, target);
        return result;
    }

    /**
     * Deserializes to a "raw" type, which will then be fed to the appropriate
     * {@code Deserializer}.
     *
     * @param parser the parser to read from
     * @return the raw deserialized object
     * @throws IOException on error reading from the parser
     */
    private Object deserializeRaw(JsonParser parser)
            throws IOException {

        JsonToken token = parser.getCurrentToken();
        switch (token) {
        case VALUE_NULL:            return null;

        case VALUE_TRUE:            return true;
        case VALUE_FALSE:           return false;

        case VALUE_STRING:          return parser.getText();
        case VALUE_EMBEDDED_OBJECT: return parser.getEmbeddedObject();

        case VALUE_NUMBER_INT:      return parser.getBigIntegerValue();
        case VALUE_NUMBER_FLOAT:    return parser.getDecimalValue();

        case START_ARRAY:           return parseArray(parser);
        case START_OBJECT:          return parseObject(parser);

        default:
            throw new IllegalStateException(
                    "Unexpected token " + token + " at "
                    + parser.getCurrentLocation());
        }
    }

    private Object parseArray(JsonParser parser) throws IOException {
        List<Object> list = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            Object value = deserializeRaw(parser);
            list.add(value);
        }

        return list;
    }

    private Object parseObject(JsonParser parser) throws IOException {
        Map<String, Object> map = new HashMap<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME) {
                throw new IllegalStateException(
                        "Unexpected token " + parser.getCurrentToken() + " at "
                        + parser.getCurrentLocation());
            }

            String name = parser.getText();

            parser.nextToken();
            Object value = deserializeRaw(parser);

            map.put(name, value);
        }

        return map;
    }

    /**
     * A fluent builder for {@code JsonSlugModule}s.
     */
    public static final class Builder {

        private SlugBox box;
        private SlugTypeRegistry registry;
        private Serializers serializers;
        private Deserializers deserializers;
        private JsonFactory factory;

        /**
         * Configures the {@code SlugBox} that this module will use to create
         * new slugs. If left null, a new, default {@code SlugBox} will be
         * created for this module.
         *
         * @param b the custom {@code SlugBox} to use
         * @return this builder
         */
        public Builder withSlugBox(SlugBox b) {
            box = b;
            return this;
        }

        /**
         * Configures the {@code SlugTypeRegistry} that this module will use
         * to resolve in-band type information. If left null, run-time type
         * information will be ignored.
         *
         * @param r the custom {@code SlugTypeRegistry} to use
         * @return this builder
         */
        public Builder withTypeRegistry(SlugTypeRegistry r) {
            registry = r;
            return this;
        }

        /**
         * Configures the {@code Serializers} that this module will use to
         * serialize objects to JSON. If left null, a basic set of serializers
         * that can handle Booleans, Strings, Numbers, Bytes, Lists, Maps, and
         * Slugs will be created.
         *
         * @param s the set of {@code Serializers} to use
         * @return this builder
         */
        public Builder withSerializers(Serializers s) {
            serializers = s;
            return this;
        }

        /**
         * Configures the {@code Deserializers} that this module will use to
         * deserialize objects from JSON. If left null, a basic set of
         * deserializers that can handle Booleans, Strings, Numbers, Bytes,
         * Lists, Maps, and Slugs will be created.
         *
         * @param d the set of {@code Deserializers} to use
         * @return this builder
         */
        public Builder withDeserializers(Deserializers d) {
            deserializers = d;
            return this;
        }

        /**
         * Configures the {@code JsonFactory} to use for parsing and generating
         * JSON. If left null, a basic {@code JsonFactory} will be created
         * that outputs standard JSON but is a bit more liberal in what it
         * accepts (ie C/YAML comments, non-numeric numbers).
         *
         * @param f the {@code JsonFactory} to use
         * @return this builder
         */
        public Builder withJsonFactory(JsonFactory f) {
            factory = f;
            return this;
        }

        /**
         * Builds a {@code JsonSlugModule} with the current configuration of
         * this builder.
         *
         * @return a new {@code JsonSlugModule}
         */
        public JsonSlugModule build() {
            return new JsonSlugModule(this);
        }
    }
}
