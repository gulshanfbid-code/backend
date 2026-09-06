package com.instagram.backend.service;

import org.springframework.stereotype.Service;

@Service
public class InstagramService {

    private final YtDlpExecutor ytDlpExecutor;

    public InstagramService(YtDlpExecutor ytDlpExecutor) {
        this.ytDlpExecutor = ytDlpExecutor;
    }

    public String getVideoMetadata(String url) {

        try {

            return ytDlpExecutor.execute(url);

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Unable to get Instagram video metadata: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}