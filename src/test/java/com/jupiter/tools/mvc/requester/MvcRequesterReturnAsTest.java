package com.jupiter.tools.mvc.requester;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import org.springframework.web.bind.annotation.*;
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
@ContextConfiguration(classes = MvcRequesterReturnAsTest.WebConfig.class)
class MvcRequesterReturnAsTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void testReturnAs() {
        // Act
        SimpleObject result = MvcRequester.on(mockMvc)
                                          .to("/test/object")
                                          .get()
                                          .returnAs(SimpleObject.class);
        // Asserts
        assertThat(result).isNotNull()
                          .extracting(SimpleObject::getName, SimpleObject::getValue)
                          .containsOnly("test-name", 1987);
    }

    @Test
    void getWithParams() {
        // Act
        SimpleObject result = MvcRequester.on(mockMvc)
                                          .to("/test/custom-object")
                                          .withParam("name", "custom")
                                          .withParam("value", 10101)
                                          .get()
                                          .returnAs(SimpleObject.class);
        // Asserts
        assertThat(result).isNotNull()
                          .extracting(SimpleObject::getName, SimpleObject::getValue)
                          .containsOnly("custom", 10101);
    }

    @Test
    void getWithPathVariable() {
        // Act
        SimpleObject result = MvcRequester.on(mockMvc)
                                          .to("/test/{id}/object", 7)
                                          .get()
                                          .returnAs(SimpleObject.class);
        // Asserts
        assertThat(result).isNotNull()
                          .extracting(SimpleObject::getName, SimpleObject::getValue)
                          .containsOnly("7", 7);
    }

    @Test
    void simplePost() {
        // Act
        SimpleObject result = MvcRequester.on(mockMvc)
                                          .to("/test/object")
                                          .post()
                                          .returnAs(SimpleObject.class);
        // Asserts
        assertThat(result).isNotNull()
                          .extracting(SimpleObject::getName, SimpleObject::getValue)
                          .containsOnly("simple-post", 1987);
    }

    @Test
    void postObject() {
        // Arrange
        SimpleObject postBody = new SimpleObject("body", 987);
        // Act
        SimpleObject result = MvcRequester.on(mockMvc)
                                          .to("/test/object-body")
                                          .post(postBody)
                                          .returnAs(SimpleObject.class);
        // Asserts
        assertThat(result).isNotNull()
                          .extracting(SimpleObject::getName, SimpleObject::getValue)
                          .containsOnly("body-test", 1987);
    }

    @Test
    void getEmpty() {
        // Act
        SimpleObject result = MvcRequester.on(mockMvc)
                                          .to("/test/empty")
                                          .get()
                                          .returnAs(SimpleObject.class);
        // Asserts
        assertThat(result).isNull();
    }


    @Configuration
    @EnableWebMvc
    static class WebConfig {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @GetMapping("/object")
            public SimpleObject getObject() {
                return new SimpleObject("test-name", 1987);
            }

            @GetMapping("/custom-object")
            public SimpleObject getWithParams(@RequestParam("name") String name,
                                              @RequestParam("value") int value) {
                return new SimpleObject(name, value);
            }

            @GetMapping("/{id}/object")
            public SimpleObject getWithPathVariable(@PathVariable("id") int id) {
                return new SimpleObject(String.valueOf(id), id);
            }

            @PostMapping("/object")
            public SimpleObject postSimple() {
                return new SimpleObject("simple-post", 1987);
            }

            @PostMapping("/object-body")
            public SimpleObject postWithBody(@RequestBody SimpleObject body) {
                return new SimpleObject(body.getName() + "-test",
                                        body.getValue() + 1000);
            }

            @GetMapping("/empty")
            public SimpleObject getEmpty(){
                return null;
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
