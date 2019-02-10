package com.jupiter.tools.mvc.requester;

import org.apache.commons.io.IOUtils;
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
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 10.02.2019.
 *
 * @author Korovin Anatoliy
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = MvcRequesterWithFile.WebConfig.class)
class MvcRequesterWithFile {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void testMultipartFile() throws Exception {
        // Arrange
        byte[] data = "file content".getBytes();
        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/create")
                                    .withFile("data",
                                              "filename.txt",
                                              MimeType.valueOf("text/plain"),
                                              data)
                                    .upload()
                                    .returnAsPrimitive(String.class);
        // Asserts
        assertThat(result).isEqualTo("file content");
    }


    @Configuration
    @EnableWebMvc
    static class WebConfig implements WebMvcConfigurer {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @PostMapping("/create")
            public String upload(@RequestPart(value = "data") MultipartFile multipartFile) throws IOException {

                assertThat(multipartFile.getOriginalFilename()).isEqualTo("filename.txt");

                try (InputStream fileStream = multipartFile.getInputStream()) {
                    String result = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
                    assertThat(result).isEqualTo("file content");
                    return result;
                }
            }

        }
    }
}
