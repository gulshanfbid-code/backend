package com.instagram.backend.controller;

import com.instagram.backend.dto.DownloadRequest;
import com.instagram.backend.dto.DownloadResponse;
import com.instagram.backend.service.DownloadService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/download")
public class DownloadController {

    private final DownloadService downloadService;

    public DownloadController(
            DownloadService downloadService
    ) {
        this.downloadService = downloadService;
    }

    @PostMapping
    public DownloadResponse download(
            @RequestBody DownloadRequest request
    ) {

        return downloadService.downloadMedia(
                request.getUrl(),
                request.getMode(),
                request.getQuality(),
                request.getAudioFormat()
        );
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String fileName
    ) {

        Resource resource =
                downloadService.getDownloadedFile(
                        fileName
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }
}