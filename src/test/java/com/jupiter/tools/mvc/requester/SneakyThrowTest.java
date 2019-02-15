package com.jupiter.tools.mvc.requester;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 12.02.2019.
 *
 * @author Korovin Anatoliy
 */
class SneakyThrowTest {


    @Test
    void catchException() {
        MvcRequestException exc =
                Assertions.assertThrows(MvcRequestException.class,
                                        () -> SneakyThrow.wrap(() -> {
                                            // Act
                                            throw new Exception("123");
                                        }));
        // Asserts
        assertThat(exc.getCause().getMessage()).isEqualTo("123");
    }

    @Test
    void withoutException() {
        // Act
        String result = SneakyThrow.wrap(() -> "ok");
        // Asserts
        assertThat(result).isEqualTo("ok");
    }
}