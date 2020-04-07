package com.jupiter.tools.mvc.requester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.MimeType;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.jupiter.tools.mvc.requester.SneakyThrow.wrap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * Created on 03.08.2018.
 * <p>
 * Builder for MVC requests, you can use it to: <br/>
 * - set request parameters or headers <br/>
 * - set request mapping <br/>
 * - set request body <br/>
 * - set authorization token <br/>
 * - upload files <br/>
 *
 * @author Sergey Vdovin
 * @author Korovin Anatoliy
 */
public class MvcRequestPointed {

    private final URI uri;
    private final MockMvc mockMvc;
    private final Multimap<String, String> params;
    private final Map<String, MvcRequestFileData> files;
    private final ObjectMapper sendJsonMapper;
    private final ObjectMapper receiveJsonMapper;
    private final Multimap<String, String> headers;
    private final List<RequestPostProcessor> postProcessors;

    MvcRequestPointed(MockMvc mockMvc,
                      URI uri,
                      ObjectMapper sendJsonMapper,
                      ObjectMapper receiveJsonMapper) {
        this.uri = uri;
        this.mockMvc = mockMvc;
        this.params = ArrayListMultimap.create();
        this.files = new HashMap<>();
        this.sendJsonMapper = sendJsonMapper;
        this.receiveJsonMapper = receiveJsonMapper;
        this.headers = ArrayListMultimap.create();
        this.postProcessors = new ArrayList<>();
    }

    /**
     * Add a parameter in the request
     *
     * @param name   parameter name
     * @param values parameter value
     *
     * @return MvcRequestPointed
     */
    public MvcRequestPointed withParam(String name, Object... values) {
        for (Object value : values) {
            this.params.put(name, String.valueOf(value));
        }
        return this;
    }

    /**
     * Add a header in the request
     *
     * @param name   header name
     * @param values header value
     *
     * @return MvcRequestPointed
     */
    public MvcRequestPointed withHeader(String name, Object... values) {
        for (Object value : values) {
            this.headers.put(name, String.valueOf(value));
        }
        return this;
    }

    /**
     * Use OAuth authentication token in request headers
     *
     * @param token OAuth-token
     *
     * @return MvcRequestPointed
     */
    public MvcRequestPointed withOAuth(String token) {
        postProcessors.add(new OAuthRequestPostProcessor(token));
        return this;
    }

    /**
     * Use Basic authentication in request headers
     *
     * @param username имя пользователя.
     * @param password пароль пользователя.
     *
     * @return MvcRequestPointed
     */
    public MvcRequestPointed withBasicAuth(String username, String password) {
        postProcessors.add(httpBasic(username, password));
        return this;
    }

    public MvcRequestPointed withCsrf() {
        postProcessors.add(csrf());
        return this;
    }

    /**
     * Make a POST request without the body
     *
     * @return MvcRequestResult
     */
    public MvcRequestResult post() {
        ResultActions resultActions = wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::post)));
        return new MvcRequestResult(resultActions, receiveJsonMapper);
    }

    /**
     * Make a PUT request without parameters(or body)
     */
    public MvcRequestResult put() {
        ResultActions resultActions = wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::put)));
        return new MvcRequestResult(resultActions,
                                    receiveJsonMapper);
    }

    /**
     * Add a multipart-file in the request, use it with {@link #upload()} method
     *
     * @param fieldName multipart field name with a file content
     * @param fileData  byte array with a file content
     *
     * @return MvcRequestPointed
     */
    public MvcRequestPointed withFile(String fieldName,
                                      String originalFileName,
                                      MimeType mimeType,
                                      byte[] fileData) {

        this.files.put(fieldName,
                       new MvcRequestFileData(originalFileName, mimeType, fileData));

        return this;
    }

    /**
     * Make a file upload,
     * <br/>
     * To select a file you can use the {@link #withFile(String, String, MimeType, byte[])} method.
     */
    public MvcRequestResult upload() {
        ResultActions resultActions = wrap(() -> this.mockMvc.perform(makeUpload(null)));
        return new MvcRequestResult(resultActions, receiveJsonMapper);
    }

    /**
     * Make a file upload with OAuth-token
     * <br/>
     * To select a file you can use the {@link #withFile(String, String, MimeType, byte[])} method.
     *
     * @param token oauth-token
     *
     * @return MvcRequestResult
     */
    public MvcRequestResult uploadWithAuth(String token) {
        ResultActions resultActions = wrap(() -> this.mockMvc.perform(makeUpload(token)));
        return new MvcRequestResult(resultActions, receiveJsonMapper);
    }

    /**
     * Make a POST request with the selected body
     *
     * @param content request body, which convert in JSON before send
     *
     * @return MvcRequestResult
     */
    public MvcRequestResult post(Object content) {

        String jsonContent = wrap(() -> sendJsonMapper.writeValueAsString(content));

        ResultActions resultActions =
                wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::post)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(jsonContent)));

        return new MvcRequestResult(resultActions, receiveJsonMapper);
    }

    /**
     * Make a PUT request with the body
     *
     * @param content request body, which convert in JSON before send
     *
     * @return MvcRequestResult
     */
    public MvcRequestResult put(Object content) {
        return new MvcRequestResult(
                wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::put)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(sendJsonMapper.writeValueAsString(content)))),
                receiveJsonMapper);
    }

    /**
     * Make a DELETE request without the body
     *
     * @return MvcRequestResult
     */
    public MvcRequestResult delete() {
        return new MvcRequestResult(
                wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::delete)
                                                   .contentType(MediaType.APPLICATION_JSON))),
                receiveJsonMapper);
    }

    /**
     * Make a DELETE request with json body
     *
     * @param content object which will send as JSON body in the request
     *
     * @return MvcRequestResult
     */
    public MvcRequestResult delete(Object content) {
        return new MvcRequestResult(
                wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::delete)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(sendJsonMapper.writeValueAsString(content)))),
                receiveJsonMapper);
    }

    /**
     * Make a GET request
     */
    public MvcRequestResult get() {
        return new MvcRequestResult(wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::get))),
                                    receiveJsonMapper);
    }

    /**
     * Make a GET request with the body
     *
     * @param content object which will send as JSON body in the request
     */
    public MvcRequestResult get(Object content) {
        return new MvcRequestResult(
                wrap(() -> mockMvc.perform(make(MockMvcRequestBuilders::get)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(sendJsonMapper.writeValueAsString(content)))),
                receiveJsonMapper);
    }

    private MockHttpServletRequestBuilder make(Function<URI, MockHttpServletRequestBuilder> builderSupplier) {

        MockHttpServletRequestBuilder builder = builderSupplier.apply(uri);
        return prepareRequest(builder);
    }

    /**
     * set request parameters, headers and postProcessors
     *
     * @param builder request
     */
    private MockHttpServletRequestBuilder prepareRequest(MockHttpServletRequestBuilder builder) {
        if (!params.isEmpty()) {
            params.asMap().forEach(
                    (key, values) ->
                            values.forEach(value ->
                                                   builder.param(key, value)));
        }
        if (!headers.isEmpty()) {
            headers.asMap().forEach(
                    (key, values) ->
                            values.forEach(value ->
                                                   builder.header(key, value)));
        }
        if (!postProcessors.isEmpty()) {
            postProcessors.forEach(builder::with);
        }
        return builder;
    }

    /**
     * Make a POST request to upload a file
     *
     * @param token OAuth token
     *
     * @return MockHttpServletRequestBuilder
     */
    private MockHttpServletRequestBuilder makeUpload(String token) {
        // here using the deprecated method - `fileUpload`, instead of new version -
        // `multipart`, because this provide an ability to use MvcRequester with Spring 4.xx
        MockMultipartHttpServletRequestBuilder builder = fileUpload(uri);
        if (isNotBlank(token)) {
            builder.header("Authorization", String.format("Bearer %s", token));
        }
        for (Map.Entry<String, MvcRequestFileData> entry : this.files.entrySet()) {
            MvcRequestFileData data = entry.getValue();

            String contentType = data.getMimeType() == null
                                 ? null
                                 : data.getMimeType().toString();

            MockMultipartFile mockMultipartFile = new MockMultipartFile(entry.getKey(),
                                                                        data.getOriginalFileName(),
                                                                        contentType,
                                                                        data.getFileData());
            builder.file(mockMultipartFile);
        }
        return prepareRequest(builder);
    }


}
