package com.smartstay.console.dto.files;

public record FileData(byte[] content,
                       String contentType,
                       String fileName) {
}
