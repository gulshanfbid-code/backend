package com.instagram.backend.controller;

import com.instagram.backend.dto.DownloadRequest;
import com.instagram.backend.dto.DownloadResponse;
import com.instagram.backend.dto.VideoInfoResponse;
import com.instagram.backend.service.DownloadService;
import com.instagram.backend.service.VideoInfoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/download")
public class DownloadController {

    private final DownloadService downloadService;
    private final VideoInfoService videoInfoService;

    public DownloadController(DownloadService downloadService,
                              VideoInfoService videoInfoService) {
        this.downloadService = downloadService;
        this.videoInfoService = videoInfoService;
    }

    @PostMapping
    public DownloadResponse download(@RequestBody DownloadRequest request) {
        if (request == null || request.getUrl() == null || request.getUrl().isBlank()) {
            return new DownloadResponse("error", "URL is required", null);
        }

        return downloadService.downloadMedia(
                request.getUrl(),
                request.getMode(),
                request.getQuality(),
                request.getAudioFormat()
        );
    }

    @GetMapping("/validate")
    public DownloadResponse validate(@RequestParam String url) {
        return downloadService.validateUrl(url);
    }

    @GetMapping("/info")
    public VideoInfoResponse info(@RequestParam String url) {
        return videoInfoService.getVideoInfo(url);
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
        Resource resource = downloadService.getDownloadedFile(fileName);
        String filename = resource.getFilename();

        // Spring 7 / Spring Boot 4 rejects wildcard media types (video/*, audio/*)
        // in ResponseEntity.contentType(). Resolve a concrete MIME type instead.
        MediaType mediaType = filename == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaTypeFactory.getMediaType(filename)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
                        java.net.URLEncoder.encode(filename == null ? "download" : filename, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"))
                .body(resource);
    }
}
