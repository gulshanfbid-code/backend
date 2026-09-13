# Social Media Downloader Backend

Spring Boot backend for downloading supported public media URLs using yt-dlp and FFmpeg.

## Supported platforms

- Instagram
- Facebook
- YouTube

The backend keeps the existing `POST /api/download` endpoint and automatically detects the platform from the URL.

## Supported downloads

- Video: best available, 1080p, 720p, 480p, 360p when available
- Audio: MP3 and M4A
- FFmpeg-backed media processing
- Existing file download endpoint preserved

Use the service only for content you own or have permission to download and in accordance with the applicable platform terms.

## API

### POST `/api/download`

```json
{
  "url": "https://www.youtube.com/watch?v=...",
  "mode": "video",
  "quality": "1080p",
  "audioFormat": "mp3"
}
```

The same request shape works for supported Instagram and Facebook URLs.

### GET `/api/download/validate?url=...`

Detects whether the URL belongs to Instagram, Facebook, or YouTube.

### GET `/api/download/info?url=...`

Returns yt-dlp metadata for a supported URL.

### GET `/api/download/file?fileName=...`

Downloads a generated file.

## Configuration

Set these environment variables in production:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `CORS_ALLOWED_ORIGINS`
- `YT_DLP_PATH`
- `YT_DLP_FFMPEG_PATH`
- `YT_DLP_DOWNLOAD_DIR`
- `PORT`

For Docker/Railway, the Docker image installs yt-dlp and FFmpeg automatically.


## Supported platforms

- Instagram
- Facebook
- YouTube (including Shorts)
- TikTok
- X / Twitter

The same `POST /api/download` endpoint supports `mode=video` or `mode=audio`.
Use `GET /api/download/info?url=...` to retrieve the video metadata and the video resolutions actually exposed by yt-dlp for that URL.
For video downloads, `quality` can be `best` or a discovered value such as `360p`, `720p`, `1080p`, etc. The downloader selects the best compatible stream at or below the requested height when available.

Use the downloader only for content you own or have permission to download. The backend does not bypass private content, DRM, authentication, or other access controls.
