package com.smartstay.console.config;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

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

    private String getBucketName(String fileUrl) {
        String host = URI.create(fileUrl).getHost();
        return host.substring(0, host.indexOf(".s3"));
    }

    private String getKey(String fileUrl) {
        return URI.create(fileUrl).getPath().substring(1);
    }
}
