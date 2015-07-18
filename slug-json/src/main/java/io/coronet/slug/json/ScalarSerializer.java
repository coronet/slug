package io.coronet.slug.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 */
public final class ScalarSerializer implements Serializer<Object> {

    private final Map<Class<?>, Ser<?>> sers;

    public ScalarSerializer() {
        Map<Class<?>, Ser<?>> map = new HashMap<>();

        map.put(Boolean.class, new BooleanSer());
        map.put(String.class, new StringSer());
        map.put(Byte.class, new ByteSer());
        map.put(Short.class, new ShortSer());
        map.put(Integer.class, new IntegerSer());
        map.put(Long.class, new LongSer());
        map.put(BigInteger.class, new BigIntegerSer());
        map.put(Float.class, new FloatSer());
        map.put(Double.class, new DoubleSer());
        map.put(BigDecimal.class, new BigDecimalSer());

        sers = Collections.unmodifiableMap(map);
    }

    @Override
    public boolean canSerialize(Object value) {
        return sers.containsKey(value.getClass());
    }

    @Override
    public void serialize(
            Object value,
            JsonGenerator generator,
            Serializers serializers) throws IOException {

        Ser<?> s = sers.get(value.getClass());

        @SuppressWarnings("unchecked")
        Ser<Object> erased = (Ser<Object>) s;

        erased.serialize(value, generator);
    }

    private static interface Ser<T> {
        void serialize(T value, JsonGenerator generator) throws IOException;
    }

    private static final class BooleanSer implements Ser<Boolean> {
        @Override
        public void serialize(Boolean value, JsonGenerator generator)
                throws IOException {

            generator.writeBoolean(value);
        }
    }

    private static final class StringSer implements Ser<String> {
        @Override
        public void serialize(String value, JsonGenerator generator)
                throws IOException {

            generator.writeString(value);
        }
    }

    private static final class ByteSer implements Ser<Byte> {
        @Override
        public void serialize(Byte value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }

    private static final class ShortSer implements Ser<Short> {
        @Override
        public void serialize(Short value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }

    private static final class IntegerSer implements Ser<Integer> {

        @Override
        public void serialize(Integer value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }

    private static final class LongSer implements Ser<Long> {
        @Override
        public void serialize(Long value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }

    private static final class BigIntegerSer implements Ser<BigInteger> {
        @Override
        public void serialize(BigInteger value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }

    private static final class FloatSer implements Ser<Float> {
        @Override
        public void serialize(Float value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }

    private static final class DoubleSer implements Ser<Double> {
        @Override
        public void serialize(Double value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }

    private static final class BigDecimalSer implements Ser<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, JsonGenerator generator)
                throws IOException {

            generator.writeNumber(value);
        }
    }
}
