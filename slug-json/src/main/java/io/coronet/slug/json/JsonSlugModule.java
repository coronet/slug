package io.coronet.slug.json;

import io.coronet.slug.Slug;
import io.coronet.slug.SlugBox;
import io.coronet.slug.SlugModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
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
 *
 */
public final class JsonSlugModule extends SlugModule {

    public static Builder builder() {
        return new Builder();
    }

    private final SlugBox box;
    private final Serializers serializers;
    private final Deserializers deserializers;
    private final JsonFactory factory;

    private JsonSlugModule(Builder builder) {
        SlugBox b = builder.box;
        if (b == null) {
            b = new SlugBox();
        }

        Serializers s = builder.serializers;
        if (s == null) {
            s = Serializers.builder()
                    .with(new ScalarSerializer())
                    .with(new BinarySerializer())
                    .with(new ListSerializer())
                    .with(new MapSerializer())
                    .with(new SlugSerializer())
                    .build();
        }

        Deserializers d = builder.deserializers;
        if (d == null) {
            d = Deserializers.builder()
                    .with(new ScalarDeserializer())
                    .with(new BinaryDeserializer())
                    .with(new SlugDeserializer(b))
                    .build();
        }

        JsonFactory f = builder.factory;
        if (f == null) {
            f = new JsonFactory();
            f.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
            f.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            f.enable(JsonParser.Feature.ALLOW_COMMENTS);
            f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
            f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        }

        this.box = b;
        this.serializers = s;
        this.deserializers = d;
        this.factory = f;
    }

    @Override
    public void serializeTo(Object object, OutputStream stream)
            throws IOException {

        try (JsonGenerator generator = factory.createGenerator(stream)) {
            serializeTo(object, generator);
        }
    }

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

    private Object deserializeTo(JsonParser parser, Type target)
            throws IOException {

        Object result = deserializeTo0(parser, target);
        result = deserializers.deserializeTo(result, target);
        return result;
    }

    private Object deserializeTo0(JsonParser parser, Type target)
            throws IOException {

        switch (parser.getCurrentToken()) {
        case VALUE_NULL:            return null;

        case VALUE_TRUE:            return true;
        case VALUE_FALSE:           return false;

        case VALUE_STRING:          return parser.getText();
        case VALUE_EMBEDDED_OBJECT: return parser.getEmbeddedObject();

        case VALUE_NUMBER_INT:      return parser.getBigIntegerValue();
        case VALUE_NUMBER_FLOAT:    return parser.getDecimalValue();

        case START_ARRAY:           return parseArray(parser, target);
        case START_OBJECT:          return parseObject(parser, target);

        default:
            throw new IllegalStateException(
                    "Unexpected token " + parser.getCurrentToken() + " at "
                    + parser.getCurrentLocation());
        }
    }

    private Object parseArray(JsonParser parser, Type target)
            throws IOException {

        List<Object> list = new ArrayList<>();

        Type element = getElementType(target);

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            Object value = deserializeTo(parser, element);
            list.add(value);
        }

        return list;
    }

    private Type getElementType(Type listType) {
        if (!(listType instanceof ParameterizedType)) {
            return Object.class;
        }

        ParameterizedType ptype = (ParameterizedType) listType;
        if (ptype.getRawType() != List.class) {
            return Object.class;
        }

        return ptype.getActualTypeArguments()[0];
    }

    private Object parseObject(JsonParser parser, Type type)
            throws IOException {

        Map<String, Object> map = new HashMap<>();

        TypeResolver resolver = getTypeResolver(type);

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME) {
                throw new IllegalStateException(
                        "Unexpected token " + parser.getCurrentToken() + " at "
                        + parser.getCurrentLocation());
            }

            String name = parser.getText();

            parser.nextToken();
            Object value = deserializeTo(parser, resolver.resolve(name));

            map.put(name, value);
        }

        return map;
    }

    private TypeResolver getTypeResolver(Type type) {
        if (type instanceof Class<?>) {

            Class<?> c = (Class<?>) type;

            if (Slug.class.isAssignableFrom(c)) {
                @SuppressWarnings("unchecked")
                Class<? extends Slug<?>> s = (Class<? extends Slug<?>>) c;
                return new SlugTypeResolver(box.getMembers(s));
            }

        } else if (type instanceof ParameterizedType) {

            ParameterizedType ptype = (ParameterizedType) type;

            if (ptype.getRawType() == Map.class) {
                return new MapTypeResolver(ptype.getActualTypeArguments()[1]);
            }

        }

        return new MapTypeResolver(Object.class);
    }

    public static final class Builder {

        private SlugBox box;
        private Serializers serializers;
        private Deserializers deserializers;
        private JsonFactory factory;

        public Builder withSlugBox(SlugBox b) {
            box = b;
            return this;
        }

        public Builder withSerializers(Serializers s) {
            serializers = s;
            return this;
        }

        public Builder withDeserializers(Deserializers d) {
            deserializers = d;
            return this;
        }

        public Builder withJsonFactory(JsonFactory f) {
            factory = f;
            return this;
        }

        public JsonSlugModule build() {
            return new JsonSlugModule(this);
        }
    }

    private static interface TypeResolver {
        Type resolve(String name);
    }

    private static final class MapTypeResolver implements TypeResolver {

        private final Type type;

        public MapTypeResolver(Type type) {
            this.type = type;
        }

        @Override
        public Type resolve(String name) {
            return type;
        }
    }

    private static final class SlugTypeResolver implements TypeResolver {

        private final Map<String, Type> members;

        public SlugTypeResolver(Map<String, Type> members) {
            this.members = members;
        }

        @Override
        public Type resolve(String name) {
            return members.get(name);
        }
    }
}
