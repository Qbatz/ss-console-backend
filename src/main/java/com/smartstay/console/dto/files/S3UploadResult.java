package com.smartstay.console.dto.files;

public record S3UploadResult(String bucket,
                             String key,
                             String url) {
}
