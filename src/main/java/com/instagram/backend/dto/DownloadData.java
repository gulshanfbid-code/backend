package com.instagram.backend.dto;

public class DownloadData {

    private String fileName;
    private String filePath;
    private String downloadUrl;
    private String mode;
    private String quality;
    private String audioFormat;

    public DownloadData() {
    }

    public DownloadData(String fileName, String filePath, String downloadUrl,
                         String mode, String quality, String audioFormat) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.downloadUrl = downloadUrl;
        this.mode = mode;
        this.quality = quality;
        this.audioFormat = audioFormat;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getAudioFormat() { return audioFormat; }
    public void setAudioFormat(String audioFormat) { this.audioFormat = audioFormat; }
}
