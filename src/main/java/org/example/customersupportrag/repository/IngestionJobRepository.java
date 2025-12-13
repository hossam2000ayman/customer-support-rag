package org.example.customersupportrag.repository;

import org.example.customersupportrag.model.IngestionJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionJobRepository extends JpaRepository<IngestionJob, UUID> {

}
