package org.example.customersupportrag.web;

import org.example.customersupportrag.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestPart MultipartFile file) throws IOException {
        int chunks = documentIngestionService.ingestMultipartFile(file);
        return ResponseEntity.ok().body(
                Map.of(
                        "status", "ok",
                        "chunks", chunks
                )
        );
    }
}
