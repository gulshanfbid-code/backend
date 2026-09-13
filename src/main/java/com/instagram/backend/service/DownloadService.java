package com.instagram.backend.service;

import com.instagram.backend.config.YtDlpConfig;
import com.instagram.backend.dto.DownloadData;
import com.instagram.backend.dto.DownloadResponse;
import com.instagram.backend.util.Platform;
import com.instagram.backend.util.UrlValidator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DownloadService {

    private final YtDlpService ytDlpService;
    private final YtDlpConfig ytDlpConfig;
    private final PlatformDetector platformDetector;

    public DownloadService(YtDlpService ytDlpService,
                           YtDlpConfig ytDlpConfig,
                           PlatformDetector platformDetector) {
        this.ytDlpService = ytDlpService;
        this.ytDlpConfig = ytDlpConfig;
        this.platformDetector = platformDetector;
    }

    public DownloadResponse validateUrl(String url) {
        Platform platform = platformDetector.detect(url);

        if (platform == null) {
            return new DownloadResponse(
                    "error",
                    "Unsupported URL. Supported platforms: Instagram, Facebook, YouTube, TikTok and X.",
                    null
            );
        }

        return new DownloadResponse(
                "success",
                "Valid " + platform.getDisplayName() + " URL",
                platform.getDisplayName()
        );
    }

    public DownloadResponse downloadMedia(String url,
                                           String mode,
                                           String quality,
                                           String audioFormat) {

        Platform platform = platformDetector.detect(url);

        if (platform == null) {
            return new DownloadResponse(
                    "error",
                    "Unsupported URL. Supported platforms: Instagram, Facebook, YouTube, TikTok and X.",
                    null
            );
        }

        String normalizedMode = normalizeOrDefault(mode, "video");
        String normalizedQuality = normalizeOrDefault(quality, "best");
        String normalizedAudioFormat = normalizeOrDefault(audioFormat, "mp3");

        try {
            File downloadedFile = ytDlpService.downloadMedia(
                    url.trim(),
                    normalizedMode,
                    normalizedQuality,
                    normalizedAudioFormat
            );

            if (downloadedFile == null || !downloadedFile.exists() || downloadedFile.length() == 0) {
                return new DownloadResponse("error", "Media download failed", null);
            }

            String fileName = downloadedFile.getName();
            String filePath = downloadedFile.getAbsolutePath();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            String downloadUrl = "/api/download/file?fileName=" + encodedFileName;

            DownloadData data = new DownloadData(
                    fileName,
                    filePath,
                    downloadUrl,
                    normalizedMode,
                    normalizedQuality,
                    normalizedAudioFormat
            );
            data.setPlatform(platform.getDisplayName());

            return new DownloadResponse(
                    "success",
                    platform.getDisplayName() + " media downloaded successfully",
                    data
            );
        } catch (Exception exception) {
            String message = exception.getMessage();
            return new DownloadResponse(
                    "error",
                    message == null || message.isBlank() ? "Media download failed" : message,
                    null
            );
        }
    }

    public Resource getDownloadedFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new RuntimeException("File name is required");
        }

        try {
            Path downloadDirectory = Paths.get(ytDlpConfig.getDownloadDir())
                    .toAbsolutePath()
                    .normalize();

            Path requestedFile = downloadDirectory.resolve(fileName).normalize();

            if (!requestedFile.startsWith(downloadDirectory)) {
                throw new RuntimeException("Invalid file path");
            }

            Resource resource = new FileSystemResource(requestedFile);
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found: " + fileName);
            }

            return resource;
        } catch (Exception exception) {
            throw new RuntimeException("Unable to download file: " + exception.getMessage(), exception);
        }
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim().toLowerCase();
    }
}
