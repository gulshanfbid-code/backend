package com.instagram.backend.service;

import com.instagram.backend.config.YtDlpConfig;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class YtDlpService {

    private final YtDlpExecutor executor;
    private final YtDlpConfig config;

    public YtDlpService(YtDlpExecutor executor, YtDlpConfig config) {
        this.executor = executor;
        this.config = config;
    }

    public String getVideoInfo(String url) {
        try {
            return executor.execute(url);
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to get video information: " + exception.getMessage(),
                    exception
            );
        }
    }

    public File downloadMedia(String url, String mode, String quality, String audioFormat) {
        try {
            File downloadDirectory = new File(config.getDownloadDir());
            if (!downloadDirectory.exists()) {
                downloadDirectory.mkdirs();
            }

            String outputPath =
                    config.getDownloadDir() + "/%(title)s [%(id)s].%(ext)s";

            return executor.download(url, outputPath, mode, quality, audioFormat);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Media download failed: " + exception.getMessage(),
                    exception
            );
        }
    }

    public File downloadVideo(String url) {
        return downloadMedia(url, "video", "best", "mp3");
    }
}
