package com.jupiter.tools.mvc.requester;

import com.jupiter.tools.mvc.requester.cobertura.CoverageIgnore;

/**
 * Created on 16.07.2018.
 *
 * @author Korovin Anatoliy
 */
final class PrimitiveConverter {

    @CoverageIgnore
    private PrimitiveConverter(){
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static Object convertToPrimitive(String value, Class targetType) {

        if (Boolean.class == targetType) return Boolean.parseBoolean(value);
        if (Byte.class == targetType) return Byte.parseByte(value);
        if (Short.class == targetType) return Short.parseShort(value);
        if (Integer.class == targetType) return Integer.parseInt(value);
        if (Long.class == targetType) return Long.parseLong(value);
        if (Float.class == targetType) return Float.parseFloat(value);
        if (Double.class == targetType) return Double.parseDouble(value);
        return value;
    }
}
