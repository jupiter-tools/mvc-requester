package com.jupiter.tools.mvc.requester.url;

import java.net.URI;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created on 07/04/2020
 * <p>
 *
 * @author Korovin Anatoliy
 */
public class UriBuilder {

    public URI build(String pattern, Object[] args) {
        String urlPattern = pattern.trim();
        URI uri = internalBuildUri(urlPattern, args);
        return startFromDash(uri)
               ? uri
               : internalBuildUri('/' + urlPattern, args);
    }

    private URI internalBuildUri(String url, Object[] args) {

        UriComponents components = (args != null)
                                   ? UriComponentsBuilder.fromUriString(url).buildAndExpand(args)
                                   : UriComponentsBuilder.fromUriString(url).build();

        return components.encode().toUri();
    }

    private boolean startFromDash(URI uri) {
        return uri.toString().charAt(0) == '/';
    }
}
