package com.jupiter.tools.mvc.requester;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 10.02.2019.
 *
 * @author Korovin Anatoliy
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = MvcRequesterDoReturnTest.WebConfig.class)
class MvcRequesterDoReturnTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void returnParametrizedType() {
        // Act
        List<SimpleObject> objectList = MvcRequester.on(mockMvc)
                                                    .to("/test/objects")
                                                    .get()
                                                    .doReturn(new TypeReference<List<SimpleObject>>() {});
        // Asserts
        assertThat(objectList).isNotNull()
                              .hasSize(3)
                              .extracting(SimpleObject::getName)
                              .containsOnly("AAA", "BBB", "CCC");
    }

    @Test
    void returnParametrizedTypeInUtf8() {
        // Act
        List<SimpleObject> objectList = MvcRequester.on(mockMvc)
                                                    .to("/test/objects/charset")
                                                    .get()
                                                    .charset(Charset.forName("utf-8"))
                                                    .doReturn(new TypeReference<List<SimpleObject>>() {});
        // Asserts
        assertThat(objectList).isNotNull()
                              .hasSize(1)
                              .extracting(SimpleObject::getName)
                              .containsOnly("йо-хо-хо");
    }

    @Test
    void returnNull() {
        // Act
        SimpleObject result = MvcRequester.on(mockMvc)
                                                    .to("/test/empty")
                                                    .get()
                                                    .doReturn(new TypeReference<SimpleObject>() {});
        // Asserts
        assertThat(result).isNull();
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig implements WebMvcConfigurer {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @GetMapping("/objects")
            public List<SimpleObject> getObject() {
                SimpleObject a = new SimpleObject("AAA", 1);
                SimpleObject b = new SimpleObject("BBB", 1);
                SimpleObject c = new SimpleObject("CCC", 1);
                return Arrays.asList(a, b, c);
            }

            @GetMapping("/empty")
            public SimpleObject getEmpty(){
                return null;
            }

            @GetMapping(value = "/objects/charset", produces = "application/json;charset=utf8")
            public List<SimpleObject> getObjectsCharset() {
                return Arrays.asList(new SimpleObject("йо-хо-хо", 1));
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
