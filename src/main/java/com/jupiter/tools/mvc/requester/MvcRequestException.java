package com.jupiter.tools.mvc.requester;

/**
 * Created on 12.02.2019.
 *
 * @author Korovin Anatoliy
 */
public class MvcRequestException extends RuntimeException {

    public MvcRequestException(Exception e) {
        super(e);
    }
}
