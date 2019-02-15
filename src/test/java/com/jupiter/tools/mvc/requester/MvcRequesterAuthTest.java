package com.jupiter.tools.mvc.requester;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
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
import org.springframework.web.bind.annotation.PostMapping;
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
@ContextConfiguration(classes = MvcRequesterAuthTest.WebConfig.class)
class MvcRequesterAuthTest {

    private static String TOKEN = "12345-12345";
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getWithOAuth() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/oauth")
                                    .withOAuth(TOKEN)
                                    .get()
                                    .returnAsPrimitive(String.class);
        // Asserts
        assertThat(result).isEqualTo(TOKEN);
    }

    @Test
    void postWithOAuth() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/oauth")
                                    .withOAuth(TOKEN)
                                    .post()
                                    .returnAsPrimitive(String.class);
        // Asserts
        assertThat(result).isEqualTo(TOKEN);
    }

    @Test
    void getWithBasicAuth() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/basic")
                                    .withBasicAuth("root", "12345")
                                    .get()
                                    .returnAsPrimitive(String.class);
        // Asserts
        assertThat(result).isEqualTo("cm9vdDoxMjM0NQ==");
    }

    @Test
    void postWithBasicAuth() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/basic")
                                    .withBasicAuth("root", "12345")
                                    .post()
                                    .returnAsPrimitive(String.class);
        // Asserts
        assertThat(result).isEqualTo("cm9vdDoxMjM0NQ==");
    }

    @Test
    void postWithNullToken() {
        // Act
        Assertions.assertThrows(Exception.class,
                                () -> MvcRequester.on(mockMvc)
                                              .to("/test/oauth")
                                              .withOAuth(null)
                                              .post());
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @GetMapping("/oauth")
            public String getOAuth(HttpServletRequest request) {

                String authorization = request.getHeader("Authorization");
                assertThat(authorization).isEqualTo("Bearer " + TOKEN);

                return authorization.replaceFirst("Bearer ", "");
            }

            @PostMapping("/oauth")
            public String postOAuth(HttpServletRequest request) {

                String authorization = request.getHeader("Authorization");
                assertThat(authorization).isEqualTo("Bearer " + TOKEN);

                return authorization.replaceFirst("Bearer ", "");
            }

            @GetMapping("/basic")
            public String getBasicAuth(HttpServletRequest request) {

                String authorization = request.getHeader("Authorization");
                assertThat(authorization).isEqualTo("Basic cm9vdDoxMjM0NQ==");

                return authorization.replaceFirst("Basic ", "");
            }

            @PostMapping("/basic")
            public String postBasicAuth(HttpServletRequest request) {

                String authorization = request.getHeader("Authorization");
                assertThat(authorization).isEqualTo("Basic cm9vdDoxMjM0NQ==");

                return authorization.replaceFirst("Basic ", "");
            }
        }
    }
}
