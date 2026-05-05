package com.example.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
public class AudioGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AudioGenerationService.class);

    @Value("${python.executable}")
    private String pythonExecutable;

    @Value("${python.script.path}")
    private String pythonScriptPath;

    public String generateAudio(String romaji, Integer vocabId) throws Exception {
        logger.info("Đang sinh audio cho romaji: {}, vocabId: {}", romaji, vocabId);

        // Làm sạch romaji để làm tên file
        String cleanRomaji = romaji.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");

        ProcessBuilder processBuilder = new ProcessBuilder(
                pythonExecutable,
                pythonScriptPath,
                cleanRomaji,
                String.valueOf(vocabId)
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Đọc cả stdout và stderr
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stdout.append(line).append("\n");
                logger.info("Python stdout: {}", line);
            }
        }

        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                stderr.append(line).append("\n");
                logger.error("Python stderr: {}", line);
            }
        }

        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Python script timeout after 30 seconds");
        }

        int exitCode = process.exitValue();
        String output = stdout.toString().trim();
        String errorOutput = stderr.toString().trim();

        logger.info("Python exit code: {}", exitCode);

        if (exitCode != 0) {
            throw new RuntimeException("Python script failed with exit code " + exitCode +
                    ". Error: " + errorOutput + ". Output: " + output);
        }

        // Tìm URL trong output (dòng bắt đầu bằng http)
        String audioUrl = null;
        for (String line : output.split("\n")) {
            line = line.trim();
            if (line.startsWith("http://") || line.startsWith("https://")) {
                audioUrl = line;
                break;
            }
        }

        if (audioUrl == null || audioUrl.isEmpty()) {
            throw new RuntimeException("No valid URL found in Python output. Output: " + output);
        }

        logger.info("Audio generated successfully: {}", audioUrl);
        return audioUrl;
    }
}