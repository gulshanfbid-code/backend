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

    /**
     * Starts the media download.
     *
     * POST /api/download
     */
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

    /**
     * Returns the downloaded file.
     *
     * GET /api/download/file?fileName=...
     */
    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String fileName
    ) {

        Resource resource =
                downloadService.getDownloadedFile(
                        fileName
                );

        String filename =
                resource.getFilename();

        /*
         * Default MIME type.
         */
        MediaType mediaType =
                MediaType.APPLICATION_OCTET_STREAM;

        /*
         * Detect the actual media type from
         * the downloaded file extension.
         */
        if (filename != null) {

            String lowerCaseFilename =
                    filename.toLowerCase();

            if (lowerCaseFilename.endsWith(".mp4")) {

                mediaType =
                        MediaType.parseMediaType(
                                "video/mp4"
                        );

            } else if (
                    lowerCaseFilename.endsWith(".mp3")
            ) {

                mediaType =
                        MediaType.parseMediaType(
                                "audio/mpeg"
                        );

            } else if (
                    lowerCaseFilename.endsWith(".m4a")
            ) {

                mediaType =
                        MediaType.parseMediaType(
                                "audio/mp4"
                        );
            }
        }

        return ResponseEntity.ok()

                /*
                 * Tell the browser the correct
                 * media type.
                 */
                .contentType(mediaType)

                /*
                 * Force the browser to download
                 * the file using its real filename.
                 */
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                filename +
                                "\""
                )

                /*
                 * Send the actual downloaded file.
                 */
                .body(resource);
    }
}