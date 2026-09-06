package com.instagram.backend.util;

import java.net.URI;

public class UrlValidator {

    private UrlValidator() {
    }

    public static boolean isValidInstagramUrl(String url) {

        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {

            URI uri = new URI(url);

            String host = uri.getHost();

            if (host == null) {
                return false;
            }

            host = host.toLowerCase();

            return host.equals("instagram.com")
                    || host.equals("www.instagram.com")
                    || host.equals("m.instagram.com");

        } catch (Exception exception) {

            return false;
        }
    }
}