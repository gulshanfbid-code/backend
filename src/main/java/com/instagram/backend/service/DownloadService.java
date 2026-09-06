package com.instagram.backend.service;

import com.instagram.backend.config.YtDlpConfig;
import com.instagram.backend.dto.DownloadData;
import com.instagram.backend.dto.DownloadResponse;
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

    public DownloadService(
            YtDlpService ytDlpService,
            YtDlpConfig ytDlpConfig
    ) {
        this.ytDlpService = ytDlpService;
        this.ytDlpConfig = ytDlpConfig;
    }

    public DownloadResponse validateUrl(String url) {

        if (!UrlValidator.isValidInstagramUrl(url)) {
            return new DownloadResponse(
                    "error",
                    "Invalid Instagram URL",
                    null
            );
        }

        return new DownloadResponse(
                "success",
                "Valid Instagram URL",
                null
        );
    }

    public DownloadResponse downloadMedia(
            String url,
            String mode,
            String quality,
            String audioFormat
    ) {

        if (!UrlValidator.isValidInstagramUrl(url)) {
            return new DownloadResponse(
                    "error",
                    "Invalid Instagram URL",
                    null
            );
        }

        String normalizedMode =
                mode == null || mode.isBlank()
                        ? "video"
                        : mode.trim().toLowerCase();

        String normalizedQuality =
                quality == null || quality.isBlank()
                        ? "best"
                        : quality.trim().toLowerCase();

        String normalizedAudioFormat =
                audioFormat == null || audioFormat.isBlank()
                        ? "mp3"
                        : audioFormat.trim().toLowerCase();

        try {

            File downloadedFile =
                    ytDlpService.downloadMedia(
                            url,
                            normalizedMode,
                            normalizedQuality,
                            normalizedAudioFormat
                    );

            if (downloadedFile == null ||
                    !downloadedFile.exists()) {

                return new DownloadResponse(
                        "error",
                        "Media download failed",
                        null
                );
            }

            String fileName =
                    downloadedFile.getName();

            String filePath =
                    downloadedFile.getAbsolutePath();

            /*
             * Encode the filename before putting it
             * inside the URL query parameter.
             *
             * Example:
             *
             * Original:
             * Video by demicstory [Dc3wfDUlmvT].mp4
             *
             * Encoded:
             * Video+by+demicstory+%5BDc3wfDUlmvT%5D.mp4
             */
            String encodedFileName =
                    URLEncoder.encode(
                            fileName,
                            StandardCharsets.UTF_8
                    );

            String downloadUrl =
                    "/api/download/file?fileName=" +
                            encodedFileName;

            DownloadData data =
                    new DownloadData(
                            fileName,
                            filePath,
                            downloadUrl,
                            normalizedMode,
                            normalizedQuality,
                            normalizedAudioFormat
                    );

            return new DownloadResponse(
                    "success",
                    "Media downloaded successfully",
                    data
            );

        } catch (Exception exception) {

            return new DownloadResponse(
                    "error",
                    exception.getMessage() == null
                            ? "Media download failed"
                            : exception.getMessage(),
                    null
            );
        }
    }

    public Resource getDownloadedFile(
            String fileName
    ) {

        try {

            Path downloadDirectory =
                    Paths.get(
                                    ytDlpConfig.getDownloadDir()
                            )
                            .toAbsolutePath()
                            .normalize();

            Path requestedFile =
                    downloadDirectory
                            .resolve(fileName)
                            .normalize();

            /*
             * Security check:
             * Prevent ../ path traversal.
             */
            if (!requestedFile.startsWith(
                    downloadDirectory
            )) {

                throw new RuntimeException(
                        "Invalid file path"
                );
            }

            Resource resource =
                    new FileSystemResource(
                            requestedFile
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new RuntimeException(
                        "File not found: " + fileName
                );
            }

            return resource;

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Unable to download file: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}