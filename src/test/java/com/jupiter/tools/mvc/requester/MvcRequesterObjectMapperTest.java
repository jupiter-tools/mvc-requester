package com.jupiter.tools.mvc.requester;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 10.02.2019.
 *
 * @author Korovin Anatoliy
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = MvcRequesterObjectMapperTest.WebConfig.class)
class MvcRequesterObjectMapperTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void customSendMapper() {
        // Arrange
        ObjectMapper sendMapper = new ObjectMapper();
        ObjectMapper receiveMapper = new ObjectMapper();
        sendMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleObject body = new SimpleObject(null, 123);
        // Act
        String result = MvcRequester.on(mockMvc, sendMapper, receiveMapper)
                                    .to("test/object")
                                    .post(body)
                                    .returnAsPrimitive(String.class);
        // Assert
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void customReceiveMapper() {
        // Arrange
        ObjectMapper sendMapper = new ObjectMapper();
        ObjectMapper receiveMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new CustomDeserializer());
        receiveMapper.registerModule(module);

        // Act
        SimpleObject result = MvcRequester.on(mockMvc, sendMapper, receiveMapper)
                                          .to("test/empty")
                                          .post()
                                          .returnAs(SimpleObject.class);
        // Assert
        assertThat(result.getName()).isNull();
    }

    @Test
    void customSendAndReceiveMapper() {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new CustomDeserializer());
        mapper.registerModule(module);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleObject body = new SimpleObject(null, 123);

        // Act
        SimpleObject result = MvcRequester.on(mockMvc, mapper)
                                          .to("test/empty-with-body")
                                          .post(body)
                                          .returnAs(SimpleObject.class);
        // Assert
        assertThat(result.getName()).isNull();
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig implements WebMvcConfigurer {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @PostMapping("/object")
            public String postObject(@RequestBody String body) {
                assertThat(body).isNotNull()
                                .isEqualTo("{\"value\":123}");
                return "ok";
            }

            @PostMapping("/empty")
            public SimpleObject returnWithEmptyString() {
                return new SimpleObject("", 123);
            }

            @PostMapping("/empty-with-body")
            public SimpleObject receiveAndReturn(@RequestBody String body) {
                assertThat(body).isEqualTo("{\"value\":123}");
                return new SimpleObject("", 123);
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class SimpleObject {
        private String name;
        private Integer value;
    }

    static class CustomDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext context)
                throws IOException, JsonProcessingException {

            JsonNode node = jsonParser.readValueAsTree();
            if (node.asText().isEmpty()) {
                return null;
            }
            return node.toString();

        }
    }
}
