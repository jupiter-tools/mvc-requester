package com.jupiter.tools.mvc.requester;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by Maxim Seredkin on 29.03.2018.
 * <p>
 * Пост-процессор для добавление OAuth авторизации в запросы для MockMvc.
 *
 * @author Maxim Seredkin
 */
public class OAuthRequestPostProcessor implements RequestPostProcessor {
    private final String token;

    public OAuthRequestPostProcessor(String token) {
        this.token = token;
    }

    @Override
    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
        if (isNotBlank(token)) {
            mockHttpServletRequest.addHeader("Authorization", String.format("Bearer %s", token));
        }

        return mockHttpServletRequest;
    }
}