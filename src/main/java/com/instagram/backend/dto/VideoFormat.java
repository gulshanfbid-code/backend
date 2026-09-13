package com.instagram.backend.dto;

public class VideoFormat {
    private String quality;
    private Integer height;
    private Integer width;
    private String formatId;
    private String ext;
    private String fps;

    public VideoFormat() {}
    public VideoFormat(String quality, Integer height, Integer width, String formatId, String ext, String fps) {
        this.quality=quality; this.height=height; this.width=width; this.formatId=formatId; this.ext=ext; this.fps=fps;
    }
    public String getQuality(){return quality;} public void setQuality(String v){quality=v;}
    public Integer getHeight(){return height;} public void setHeight(Integer v){height=v;}
    public Integer getWidth(){return width;} public void setWidth(Integer v){width=v;}
    public String getFormatId(){return formatId;} public void setFormatId(String v){formatId=v;}
    public String getExt(){return ext;} public void setExt(String v){ext=v;}
    public String getFps(){return fps;} public void setFps(String v){fps=v;}
}
