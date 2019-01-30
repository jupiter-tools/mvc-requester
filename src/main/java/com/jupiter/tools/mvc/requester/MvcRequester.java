package com.jupiter.tools.mvc.requester;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created on 30.08.2017.
 *
 * Wrapper for MockMvc to send request and assert response
 * in more intuitive way.
 *
 * @author Sergey Vdovin
 * @author Korovin Anatoliy
 */
public class MvcRequester {

    private final ObjectMapper sendJsonMapper;
    private final ObjectMapper receiveJsonMapper;
    private final MockMvc mockMvc;

    private MvcRequester(MockMvc mockMvc) {

        this.mockMvc = mockMvc;
        this.sendJsonMapper = new ObjectMapper();
        this.receiveJsonMapper = new ObjectMapper();
    }

    private MvcRequester(MockMvc mockMvc,
                         ObjectMapper sendJsonMapper,
                         ObjectMapper receiveJsonMapper) {

        this.mockMvc = mockMvc;
        this.sendJsonMapper = sendJsonMapper;
        this.receiveJsonMapper = receiveJsonMapper;
    }

    /**
     * static factory method
     *
     * @param mockMvc {@link MockMvc} which will be used to make a request
     */
    public static MvcRequester on(MockMvc mockMvc) {
        return new MvcRequester(mockMvc);
    }

    /**
     * Static factory method, with a definition of {@link ObjectMapper} which will be used to
     * read/write JSON data for requests and from responses.
     *
     * @param mockMvc      {@link MockMvc} which will be used to make a request
     * @param objectMapper {@link ObjectMapper} used to read/write JSON in response/request
     * @return MvcRequester
     */
    public static MvcRequester on(MockMvc mockMvc,
                                  ObjectMapper objectMapper) {

        return new MvcRequester(mockMvc, objectMapper, objectMapper);
    }

    /**
     * Static factory method, with a definition of {@link ObjectMapper} which will be used to
     * read/write JSON data for requests and from responses.
     *
     * @param mockMvc           {@link MockMvc} which will be used to make a request
     * @param sendJsonMapper    {@link ObjectMapper} to write JSON data before send it in request
     * @param receiveJsonMapper {@link ObjectMapper} to read JSON data from response
     * @return MvcRequester
     */
    public static MvcRequester on(MockMvc mockMvc,
                                  ObjectMapper sendJsonMapper,
                                  ObjectMapper receiveJsonMapper) {

        return new MvcRequester(mockMvc, sendJsonMapper, receiveJsonMapper);
    }

    /**
     * Send request to selected URI
     *
     * @param pattern pattern of the URI to the resource
     * @param args    values of arguments which used in pattern
     */
    public MvcRequestPointed to(String pattern, Object... args) {
        String url = pattern.trim();
        if (url.charAt(0) != '/') {
            url = '/' + url;
        }
        return new MvcRequestPointed(mockMvc, UriComponentsBuilder.fromUriString(url)
                                                                  .buildAndExpand(args)
                                                                  .encode()
                                                                  .toUri(),
                                     sendJsonMapper,
                                     receiveJsonMapper);
    }
}