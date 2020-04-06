package com.jupiter.tools.mvc.requester.url;

import java.net.URI;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class UriBuilderTest {

    @Test
    void withEmptyArgsArray() {
        UriBuilder builder = new UriBuilder();
        URI uri = builder.build("/path/test", new Object[]{});
        assertThat(uri.toString()).isEqualTo("/path/test");
    }

    @Test
    void withoutArgsArray() {
        UriBuilder builder = new UriBuilder();
        URI uri = builder.build("/path/test", null);
        assertThat(uri.toString()).isEqualTo("/path/test");
    }

    @Test
    void replaceOneArgs() {
        UriBuilder builder = new UriBuilder();
        URI uri = builder.build("/path/{var}/test", new Object[]{123});
        assertThat(uri.toString()).isEqualTo("/path/123/test");
    }

    @Test
    void replaceMultipleArgs() {
        UriBuilder builder = new UriBuilder();
        URI uri = builder.build("/path/{var1}/test/{var2}", new Object[]{123, "complete"});
        assertThat(uri.toString()).isEqualTo("/path/123/test/complete");
    }

    @Test
    void skipPrefixInPattern() {
        UriBuilder builder = new UriBuilder();
        URI uri = builder.build("path/{var}/test", // skip starting dash `/` in URL
                                new Object[]{123});
        assertThat(uri.toString()).isEqualTo("/path/123/test");
    }

    @Test
    void skipPrefixInTheFirstArg() {
        UriBuilder builder = new UriBuilder();
        URI uri = builder.build("{url}/{var}/test", // skip starting dash `/` in URL
                                new Object[]{"path", 123});
        assertThat(uri.toString()).isEqualTo("/path/123/test");
    }

    @Test
    void prefixInTheFirstArg() {
        UriBuilder builder = new UriBuilder();
        URI uri = builder.build("{url}/{var}/test", // skip starting dash `/` in URL
                                new Object[]{"/path", 123});
        assertThat(uri.toString()).isEqualTo("/path/123/test");
    }
}