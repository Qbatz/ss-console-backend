package com.smartstay.console.config;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
}
