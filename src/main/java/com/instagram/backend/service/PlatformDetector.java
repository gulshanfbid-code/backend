package com.instagram.backend.service;

import com.instagram.backend.util.Platform;
import com.instagram.backend.util.UrlValidator;
import org.springframework.stereotype.Service;

@Service
public class PlatformDetector {

    public Platform detect(String url) {
        return UrlValidator.detectPlatform(url);
    }

    public String detectDisplayName(String url) {
        Platform platform = detect(url);
        return platform == null ? null : platform.getDisplayName();
    }
}
