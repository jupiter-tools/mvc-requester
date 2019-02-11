package com.jupiter.tools.mvc.requester;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
class MvcRequesterRequestMappingTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @ExtendWith(SpringExtension.class)
    @WebAppConfiguration
    @ContextConfiguration(classes = MvcRequesterRequestMappingTest.WebConfig.class)
    @Nested
    class SimpleTests {


        @Test
        void get() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("simple/hello")
                                        .get()
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("GET");
        }

        @Test
        void post() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("simple/hello")
                                        .post()
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("POST");
        }

        @Test
        void put() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("simple/hello")
                                        .put()
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("PUT");
        }

        @Test
        void delete() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("simple/hello")
                                        .delete()
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("DELETE");
        }
    }

    @ExtendWith(SpringExtension.class)
    @WebAppConfiguration
    @ContextConfiguration(classes = MvcRequesterRequestMappingTest.WebConfig.class)
    @Nested
    class BodyTests {

        private SimpleObject body = new SimpleObject("body", 123);

        @Test
        void get() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("body/hello")
                                        .get(body)
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("GET");
        }

        @Test
        void post() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("body/hello")
                                        .post(body)
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("POST");
        }

        @Test
        void put() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("body/hello")
                                        .put(body)
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("PUT");
        }

        @Test
        void delete() throws Exception {
            // Act
            String result = MvcRequester.on(mockMvc)
                                        .to("body/hello")
                                        .delete(body)
                                        .returnAsPrimitive(String.class);
            // Assert
            assertThat(result).isEqualTo("DELETE");
        }
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig implements WebMvcConfigurer {

        @RestController
        @RequestMapping("/simple")
        public class TestController {

            @GetMapping("/hello")
            public String getHello() {
                return "GET";
            }

            @PostMapping("/hello")
            public String postHello() {
                return "POST";
            }

            @PutMapping("/hello")
            public String putHello() {
                return "PUT";
            }

            @DeleteMapping("/hello")
            public String deleteHello(@RequestBody(required = false) SimpleObject body) {
                return "DELETE";
            }
        }

        @RestController
        @RequestMapping("/body")
        public class TestControllerWithBody {

            @GetMapping("/hello")
            public String getHello(@RequestBody SimpleObject body) {
                assertThat(body).extracting(SimpleObject::getName, SimpleObject::getValue)
                                .contains("body", 123);
                return "GET";
            }

            @PostMapping("/hello")
            public String postHello(@RequestBody SimpleObject body) {
                assertThat(body).extracting(SimpleObject::getName, SimpleObject::getValue)
                                .contains("body", 123);
                return "POST";
            }

            @PutMapping("/hello")
            public String putHello(@RequestBody SimpleObject body) {
                assertThat(body).extracting(SimpleObject::getName, SimpleObject::getValue)
                                .contains("body", 123);
                return "PUT";
            }

            @DeleteMapping("/hello")
            public String deleteHello(@RequestBody SimpleObject body) {
                assertThat(body).extracting(SimpleObject::getName, SimpleObject::getValue)
                                .contains("body", 123);
                return "DELETE";
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class SimpleObject {
        private String name;
        private int value;
    }
}
