package org.example.customersupportrag.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ObjectStorageService {

    private final MinioClient minioClient;

    private final String bucket;

    public ObjectStorageService(MinioClient minioClient, @Value("${minio.bucket-name}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO bucket error", e);
        }
    }

    public String upload(UUID jobId, MultipartFile file) {
        try {
            ensureBucketExists();

            String objectName = jobId + "/" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return objectName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }
}
