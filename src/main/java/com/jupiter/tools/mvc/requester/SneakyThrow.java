package com.jupiter.tools.mvc.requester;

import java.util.concurrent.Callable;

/**
 * Created on 12.02.2019.
 *
 * @author Korovin Anatoliy
 */
class SneakyThrow {

    // $COVERAGE-IGNORE$
    private SneakyThrow() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Run callable within try-catch block and wrap checked exceptions in unchecked
     *
     * @param callable function which may throws checked exceptions
     * @param <Type>   type of returned value
     * @return function result
     */
    static <Type> Type wrap(Callable<Type> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new MvcRequestException(e);
        }
    }
}
