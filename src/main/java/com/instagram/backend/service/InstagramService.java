package com.instagram.backend.service;

import org.springframework.stereotype.Service;

@Service
public class InstagramService {

    private final YtDlpService ytDlpService;

    public InstagramService(YtDlpService ytDlpService) {
        this.ytDlpService = ytDlpService;
    }

    public String getVideoMetadata(String url) {
        return ytDlpService.getVideoInfo(url);
    }
}
