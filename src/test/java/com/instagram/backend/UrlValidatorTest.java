package com.instagram.backend;

import com.instagram.backend.util.Platform;
import com.instagram.backend.util.UrlValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    @Test
    void detectsInstagram() {
        assertEquals(Platform.INSTAGRAM,
                UrlValidator.detectPlatform("https://www.instagram.com/reel/ABC123/"));
    }

    @Test
    void detectsFacebook() {
        assertEquals(Platform.FACEBOOK,
                UrlValidator.detectPlatform("https://www.facebook.com/reel/123456789/"));
        assertEquals(Platform.FACEBOOK,
                UrlValidator.detectPlatform("https://fb.watch/abc123/"));
    }

    @Test
    void detectsYouTube() {
        assertEquals(Platform.YOUTUBE,
                UrlValidator.detectPlatform("https://www.youtube.com/watch?v=abc123"));
        assertEquals(Platform.YOUTUBE,
                UrlValidator.detectPlatform("https://youtu.be/abc123"));
    }

    @Test
    void detectsTikTok() {
        assertEquals(Platform.TIKTOK, UrlValidator.detectPlatform("https://www.tiktok.com/@user/video/123456789"));
        assertEquals(Platform.TIKTOK, UrlValidator.detectPlatform("https://vm.tiktok.com/abc123/"));
    }

    @Test
    void detectsX() {
        assertEquals(Platform.X, UrlValidator.detectPlatform("https://x.com/user/status/123456789"));
        assertEquals(Platform.X, UrlValidator.detectPlatform("https://twitter.com/user/status/123456789"));
    }

    @Test
    void rejectsUnsupportedHost() {
        assertNull(UrlValidator.detectPlatform("https://example.com/video"));
        assertFalse(UrlValidator.isSupportedUrl("https://example.com/video"));
    }
}
