package com.jupiter.tools.mvc.requester;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 12.02.2019.
 *
 * @author Korovin Anatoliy
 */
class PrimitiveConverterTest {

    static Stream<Arguments> data() {
        return Stream.of(Arguments.of("1987", Integer.class, 1987),
                         Arguments.of("1987", Long.class, 1987L),
                         Arguments.of("true", Boolean.class, true),
                         Arguments.of("87", Byte.class, (byte) 87),
                         Arguments.of("1987", Short.class, (short) 1987),
                         Arguments.of("1987.0", Float.class, 1987.0f),
                         Arguments.of("1987.0", Double.class, (double) 1987.0f),
                         Arguments.of("unknown", String.class, "unknown"));
    }

    @ParameterizedTest
    @MethodSource("data")
    <T> void convert(String value, Class<T> type, T expected) {
        // Act
        Object result = PrimitiveConverter.convertToPrimitive(value, type);
        // Asserts
        assertThat(result).isEqualTo(expected);
    }
}