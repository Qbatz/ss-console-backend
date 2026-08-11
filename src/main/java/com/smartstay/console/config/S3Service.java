package com.smartstay.console.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.smartstay.console.dto.files.FileData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Paths;

@Service
public class S3Service {

    @Value("${AWS_ACCESS_KEY_ID}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String secretKey;

    public void deleteFile(String fileUrl) {

        AmazonS3 s3Client = AWSConfig.setupS3Client(accessKey, secretKey);

        s3Client.deleteObject(
                getBucketName(fileUrl), getKey(fileUrl));
    }

    public FileData downloadFile(String fileUrl) {

        AmazonS3 s3Client = AWSConfig.setupS3Client(accessKey, secretKey);

        String bucket = getBucketName(fileUrl);
        String key = getKey(fileUrl);

        S3Object s3Object = s3Client.getObject(bucket, key);

        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {

            byte[] content = inputStream.readAllBytes();

            String fileName = Paths.get(key)
                    .getFileName()
                    .toString();

            String contentType = s3Object
                    .getObjectMetadata()
                    .getContentType();

            if (contentType == null) {
                contentType = URLConnection.guessContentTypeFromName(fileName);
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return new FileData(content, contentType, fileName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    private String getBucketName(String fileUrl) {
        String host = URI.create(fileUrl).getHost();
        return host.substring(0, host.indexOf(".s3"));
    }

    private String getKey(String fileUrl) {
        return URI.create(fileUrl).getPath().substring(1);
    }
}
