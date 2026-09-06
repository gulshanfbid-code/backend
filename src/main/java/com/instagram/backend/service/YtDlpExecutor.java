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

@Service
public class YtDlpExecutor {

    private final YtDlpConfig config;

    public YtDlpExecutor(YtDlpConfig config) {
        this.config = config;
    }

    public String execute(String url) throws IOException {
        return run(
                "--dump-single-json",
                url
        );
    }

    public File download(String url, String outputPath,
                          String mode, String quality, String audioFormat) throws IOException {

        File downloadDirectory = new File(config.getDownloadDir());
        if (!downloadDirectory.exists() && !downloadDirectory.mkdirs()) {
            throw new IOException("Unable to create download directory");
        }

        String normalizedMode = normalizeMode(mode);
        String normalizedQuality = normalizeQuality(quality);
        String normalizedAudioFormat = normalizeAudioFormat(audioFormat);

        CommandLine commandLine = new CommandLine(config.getYtDlpPath());

        commandLine.addArgument("--no-playlist");
        commandLine.addArgument("--ffmpeg-location");
        commandLine.addArgument(config.getFfmpegPath());
        commandLine.addArgument("--print");
        commandLine.addArgument("after_move:filepath");

        if ("audio".equals(normalizedMode)) {
            commandLine.addArgument("-x");
            commandLine.addArgument("--audio-format");
            commandLine.addArgument(normalizedAudioFormat);
            commandLine.addArgument("--audio-quality");
            commandLine.addArgument("0");
        } else {
            commandLine.addArgument("-f");
            commandLine.addArgument(buildVideoFormat(normalizedQuality));
            commandLine.addArgument("--merge-output-format");
            commandLine.addArgument("mp4");
        }

        commandLine.addArgument("-o");
        commandLine.addArgument(outputPath);
        commandLine.addArgument(url);

        return executeDownload(commandLine);
    }

    private String buildVideoFormat(String quality) {
        if ("best".equals(quality)) {
            return "bv*+ba/b";
        }

        int height = Integer.parseInt(quality.replace("p", ""));
        return "bv*[height<=?" + height + "]+ba/b[height<=?" + height + "]/bv*+ba/b";
    }

    private File executeDownload(CommandLine commandLine) throws IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        DefaultExecutor executor = DefaultExecutor.builder().get();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));

        try {
            executor.execute(commandLine);
        } catch (IOException exception) {
            String error = stderr.toString(StandardCharsets.UTF_8).trim();
            throw new IOException(
                    "yt-dlp download failed" + (error.isEmpty() ? "" : ": " + error),
                    exception
            );
        }

        String output = stdout.toString(StandardCharsets.UTF_8).trim();
        if (output.isEmpty()) {
            throw new IOException("yt-dlp did not return the downloaded file path");
        }

        String[] lines = output.split("\\R");
        String actualPath = lines[lines.length - 1].trim();
        File downloadedFile = new File(actualPath);

        if (!downloadedFile.exists() || !downloadedFile.isFile()) {
            throw new IOException("Downloaded file was not found: " + actualPath);
        }

        return downloadedFile;
    }

    private String run(String... args) throws IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine commandLine = new CommandLine(config.getYtDlpPath());
        for (String arg : args) {
            commandLine.addArgument(arg);
        }

        DefaultExecutor executor = DefaultExecutor.builder().get();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));

        try {
            executor.execute(commandLine);
            return stdout.toString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            String error = stderr.toString(StandardCharsets.UTF_8).trim();
            throw new IOException(
                    "yt-dlp failed" + (error.isEmpty() ? "" : ": " + error),
                    exception
            );
        }
    }

    private String normalizeMode(String mode) throws IOException {
        if (mode == null || mode.isBlank()) return "video";
        String value = mode.trim().toLowerCase();
        if (!value.equals("video") && !value.equals("audio")) {
            throw new IOException("Invalid mode. Use video or audio.");
        }
        return value;
    }

    private String normalizeQuality(String quality) throws IOException {
        if (quality == null || quality.isBlank() || quality.equalsIgnoreCase("best")) {
            return "best";
        }

        String value = quality.trim().toLowerCase();
        if (!value.matches("(1080|720|480|360)p")) {
            throw new IOException("Invalid video quality.");
        }
        return value;
    }

    private String normalizeAudioFormat(String audioFormat) throws IOException {
        if (audioFormat == null || audioFormat.isBlank()) return "mp3";

        String value = audioFormat.trim().toLowerCase();
        if (!value.equals("mp3") && !value.equals("m4a")) {
            throw new IOException("Invalid audio format. Use mp3 or m4a.");
        }
        return value;
    }
}
