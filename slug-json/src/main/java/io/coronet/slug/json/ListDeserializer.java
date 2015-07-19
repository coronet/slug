package io.coronet.slug.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A deserializer that recursively deserializes elements within a list.
 */
public final class ListDeserializer implements Deserializer {

    @Override
    public boolean canDeserialize(Object value, Type target) {
        return (value instanceof List<?>);
    }

    @Override
    public Object deserialize(
            Object value,
            Type target,
            Deserializers deserializers) {

        List<?> input = (List<?>) value;
        List<Object> output = new ArrayList<>(input.size());

        Type elementType = null;
        if (target instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) target;
            if (ptype.getRawType() == List.class) {
                elementType = ptype.getActualTypeArguments()[0];
            }
        }

        for (Object o : input) {
            Object e = deserializers.deserializeTo(o, elementType);

            output.add(e);
        }

        return output;
    }
}
