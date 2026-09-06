# InstaSave Backend

Spring Boot backend for InstaSave Instagram media downloads.

## Supported downloads

- Video: Best available, 1080p, 720p, 480p, 360p (when available)
- Audio: MP3 and M4A
- FFmpeg-backed video/audio processing
- Existing file download endpoint preserved

## API

POST `/api/download`

```json
{
  "url": "https://www.instagram.com/reel/...",
  "mode": "video",
  "quality": "1080p",
  "audioFormat": "mp3"
}
```

For audio:

```json
{
  "url": "https://www.instagram.com/reel/...",
  "mode": "audio",
  "audioFormat": "mp3"
}
```

The backend automatically falls back to the best compatible available stream when an exact resolution is not exposed by the source.

GET `/api/download/file/{fileName}` downloads the generated file.

## Configuration

Keep your existing `yt-dlp.path`, `yt-dlp.ffmpeg-path`, and `yt-dlp.download-dir` settings.

For deployment, set:

`app.cors.allowed-origins=https://your-frontend-domain`

Multiple origins can be comma-separated.
