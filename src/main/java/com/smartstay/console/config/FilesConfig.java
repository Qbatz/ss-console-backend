package com.smartstay.console.config;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FilesConfig {

    public static File convertMultipartToFileNew(MultipartFile file)  {
        File convFile = null;
        try {
            convFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return convFile;
    }

    public static File base64ToImage(String customerId, String base64) {
        if (base64 == null) {
            return null;
        }
        String[] parts = base64.split(",");

        String imageString = "";
        if (parts.length > 1) {
            imageString = parts[1];
        }
        else {
            imageString = base64;
        }

        byte[] imageBytes = Base64.getDecoder().decode(imageString);

        try {
            Path tempFile = Files.createTempFile(customerId, ".jpg");
            Files.write(tempFile, imageBytes);
            return tempFile.toFile();
        } catch (IOException e) {
            return null;
        }
    }

    public static File writePdf(byte[] pdfBytes, String fileName) {
        try {
            Path tempFile = Files.createTempFile(fileName, ".pdf");
            Files.write(tempFile, pdfBytes);
            return tempFile.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write PDF to temp file", e);
        }
    }
}
