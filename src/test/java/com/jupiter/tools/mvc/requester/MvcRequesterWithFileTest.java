package com.jupiter.tools.mvc.requester;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
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
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
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
@ContextConfiguration(classes = MvcRequesterWithFileTest.WebConfig.class)
class MvcRequesterWithFileTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void uploadMultipartFile() {
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

    @Test
    void uploadFileWithToken() {
        // Arrange
        byte[] data = "file content".getBytes();
        // Act
        MvcRequester.on(mockMvc)
                    .to("/test/auth/create")
                    .withFile("data",
                              "filename.txt",
                              MimeType.valueOf("text/plain"),
                              data)
                    .uploadWithAuth("12345-12345");
    }

    @Test
    void uploadWithoutMimeType() {
        // Arrange
        byte[] data = "file content".getBytes();
        // Act
        Assertions.assertThrows(Exception.class,
                                () -> MvcRequester.on(mockMvc)
                                                  .to("/test/auth/create")
                                                  .withFile("data",
                                                            "filename.txt",
                                                            null,
                                                            data)
                                                  .upload());
    }

    @Test
    void uploadTwoFiles() {
        // Arrange
        byte[] firstData = "first file content".getBytes();
        byte[] secondData = "second file content".getBytes();

        // Act
        String result = MvcRequester.on(mockMvc)
                                    .to("/test/two")
                                    .withFile("first",
                                              "first.txt",
                                              MimeType.valueOf("text/plain"),
                                              firstData)
                                    .withFile("second",
                                              "second.txt",
                                              MimeType.valueOf("application/octet-stream"),
                                              secondData)
                                    .upload()
                                    .returnAsPrimitive(String.class);
        // Asserts
        assertThat(result).isEqualTo("first file content+second file content");
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig {

        @RestController
        @RequestMapping("/test")
        public class TestController {

            @PostMapping("/create")
            public String upload(@RequestPart(value = "data") MultipartFile multipartFile) throws IOException {

                assertThat(multipartFile.getOriginalFilename()).isEqualTo("filename.txt");
                assertThat(multipartFile.getContentType()).isEqualTo("text/plain");

                try (InputStream fileStream = multipartFile.getInputStream()) {
                    String result = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
                    assertThat(result).isEqualTo("file content");
                    return result;
                }
            }

            @PostMapping("/auth/create")
            public void authUpload(HttpServletRequest request,
                                   @RequestPart(value = "data") MultipartFile multipartFile) throws IOException {

                String authorization = request.getHeader("Authorization");
                assertThat(authorization).isEqualTo("Bearer 12345-12345");

                try (InputStream fileStream = multipartFile.getInputStream()) {
                    String result = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
                    assertThat(result).isEqualTo("file content");
                }
            }

            @PostMapping("/two")
            public String uploadTwoFiles(@RequestPart(value = "first") MultipartFile first,
                                         @RequestPart(value = "second") MultipartFile second) throws IOException {

                String result = "";

                assertThat(first.getOriginalFilename()).isEqualTo("first.txt");
                assertThat(first.getContentType()).isEqualTo("text/plain");
                try (InputStream fileStream = first.getInputStream()) {
                    String content = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
                    assertThat(content).isEqualTo("first file content");
                    result += content;
                }

                assertThat(second.getOriginalFilename()).isEqualTo("second.txt");
                assertThat(second.getContentType()).isEqualTo("application/octet-stream");
                try (InputStream fileStream = second.getInputStream()) {
                    String content = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
                    assertThat(content).isEqualTo("second file content");
                    result += "+" + content;
                }

                return result;
            }
        }
    }
}
