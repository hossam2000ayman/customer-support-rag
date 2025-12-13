package org.example.customersupportrag.web;

import org.example.customersupportrag.model.IngestionJob;
import org.example.customersupportrag.repository.IngestionJobRepository;
import org.example.customersupportrag.service.ObjectStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    private final IngestionJobRepository ingestionJobRepository;
    private final ObjectStorageService objectStorageService;

    public DocumentController(IngestionJobRepository ingestionJobRepository, ObjectStorageService objectStorageService) {
        this.ingestionJobRepository = ingestionJobRepository;
        this.objectStorageService = objectStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestPart MultipartFile file) {

        UUID jobId = UUID.randomUUID();

        // 1️⃣ create job
        IngestionJob job = new IngestionJob();
        job.setId(jobId);
        job.setFilename(file.getOriginalFilename());
        job.setStatus("CREATED");
        ingestionJobRepository.save(job);

        // 2️⃣ upload file to MinIO
        objectStorageService.upload(jobId, file);

        // 3️⃣ respond immediately
        return ResponseEntity.accepted().body(
                Map.of(
                        "jobId", jobId,
                        "status", "CREATED"
                )
        );
    }
}
