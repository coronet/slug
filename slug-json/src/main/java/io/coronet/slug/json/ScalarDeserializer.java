package io.coronet.slug.json;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic deserializer that transforms raw numeric scalars to the requested
 * {@code Number} type, if the actual value will fit.
 */
public final class ScalarDeserializer implements Deserializer {

    private final Map<Class<?>, Deser> desers;

    public ScalarDeserializer() {
        Map<Class<?>, Deser> map = new HashMap<>();

        map.put(Byte.class, new ByteDeser());
        map.put(Short.class, new ShortDeser());
        map.put(Integer.class, new IntegerDeser());
        map.put(Long.class, new LongDeser());
        map.put(Float.class, new FloatDeser());
        map.put(Double.class, new DoubleDeser());
        map.put(BigDecimal.class, new BigDecimalDeser());

        desers = Collections.unmodifiableMap(map);
    }

    @Override
    public boolean canDeserialize(Object value, Type target) {
        if (!(value instanceof Number)) {
            return false;
        }
        return desers.containsKey(target);
    }

    @Override
    public Object deserialize(Object value, Type target) {
        return desers.get(target).deserialize(value);
    }


    private static interface Deser {
        Object deserialize(Object input);
    }

    private static final class ByteDeser implements Deser {
        @Override
        public Object deserialize(Object input) {
            if (input instanceof BigInteger) {
                BigInteger bi = (BigInteger) input;
                if (bi.bitLength() < 8) {
                    return bi.byteValueExact();
                }
            }
            return input;
        }
    }

    private static final class ShortDeser implements Deser {
        @Override
        public Object deserialize(Object input) {
            if (input instanceof BigInteger) {
                BigInteger bi = (BigInteger) input;
                if (bi.bitLength() < 16) {
                    return bi.shortValueExact();
                }
            }
            return input;
        }
    }

    private static final class IntegerDeser implements Deser {
        @Override
        public Object deserialize(Object input) {
            if (input instanceof BigInteger) {
                BigInteger bi = (BigInteger) input;
                if (bi.bitLength() < 32) {
                    return bi.intValueExact();
                }
            }
            return input;
        }
    }

    private static final class LongDeser implements Deser {
        @Override
        public Object deserialize(Object input) {
            if (input instanceof BigInteger) {
                BigInteger bi = (BigInteger) input;
                if (bi.bitLength() < 64) {
                    return bi.longValueExact();
                }
            }
            return input;
        }
    }

    private static final class FloatDeser implements Deser {
        @Override
        public Object deserialize(Object input) {
            if (input instanceof BigInteger) {
                return ((BigInteger) input).floatValue();
            }
            if (input instanceof BigDecimal) {
                return ((BigDecimal) input).floatValue();
            }
            return input;
        }
    }

    private static final class DoubleDeser implements Deser {
        @Override
        public Object deserialize(Object input) {
            if (input instanceof BigInteger) {
                return ((BigInteger) input).doubleValue();
            }
            if (input instanceof BigDecimal) {
                return ((BigDecimal) input).doubleValue();
            }
            return input;
        }
    }

    private static final class BigDecimalDeser implements Deser {
        @Override
        public Object deserialize(Object input) {
            if (input instanceof BigInteger) {
                return new BigDecimal((BigInteger) input);
            }
            return input;
        }
    }
}
