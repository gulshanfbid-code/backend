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

    public File download(
            String url,
            String outputPath,
            String mode,
            String quality,
            String audioFormat
    ) throws IOException {

        File downloadDirectory =
                new File(config.getDownloadDir())
                        .getAbsoluteFile();

        if (!downloadDirectory.exists()
                && !downloadDirectory.mkdirs()) {

            throw new IOException(
                    "Unable to create download directory: "
                            + downloadDirectory
            );
        }

        String normalizedMode =
                normalizeMode(mode);

        String normalizedQuality =
                normalizeQuality(quality);

        String normalizedAudioFormat =
                normalizeAudioFormat(audioFormat);

        CommandLine commandLine =
                new CommandLine(
                        config.getYtDlpPath()
                );

        /*
         * ========================================
         * GENERAL OPTIONS
         * ========================================
         */

        // Do not download playlists.
        commandLine.addArgument("--no-playlist");

        /*
         * Tell yt-dlp where FFmpeg is located.
         */
        commandLine.addArgument("--ffmpeg-location");
        commandLine.addArgument(
                config.getFfmpegPath()
        );

        /*
         * Print the final processed file path.
         *
         * This is important because we use this
         * output to find the downloaded file.
         */
        commandLine.addArgument("--print");
        commandLine.addArgument(
                "after_move:filepath"
        );

        /*
         * ========================================
         * AUDIO DOWNLOAD
         * ========================================
         */

        if ("audio".equals(normalizedMode)) {

            /*
             * Extract audio.
             */
            commandLine.addArgument("-x");

            /*
             * Requested audio format:
             * mp3 or m4a
             */
            commandLine.addArgument(
                    "--audio-format"
            );

            commandLine.addArgument(
                    normalizedAudioFormat
            );

            /*
             * Best audio quality.
             */
            commandLine.addArgument(
                    "--audio-quality"
            );

            commandLine.addArgument("0");
        }

        /*
         * ========================================
         * VIDEO DOWNLOAD
         * ========================================
         */

        else {

            /*
             * Select requested video quality.
             */
            commandLine.addArgument("-f");

            commandLine.addArgument(
                    buildVideoFormat(
                            normalizedQuality
                    )
            );

            /*
             * When video and audio are separate,
             * merge them into MP4.
             *
             * Keep this behavior because this was
             * already working before the domain
             * connection.
             */
            commandLine.addArgument(
                    "--merge-output-format"
            );

            commandLine.addArgument("mp4");

            /*
             * IMPORTANT:
             *
             * We intentionally DO NOT use:
             *
             * --recode-video mp4
             *
             * because it forces another encoding step
             * and was not part of the original working
             * configuration.
             */
        }

        /*
         * ========================================
         * OUTPUT FILE
         * ========================================
         *
         * yt-dlp creates the actual extension using
         * %(ext)s.
         */
        String fileNameTemplate =
                "%(title)s [%(id)s].%(ext)s";

        commandLine.addArgument("-o");

        commandLine.addArgument(
                fileNameTemplate
        );

        /*
         * Instagram URL.
         */
        commandLine.addArgument(url);

        return executeDownload(
                commandLine,
                downloadDirectory
        );
    }

    /**
     * Builds the yt-dlp video format selector.
     */
    private String buildVideoFormat(
            String quality
    ) {

        /*
         * Best available quality.
         */
        if ("best".equals(quality)) {

            return "bv*+ba/b";
        }

        /*
         * Convert:
         *
         * 1080p -> 1080
         * 720p  -> 720
         * 480p  -> 480
         * 360p  -> 360
         */
        int height =
                Integer.parseInt(
                        quality.replace(
                                "p",
                                ""
                        )
                );

        /*
         * Try to select video up to the requested
         * height and combine it with the best audio.
         *
         * If unavailable, fall back to the best
         * available format.
         */
        return "bv*[height<=?"
                + height
                + "]+ba/b[height<=?"
                + height
                + "]/bv*+ba/b";
    }

    /**
     * Executes the yt-dlp download command.
     */
    private File executeDownload(
            CommandLine commandLine,
            File downloadDirectory
    ) throws IOException {

        ByteArrayOutputStream stdout =
                new ByteArrayOutputStream();

        ByteArrayOutputStream stderr =
                new ByteArrayOutputStream();

        DefaultExecutor executor =
                DefaultExecutor.builder()
                        .setWorkingDirectory(
                                downloadDirectory
                        )
                        .get();

        executor.setStreamHandler(
                new PumpStreamHandler(
                        stdout,
                        stderr
                )
        );

        /*
         * ========================================
         * DEBUG LOGGING
         * ========================================
         */

        System.out.println(
                "========================================"
        );

        System.out.println(
                "YT-DLP DOWNLOAD DIRECTORY:"
        );

        System.out.println(
                downloadDirectory
                        .getAbsolutePath()
        );

        System.out.println(
                "YT-DLP COMMAND:"
        );

        System.out.println(
                commandLine
        );

        System.out.println(
                "========================================"
        );

        /*
         * Execute yt-dlp.
         */
        try {

            executor.execute(commandLine);

        } catch (IOException exception) {

            String error =
                    stderr.toString(
                            StandardCharsets.UTF_8
                    ).trim();

            throw new IOException(
                    "yt-dlp download failed"
                            +
                            (
                                    error.isEmpty()
                                            ? ""
                                            : ": " + error
                            ),
                    exception
            );
        }

        /*
         * Read yt-dlp output.
         */
        String output =
                stdout.toString(
                        StandardCharsets.UTF_8
                ).trim();

        System.out.println(
                "========================================"
        );

        System.out.println(
                "YT-DLP RAW OUTPUT:"
        );

        System.out.println(output);

        System.out.println(
                "========================================"
        );

        if (output.isEmpty()) {

            throw new IOException(
                    "yt-dlp did not return the downloaded file path"
            );
        }

        /*
         * ========================================
         * GET FINAL FILE PATH
         * ========================================
         *
         * yt-dlp can print multiple lines.
         *
         * The last line should contain the final
         * processed file path because we use:
         *
         * after_move:filepath
         */
        String[] lines =
                output.split("\\R");

        String actualPath =
                lines[lines.length - 1]
                        .trim();

        System.out.println(
                "YT-DLP SELECTED PATH:"
        );

        System.out.println(actualPath);

        /*
         * ========================================
         * FIX QUOTED FILENAME
         * ========================================
         *
         * IMPORTANT:
         *
         * yt-dlp may return:
         *
         * /app/downloads/"Video [123].mp4"
         *
         * instead of:
         *
         * /app/downloads/Video [123].mp4
         *
         * The quotes around the filename are NOT
         * supposed to be part of the actual filename.
         *
         * The old code only removed quotes when the
         * ENTIRE path started and ended with quotes.
         *
         * That does NOT work for:
         *
         * /app/downloads/"Video.mp4"
         *
         * So we remove quotes specifically from the
         * final filename component.
         */

        int lastSeparator =
                Math.max(
                        actualPath.lastIndexOf('/'),
                        actualPath.lastIndexOf('\\')
                );

        if (lastSeparator >= 0
                && lastSeparator < actualPath.length() - 1) {

            String directoryPart =
                    actualPath.substring(
                            0,
                            lastSeparator + 1
                    );

            String filenamePart =
                    actualPath.substring(
                            lastSeparator + 1
                    );

            /*
             * Remove double quotes around filename.
             *
             * Example:
             *
             * "Video.mp4"
             *
             * becomes:
             *
             * Video.mp4
             */
            if (filenamePart.length() >= 2
                    && filenamePart.startsWith("\"")
                    && filenamePart.endsWith("\"")) {

                filenamePart =
                        filenamePart.substring(
                                1,
                                filenamePart.length() - 1
                        );
            }

            /*
             * Remove single quotes around filename.
             */
            if (filenamePart.length() >= 2
                    && filenamePart.startsWith("'")
                    && filenamePart.endsWith("'")) {

                filenamePart =
                        filenamePart.substring(
                                1,
                                filenamePart.length() - 1
                        );
            }

            /*
             * Rebuild the clean path.
             */
            actualPath =
                    directoryPart + filenamePart;
        }

        /*
         * Remove any remaining whitespace.
         */
        actualPath =
                actualPath.trim();

        System.out.println(
                "YT-DLP CLEANED PATH:"
        );

        System.out.println(actualPath);

        /*
         * ========================================
         * CREATE FILE OBJECT
         * ========================================
         */

        File downloadedFile =
                new File(actualPath);

        /*
         * If yt-dlp returned a relative path,
         * resolve it against the download directory.
         */
        if (!downloadedFile.isAbsolute()) {

            downloadedFile =
                    new File(
                            downloadDirectory,
                            actualPath
                    );
        }

        /*
         * Resolve canonical path.
         */
        downloadedFile =
                downloadedFile.getCanonicalFile();

        /*
         * ========================================
         * FINAL DEBUG INFORMATION
         * ========================================
         */

        System.out.println(
                "ACTUAL DOWNLOADED FILE:"
        );

        System.out.println(
                downloadedFile
                        .getAbsolutePath()
        );

        System.out.println(
                "FILE NAME:"
        );

        System.out.println(
                downloadedFile.getName()
        );

        System.out.println(
                "FILE EXISTS:"
        );

        System.out.println(
                downloadedFile.exists()
        );

        System.out.println(
                "FILE SIZE:"
        );

        System.out.println(
                downloadedFile.length()
                        + " bytes"
        );

        System.out.println(
                "========================================"
        );

        /*
         * ========================================
         * VERIFY FILE
         * ========================================
         */

        if (!downloadedFile.exists()
                || !downloadedFile.isFile()) {

            throw new IOException(
                    "Downloaded file was not found: "
                            + downloadedFile
            );
        }

        /*
         * Prevent an empty file from being returned.
         */
        if (downloadedFile.length() == 0) {

            throw new IOException(
                    "Downloaded file is empty: "
                            + downloadedFile
            );
        }

        return downloadedFile;
    }

    /**
     * Executes yt-dlp commands that return text.
     */
    private String run(
            String... args
    ) throws IOException {

        ByteArrayOutputStream stdout =
                new ByteArrayOutputStream();

        ByteArrayOutputStream stderr =
                new ByteArrayOutputStream();

        CommandLine commandLine =
                new CommandLine(
                        config.getYtDlpPath()
                );

        for (String arg : args) {

            commandLine.addArgument(arg);
        }

        DefaultExecutor executor =
                DefaultExecutor.builder()
                        .get();

        executor.setStreamHandler(
                new PumpStreamHandler(
                        stdout,
                        stderr
                )
        );

        try {

            executor.execute(commandLine);

            return stdout.toString(
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {

            String error =
                    stderr.toString(
                            StandardCharsets.UTF_8
                    ).trim();

            throw new IOException(
                    "yt-dlp failed"
                            +
                            (
                                    error.isEmpty()
                                            ? ""
                                            : ": " + error
                            ),
                    exception
            );
        }
    }

    /**
     * Normalize download mode.
     */
    private String normalizeMode(
            String mode
    ) throws IOException {

        if (mode == null
                || mode.isBlank()) {

            return "video";
        }

        String value =
                mode.trim().toLowerCase();

        if (!value.equals("video")
                && !value.equals("audio")) {

            throw new IOException(
                    "Invalid mode. Use video or audio."
            );
        }

        return value;
    }

    /**
     * Normalize video quality.
     */
    private String normalizeQuality(
            String quality
    ) throws IOException {

        if (quality == null
                || quality.isBlank()
                || quality.equalsIgnoreCase("best")) {

            return "best";
        }

        String value =
                quality.trim().toLowerCase();

        if (!value.matches(
                "(1080|720|480|360)p"
        )) {

            throw new IOException(
                    "Invalid video quality."
            );
        }

        return value;
    }

    /**
     * Normalize audio format.
     */
    private String normalizeAudioFormat(
            String audioFormat
    ) throws IOException {

        if (audioFormat == null
                || audioFormat.isBlank()) {

            return "mp3";
        }

        String value =
                audioFormat.trim().toLowerCase();

        if (!value.equals("mp3")
                && !value.equals("m4a")) {

            throw new IOException(
                    "Invalid audio format. Use mp3 or m4a."
            );
        }

        return value;
    }
}