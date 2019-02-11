package com.jupiter.tools.mvc.requester;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 10.02.2019.
 *
 * @author Korovin Anatoliy
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = MvcRequesterCsrfTest.WebConfig.class)
class MvcRequesterCsrfTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void testCsrf() throws Exception {
        // Act
        String res = MvcRequester.on(mockMvc)
                                 .to("/test/get")
                                 .withCsrf()
                                 .get()
                                 .returnAsPrimitive(String.class);
        // Assert
        assertThat(res).isEqualTo("ok");
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig implements WebMvcConfigurer {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @GetMapping("get")
            public String csrf(HttpServletRequest request) {
                String csrf = request.getParameter("_csrf");
                assertThat(csrf).isNotNull();
                return "ok";
            }
        }
    }
}
