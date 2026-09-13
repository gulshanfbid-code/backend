package com.instagram.backend.dto;

import java.util.List;

public class VideoInfoResponse {
    private String id, title, description, uploader, channel, thumbnail, platform;
    private Integer duration, width, height;
    private List<VideoFormat> formats;
    private List<String> availableQualities;
    public VideoInfoResponse() {}
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getUploader(){return uploader;} public void setUploader(String v){uploader=v;}
    public String getChannel(){return channel;} public void setChannel(String v){channel=v;}
    public String getThumbnail(){return thumbnail;} public void setThumbnail(String v){thumbnail=v;}
    public String getPlatform(){return platform;} public void setPlatform(String v){platform=v;}
    public Integer getDuration(){return duration;} public void setDuration(Integer v){duration=v;}
    public Integer getWidth(){return width;} public void setWidth(Integer v){width=v;}
    public Integer getHeight(){return height;} public void setHeight(Integer v){height=v;}
    public List<VideoFormat> getFormats(){return formats;} public void setFormats(List<VideoFormat> v){formats=v;}
    public List<String> getAvailableQualities(){return availableQualities;} public void setAvailableQualities(List<String> v){availableQualities=v;}
}
