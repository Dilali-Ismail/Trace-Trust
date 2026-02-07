package org.usermanagement.traceandtrust.service;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.usermanagement.traceandtrust.exception.BusinessException;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * Uploads a file to S3 and returns the public URL.
     */
    public String uploadFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            S3Resource s3Resource = s3Template.upload(bucketName, fileName, file.getInputStream());
            log.info("File uploaded successfully to S3: {}", fileName);
            
            // Standard S3 public URL format for eu-north-1 and others:
            // https://bucket-name.s3.region.amazonaws.com/file-name
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
            
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw new BusinessException("Failed to upload image to S3: " + e.getMessage());
        }
    }

    /**
     * Deletes a file from S3 given its URL.
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        
        try {
            // Extract file name from URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            s3Template.deleteObject(bucketName, fileName);
            log.info("File deleted from S3: {}", fileName);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", fileUrl, e);
        }
    }
}
