package org.example.customersupportrag.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_jobs")
@Setter
@Getter
public class IngestionJob {
    @Id
    private UUID id;
    private String filename;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

    private String error;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
