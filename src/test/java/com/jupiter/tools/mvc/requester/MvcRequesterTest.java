package com.jupiter.tools.mvc.requester;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

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
    void expectStatusAndReturn() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/hello")
                                    .get()
                                    // Assert
                                    .expectStatus(OK)
                                    .returnAsPrimitive(String.class);

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void returnAsPrimitiveToInteger() {
        // Act
        Integer result = MvcRequester.on(mockMvc)
                                     .to("/test/integer")
                                     .get()
                                     // Assert
                                     .expectStatus(OK)
                                     .returnAsPrimitive(Integer.class);

        assertThat(result).isEqualTo(42);
    }

    @Test
    void testCreate() {
        // Arrange
        // Act
        MvcRequester.on(mockMvc)
                    .to("/test/create")
                    .post()
                    // Assert
                    .expectStatus(HttpStatus.CREATED);
    }

    @Test
    void testUrlTrim() {
        // Arrange
        // Act
        MvcRequester.on(mockMvc)
                    .to(" /test/create ")
                    .post()
                    // Assert
                    .expectStatus(HttpStatus.CREATED);
    }

    @Test
    void testAppendSlashInBeginOfUrl() {
        // Arrange
        // Act
        MvcRequester.on(mockMvc)
                    .to("test/create")
                    .post()
                    // Assert
                    .expectStatus(HttpStatus.CREATED);
    }

    @Test
    void testEmptyResponse() {
        // Act
        String res = MvcRequester.on(mockMvc)
                                 .to("test/create")
                                 .post()
                                 .returnAsPrimitive(String.class);
        // Assert
        assertThat(res).isNull();
    }

    @Test
    void testSendHeaders() {

        String result = MvcRequester.on(mockMvc)
                                    .to("test/headers/check")
                                    .withHeader("custom-header", "12345")
                                    .get()
                                    .returnAsPrimitive(String.class);

        assertThat(result).isEqualTo("secret");
    }

    @Test
    void testGetHeaders() {
        // Act
        MvcRequester.on(mockMvc)
                    .to("test/headers/get")
                    .get()
                    // Assert
                    .expectHeader("response-header", "12345");
    }


    @Test
    void expectHttpStatus() {
        // Act
        MvcRequester.on(mockMvc)
                    .to("/test/hello")
                    .get()
                    // Assert
                    .expectStatus(OK);
    }

    @Test
    void expectWithWrongStatusMustThrowAssertionError() {
        Assertions.assertThrows(AssertionError.class,
                                () -> MvcRequester.on(mockMvc)
                                                  .to("/test/error")
                                                  .get()
                                                  .expectStatus(OK));
    }

    @Test
    void doExpectTest() {
        MvcRequester.on(mockMvc)
                    .to("/test/hello")
                    .get()
                    .doExpect(MockMvcResultMatchers.content().string("hello world"));
    }

    @Test
    void skipUrlPrefix() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("test/hello")  // URI without starting `/`
                                    .get()
                                    // Assert
                                    .expectStatus(OK)
                                    .returnAsPrimitive(String.class);

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void urlPrefixInFirstArg() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("{url}/hello", "/test") // starting `/` in the first arg
                                    .get()
                                    // Assert
                                    .expectStatus(OK)
                                    .returnAsPrimitive(String.class);

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void withoutUrlPrefixInFirstArg() {
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("{url}/hello", "test")
                                    .get()
                                    // Assert
                                    .expectStatus(OK)
                                    .returnAsPrimitive(String.class);

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void returnPlainResponse() {
        // Arrange
        byte[] expected = "hello world".getBytes();
        // Act
        MockHttpServletResponse response = MvcRequester.on(mockMvc)
                                                       .to("/test/hello")
                                                       .get()
                                                       .returnResponse();
        // Assert
        assertThat(response.getStatus()).isEqualTo(OK.value());
        byte[] result = response.getContentAsByteArray();
        assertThat(Arrays.equals(expected, result)).isTrue();
    }

    @Test
    void setCharsetCp1251() {
        String response = MvcRequester.on(mockMvc)
                                  .to("/test/charset/cp1251")
                                  .get()
                                  .charset(Charset.forName("cp1251"))
                                  .returnAsPrimitive(String.class);
        assertThat(response).isEqualTo("ђ");
    }

    @Test
    void useDefaultCharsetUtf8() {
        String response = MvcRequester.on(mockMvc)
                                      .to("/test/charset/utf")
                                      .get()
                                      .returnAsPrimitive(String.class);
        assertThat(response).isEqualTo("йо-хо-хойя");
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

            @GetMapping(value = "/charset/cp1251", produces = "text/plain;charset=cp1251")
            public String cp1251Charset(){
                byte[] bytes = "ђ".getBytes(Charset.forName("cp1251"));
                return new String(bytes, Charset.forName("cp1251"));
            }

            @GetMapping(value = "/charset/utf", produces = "text/plain;charset=utf8")
            public String utf8Charset(){
                byte[] bytes = "йо-хо-хойя".getBytes(StandardCharsets.UTF_8);
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }
}
