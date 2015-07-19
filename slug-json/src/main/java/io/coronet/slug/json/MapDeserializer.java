package io.coronet.slug.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class MapDeserializer implements Deserializer {

    @Override
    public boolean canDeserialize(Object value, Type target) {
        return (value instanceof Map<?, ?>);
    }

    @Override
    public Object deserialize(
            Object value,
            Type target,
            Deserializers deserializers) {

        Map<?, ?> input = (Map<?, ?>) value;
        Map<Object, Object> output =
                new HashMap<>((int) (input.size() / 0.75f) + 1);

        Type keyType = null;
        Type valueType = null;

        if (target instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) target;
            if (ptype.getRawType() == Map.class) {
                keyType = ptype.getActualTypeArguments()[0];
                valueType = ptype.getActualTypeArguments()[1];
            }
        }

        for (Map.Entry<?, ?> entry : input.entrySet()) {
            Object k = deserializers.deserializeTo(entry.getKey(), keyType);
            Object v = deserializers.deserializeTo(entry.getValue(), valueType);

            output.put(k, v);
        }

        return output;
    }
}
