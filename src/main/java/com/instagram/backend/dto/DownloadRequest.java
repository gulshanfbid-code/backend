package com.instagram.backend.dto;

public class DownloadRequest {

    private String url;
    private String mode = "video";
    private String quality = "best";
    private String audioFormat = "mp3";

    public DownloadRequest() {
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getAudioFormat() { return audioFormat; }
    public void setAudioFormat(String audioFormat) { this.audioFormat = audioFormat; }
}
