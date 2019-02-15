package com.jupiter.tools.mvc.requester;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.MimeType.valueOf;

/**
 * Created on 12.02.2019.
 *
 * @author Korovin Anatoliy
 */
class MvcRequestFileDataTest {

    @Test
    void construct() {
        // Arrange
        byte[] data = "content content".getBytes();
        // Act
        MvcRequestFileData requestFileData = new MvcRequestFileData("filename.txt",
                                                                    valueOf("plain/text"),
                                                                    data);
        // Asserts
        assertThat(requestFileData).isNotNull();
        assertThat(requestFileData.getMimeType()).isEqualTo(valueOf("plain/text"));
        assertThat(requestFileData.getFileData()).isEqualTo(data);
        assertThat(requestFileData.getOriginalFileName()).isEqualTo("filename.txt");
    }
}