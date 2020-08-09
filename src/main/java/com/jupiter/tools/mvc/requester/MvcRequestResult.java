package com.jupiter.tools.mvc.requester;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static com.jupiter.tools.mvc.requester.SneakyThrow.wrap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 03.08.2018.
 * <p>
 * Checks or returns the result of request.
 *
 * @author Sergey Vdovin
 * @author Korovin Anatoliy
 */
public class MvcRequestResult {

    private final ResultActions resultActions;
    private final ObjectMapper jsonMapper;

    private Charset charset = StandardCharsets.UTF_8;

    MvcRequestResult(ResultActions resultActions, ObjectMapper jsonMapper) {
        this.resultActions = resultActions;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Run a custom assertion on the result
     *
     * @param matcher expected assertion of result
     * @return MvcRequestResult
     */
    public MvcRequestResult doExpect(ResultMatcher matcher) {
        return wrap(() -> {
            resultActions.andDo(print());
            resultActions.andExpect(matcher);
            return this;
        });
    }

    /**
     * check status code of the response
     *
     * @param status expected status code
     * @return MvcRequestResult
     */
    public MvcRequestResult expectStatus(HttpStatus status) {
        return wrap(() -> {
            resultActions.andDo(print());
            resultActions.andExpect(status().is(status.value()));
            return this;
        });
    }

    /**
     * check the header value in the response
     *
     * @param name  header name
     * @param value expected value of this header
     * @return MvcRequestResult
     */
    public MvcRequestResult expectHeader(String name, String value) {
        return wrap(() -> {
            resultActions.andDo(print());
            resultActions.andExpect(header().string(name, value));
            return this;
        });
    }

    /**
     * Convert the response from JSON to expected object type.
     * You can use it to return a value which parametrized by generic type.
     *
     * @param typeReference type of expected response
     * @param <ResultType>  generic parameter
     * @return ResultType result of invocation
     */
    public <ResultType> ResultType doReturn(TypeReference<ResultType> typeReference) {
        return wrap(() -> {
            resultActions.andDo(print());
            String body = getResponseBody(resultActions);
            return isBlank(body) ? null : jsonMapper.readValue(body, typeReference);
        });
    }

    /**
     * Return a response body converted to expected type from JSON.
     *
     * @param returnType   expected type of response body
     * @param <ResultType> expected type
     * @return ResultType result of invocation
     */
    public <ResultType> ResultType returnAs(Class<ResultType> returnType) {
        return wrap(() -> {
            resultActions.andDo(print());
            String body = getResponseBody(resultActions);
            return isBlank(body) ? null : jsonMapper.readerFor(returnType).readValue(body);
        });
    }

    /**
     * return result as a primitive type
     *
     * @param returnType   expected type of result
     * @param <ResultType> expected type
     * @return ResultType result of invocation
     */
    public <ResultType> ResultType returnAsPrimitive(Class<ResultType> returnType) {
        return wrap(() -> {
            resultActions.andDo(print());
            String body = getResponseBody(resultActions);
            return isBlank(body) ? null : (ResultType) PrimitiveConverter.convertToPrimitive(body, returnType);
        });
    }

    /**
     * Set charset for response converting
     * @param charset expected charset for response
     * @return MvcRequestResult instance
     */
    public MvcRequestResult charset(Charset charset){
        this.charset = charset;
        return this;
    }

    /**
     * Return a plain received response of the REST-API invocation
     *
     * @return MockHttpServletResponse
     */
    public MockHttpServletResponse returnResponse() {
        return wrap(() -> {
            resultActions.andDo(print());
            return resultActions.andReturn().getResponse();
        });
    }

    private String getResponseBody(ResultActions resultActions) {
        byte[] bytes = resultActions.andReturn()
                                    .getResponse()
                                    .getContentAsByteArray();
        return new String(bytes, this.charset);
    }
}
