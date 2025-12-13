package org.example.customersupportrag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter;

    public DocumentIngestionService(VectorStore vectorStore, TokenTextSplitter tokenTextSplitter) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = tokenTextSplitter;
    }

    public int ingestMultipartFile(MultipartFile multipartFile) throws IOException {

        Path tempPath = Path.of("/tmp/" + UUID.randomUUID() + "-" + multipartFile.getOriginalFilename());
        if (Files.notExists(tempPath)) {
            Files.createDirectories(tempPath.getParent());
        }
        Files.createFile(tempPath);
        multipartFile.transferTo(tempPath);
        Resource resource = new FileSystemResource(tempPath);

        // 1- (Extract) Read via Tika Document Reader
        DocumentReader documentReader = new TikaDocumentReader(resource);
        // Extracts and returns the list of documents from the resource.
        List<Document> documents = documentReader.get();

        // 2- (Transform) Split into token-aware chunks
        List<Document> chunks = documents.stream()
                .map(document -> tokenTextSplitter.apply(List.of(document)))
                .flatMap(List::stream)
                .toList();

        // 3- (Load) Add to Vector Store (batch)
        // vectorStore.add(List<Document>) is the canonical method on VectorStore
        // It will call the configured embedding function (Spring AI Ollama embedding) internally.
        vectorStore.add(chunks);


        Files.deleteIfExists(tempPath);
        return chunks.size();
    }
}
