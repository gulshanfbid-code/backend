package com.instagram.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instagram.backend.dto.VideoFormat;
import com.instagram.backend.dto.VideoInfoResponse;
import com.instagram.backend.util.Platform;
import com.instagram.backend.util.UrlValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoInfoService {
    private final YtDlpService ytDlpService;
    private final ObjectMapper objectMapper;
    public VideoInfoService(YtDlpService ytDlpService, ObjectMapper objectMapper) { this.ytDlpService=ytDlpService; this.objectMapper=objectMapper; }

    public VideoInfoResponse getVideoInfo(String url) {
        Platform platform = UrlValidator.detectPlatform(url);
        if (platform == null) throw new IllegalArgumentException("Unsupported URL. Supported platforms: Instagram, Facebook, YouTube, TikTok and X.");
        try {
            JsonNode root = objectMapper.readTree(ytDlpService.getVideoInfo(url.trim()));
            VideoInfoResponse r = new VideoInfoResponse();
            r.setId(text(root,"id")); r.setTitle(text(root,"title")); r.setDescription(text(root,"description"));
            r.setUploader(text(root,"uploader")); r.setChannel(text(root,"channel")); r.setThumbnail(text(root,"thumbnail"));
            r.setDuration(integer(root,"duration")); r.setWidth(integer(root,"width")); r.setHeight(integer(root,"height"));
            r.setPlatform(platform.getDisplayName());

            Map<Integer,VideoFormat> byHeight = new LinkedHashMap<>();
            JsonNode formats = root.get("formats");
            if (formats != null && formats.isArray()) {
                for (JsonNode f : formats) {
                    Integer h = integer(f,"height");
                    if (h == null || h <= 0) continue;
                    String vcodec = text(f,"vcodec");
                    if (vcodec == null || vcodec.equalsIgnoreCase("none")) continue;
                    Integer w = integer(f,"width");
                    String ext = text(f,"ext");
                    String id = text(f,"format_id");
                    String fps = f.has("fps") && !f.get("fps").isNull() ? f.get("fps").asText() : null;
                    VideoFormat candidate = new VideoFormat(h+"p",h,w,id,ext,fps);
                    VideoFormat existing = byHeight.get(h);
                    // Keep the first format for a resolution; yt-dlp will choose compatible audio when downloading.
                    if (existing == null) byHeight.put(h,candidate);
                }
            }
            List<VideoFormat> list = new ArrayList<>(byHeight.values());
            list.sort(Comparator.comparing(VideoFormat::getHeight));
            r.setFormats(list);
            r.setAvailableQualities(list.stream().map(VideoFormat::getQuality).toList());
            return r;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse video metadata: " + e.getMessage(), e);
        }
    }
    private String text(JsonNode n,String k){ return n.has(k)&&!n.get(k).isNull()?n.get(k).asText():null; }
    private Integer integer(JsonNode n,String k){ return n.has(k)&&n.get(k).isNumber()?n.get(k).asInt():null; }
}
