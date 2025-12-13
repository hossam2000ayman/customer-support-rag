package org.example.customersupportrag.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Bean
    public MinioClient minioClient(
            @Value("${minio.server.url}") String url,
            @Value("${minio.server.username}") String username,
            @Value("${minio.server.password}") String password
    ) {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(username, password)
                .build();
    }
}
