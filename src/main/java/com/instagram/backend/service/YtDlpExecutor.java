package com.instagram.backend.service;

import com.instagram.backend.config.YtDlpConfig;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

@Service
public class YtDlpExecutor {
    private final YtDlpConfig config;
    public YtDlpExecutor(YtDlpConfig config) { this.config = config; }

    public String execute(String url) throws IOException {
        return run("--dump-single-json", "--no-playlist", url);
    }

    public File download(String url, String outputPath, String mode, String quality, String audioFormat) throws IOException {
        File dir = new File(config.getDownloadDir()).getAbsoluteFile();
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Unable to create download directory: " + dir);

        String m = normalizeMode(mode);
        String q = normalizeQuality(quality);
        String af = normalizeAudioFormat(audioFormat);

        // Use an ASCII-only unique basename. This avoids Windows/Unicode filename mismatches.
        String base = "media_" + UUID.randomUUID().toString().replace("-", "");
        String template = new File(dir, base + ".%(ext)s").getAbsolutePath();

        CommandLine cmd = new CommandLine(config.getYtDlpPath());
        cmd.addArgument("--no-playlist");
        cmd.addArgument("--windows-filenames");
        cmd.addArgument("--ffmpeg-location");
        cmd.addArgument(config.getFfmpegPath());
        cmd.addArgument("--print");
        cmd.addArgument("after_move:filepath");
        cmd.addArgument("-o");
        cmd.addArgument(template);

        if ("audio".equals(m)) {
            cmd.addArgument("-x");
            cmd.addArgument("--audio-format");
            cmd.addArgument(af);
            cmd.addArgument("--audio-quality");
            cmd.addArgument("0");
        } else {
            cmd.addArgument("-f");
            cmd.addArgument(buildVideoFormat(q));
            cmd.addArgument("--merge-output-format");
            cmd.addArgument("mp4");
        }
        cmd.addArgument(url.trim());
        return executeDownload(cmd, dir, base);
    }

    private String buildVideoFormat(String quality) {
        if ("best".equals(quality)) return "bv*+ba/b";
        int height;
        try { height = Integer.parseInt(quality.toLowerCase().replace("p", "").trim()); }
        catch (NumberFormatException e) { return "bv*+ba/b"; }
        if (height <= 0) return "bv*+ba/b";
        return "bv*[height<=?" + height + "]+ba/b[height<=?" + height + "]/bv*+ba/b";
    }

    private File executeDownload(CommandLine cmd, File dir, String base) throws IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        DefaultExecutor executor = DefaultExecutor.builder().setWorkingDirectory(dir).get();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));
        try {
            executor.execute(cmd);
        } catch (IOException e) {
            String error = stderr.toString(StandardCharsets.UTF_8).trim();
            throw new IOException("yt-dlp download failed" + (error.isEmpty() ? "" : ": " + error), e);
        }

        // First prefer the explicit after_move path, but only accept it if it exists.
        String out = stdout.toString(StandardCharsets.UTF_8).trim();
        if (!out.isEmpty()) {
            String[] lines = out.split("\\R");
            for (int i = lines.length - 1; i >= 0; i--) {
                String candidate = stripQuotes(lines[i].trim());
                if (!candidate.isBlank()) {
                    File f = new File(candidate);
                    if (f.isFile() && f.length() > 0) return f;
                }
            }
        }

        // Reliable fallback: locate the unique file produced with our ASCII prefix.
        try (var stream = Files.list(dir.toPath())) {
            Path found = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(base + "."))
                    .filter(p -> !p.getFileName().toString().endsWith(".part"))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .orElse(null);
            if (found != null && Files.size(found) > 0) return found.toFile();
        }
        throw new IOException("Downloaded file was not found. yt-dlp output: " + out);
    }

    private String stripQuotes(String s) {
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))))
            return s.substring(1, s.length()-1);
        return s;
    }

    private String run(String... args) throws IOException {
        CommandLine cmd = new CommandLine(config.getYtDlpPath());
        for (String arg : args) cmd.addArgument(arg);
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        DefaultExecutor executor = DefaultExecutor.builder().setWorkingDirectory(new File(".").getAbsoluteFile()).get();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));
        try { executor.execute(cmd); }
        catch (IOException e) {
            String err = stderr.toString(StandardCharsets.UTF_8).trim();
            throw new IOException("yt-dlp failed" + (err.isEmpty()?"":": "+err), e);
        }
        return stdout.toString(StandardCharsets.UTF_8);
    }

    private String normalizeMode(String v) { return v == null || v.isBlank() ? "video" : v.trim().toLowerCase(); }
    private String normalizeQuality(String v) { return v == null || v.isBlank() ? "best" : v.trim().toLowerCase(); }
    private String normalizeAudioFormat(String v) { return "m4a".equalsIgnoreCase(v) ? "m4a" : "mp3"; }
}
