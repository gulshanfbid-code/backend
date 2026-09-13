package com.instagram.backend.util;

public enum Platform {
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook"),
    YOUTUBE("YouTube"),
    TIKTOK("TikTok"),
    X("X");

    private final String displayName;

    Platform(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
