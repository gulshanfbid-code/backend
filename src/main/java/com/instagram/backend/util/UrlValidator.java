package com.instagram.backend.util;

import java.net.URI;
import java.util.Locale;

public final class UrlValidator {
    private UrlValidator() {}

    public static boolean isSupportedUrl(String url) { return detectPlatform(url) != null; }

    public static Platform detectPlatform(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) return null;
            scheme = scheme.toLowerCase(Locale.ROOT);
            host = host.toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) return null;

            if (host.equals("instagram.com") || host.endsWith(".instagram.com")) return Platform.INSTAGRAM;
            if (host.equals("facebook.com") || host.endsWith(".facebook.com") || host.equals("fb.watch")) return Platform.FACEBOOK;
            if (host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtu.be")) return Platform.YOUTUBE;
            if (host.equals("tiktok.com") || host.endsWith(".tiktok.com")) return Platform.TIKTOK;
            if (host.equals("x.com") || host.endsWith(".x.com") || host.equals("twitter.com") || host.endsWith(".twitter.com")) return Platform.X;
            return null;
        } catch (Exception e) { return null; }
    }

    public static boolean isValidInstagramUrl(String url) { return detectPlatform(url) == Platform.INSTAGRAM; }
    public static boolean isValidFacebookUrl(String url) { return detectPlatform(url) == Platform.FACEBOOK; }
    public static boolean isValidYouTubeUrl(String url) { return detectPlatform(url) == Platform.YOUTUBE; }
    public static boolean isValidTikTokUrl(String url) { return detectPlatform(url) == Platform.TIKTOK; }
    public static boolean isValidXUrl(String url) { return detectPlatform(url) == Platform.X; }
}
