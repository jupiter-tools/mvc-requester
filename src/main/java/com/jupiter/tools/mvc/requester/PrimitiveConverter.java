package com.jupiter.tools.mvc.requester;

/**
 * Created on 16.07.2018.
 *
 * @author Korovin Anatoliy
 */
final class PrimitiveConverter {

    private PrimitiveConverter() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated"); // $COVERAGE-IGNORE$
    }

    static Object convertToPrimitive(String value, Class targetType) {

        if (Boolean.class == targetType) { return Boolean.parseBoolean(value); }
        if (Byte.class == targetType) { return Byte.parseByte(value); }
        if (Short.class == targetType) { return Short.parseShort(value); }
        if (Integer.class == targetType) { return Integer.parseInt(value); }
        if (Long.class == targetType) { return Long.parseLong(value); }
        if (Float.class == targetType) { return Float.parseFloat(value); }
        if (Double.class == targetType) { return Double.parseDouble(value); }
        return value;
    }
}
