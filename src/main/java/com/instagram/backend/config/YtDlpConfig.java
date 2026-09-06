package com.instagram.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YtDlpConfig {

    @Value("${yt-dlp.path}")
    private String ytDlpPath;

    @Value("${yt-dlp.download-dir:downloads}")
    private String downloadDir;

    @Value("${yt-dlp.ffmpeg-path}")
    private String ffmpegPath;

    public String getYtDlpPath() {
        return ytDlpPath;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }
}