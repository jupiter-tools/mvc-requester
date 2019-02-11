package com.jupiter.tools.mvc.requester;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 30.01.2019.
 *
 * @author Korovin Anatoliy
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = MvcRequesterTest.WebConfig.class)
class MvcRequesterTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void expectStatusAndReturn() throws Exception {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/hello")
                                    .get()
                                    // Assert
                                    .expectStatus(HttpStatus.OK)
                                    .returnAsPrimitive(String.class);

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void returnAsPrimitiveToInteger() throws Exception {
        // Act
        Integer result = MvcRequester.on(mockMvc)
                                     .to("/test/integer")
                                     .get()
                                     // Assert
                                     .expectStatus(HttpStatus.OK)
                                     .returnAsPrimitive(Integer.class);

        assertThat(result).isEqualTo(42);
    }

    @Test
    void testCreate() throws Exception {
        // Arrange
        // Act
        MvcRequester.on(mockMvc)
                    .to("/test/create")
                    .post()
                    // Assert
                    .expectStatus(HttpStatus.CREATED);
    }

    @Test
    void testUrlTrim() throws Exception {
        // Arrange
        // Act
        MvcRequester.on(mockMvc)
                    .to(" /test/create ")
                    .post()
                    // Assert
                    .expectStatus(HttpStatus.CREATED);
    }

    @Test
    void testAppendSlashInBeginOfUrl() throws Exception {
        // Arrange
        // Act
        MvcRequester.on(mockMvc)
                    .to("test/create")
                    .post()
                    // Assert
                    .expectStatus(HttpStatus.CREATED);
    }

    @Test
    void testEmptyResponse() throws Exception {
        // Act
        String res = MvcRequester.on(mockMvc)
                               .to("test/create")
                               .post()
                               .returnAsPrimitive(String.class);
        // Assert
        assertThat(res).isNull();
    }

    @Test
    void testSendHeaders() throws Exception {

        String result = MvcRequester.on(mockMvc)
                                    .to("test/headers/check")
                                    .withHeader("custom-header", "12345")
                                    .get()
                                    .returnAsPrimitive(String.class);

        assertThat(result).isEqualTo("secret");
    }

    @Test
    void testGetHeaders() throws Exception {
        // Act
        MvcRequester.on(mockMvc)
                    .to("test/headers/get")
                    .get()
                    // Assert
                    .expectHeader("response-header", "12345");
    }


    @Test
    void expectHttpStatus() throws Exception {
        // Act
        MvcRequester.on(mockMvc)
                    .to("/test/hello")
                    .get()
                    // Assert
                    .expectStatus(HttpStatus.OK);
    }

    @Test
    void expectWithWrongStatusMustThrowAssertionError() throws Exception {
        Assertions.assertThrows(AssertionError.class,
                                () -> MvcRequester.on(mockMvc)
                                              .to("/test/error")
                                              .get()
                                              .expectStatus(HttpStatus.OK));
    }

    @Test
    void doExpectTest() throws Exception {
        MvcRequester.on(mockMvc)
                    .to("/test/hello")
                    .get()
                    .doExpect(MockMvcResultMatchers.content().string("hello world"));
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig implements WebMvcConfigurer {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @GetMapping("/hello")
            public String hello() {
                return "hello world";
            }

            @GetMapping("/error")
            @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
            public void error() {

            }

            @GetMapping("/integer")
            public int integer() {
                return 42;
            }

            @PostMapping("/create")
            @ResponseStatus(HttpStatus.CREATED)
            public void create() {

            }

            @GetMapping("/headers/check")
            public String checkHeader(HttpServletRequest request) {
                String header = request.getHeader("custom-header");
                return (header != null && header.equals("12345"))
                       ? "secret"
                       : "fail";
            }

            @GetMapping("/headers/get")
            public ResponseEntity<Void> returnHeader() {
                return ResponseEntity.status(200)
                                     .header("response-header", "12345")
                                     .build();
            }
        }
    }
}
