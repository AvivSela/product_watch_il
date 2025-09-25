package com.avivse.retailfileservice.repository;

import com.avivse.retailfileservice.entity.RetailFile;
import com.avivse.retailfileservice.enums.FileProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RetailFileRepository extends JpaRepository<RetailFile, UUID> {

    // Find files by processing status
    List<RetailFile> findByStatus(FileProcessingStatus status);

    // Duplicate detection methods
    Optional<RetailFile> findByChecksum(String checksum);

    boolean existsByChecksum(String checksum);

    // Paginated query for all files with optional filters
    @Query("SELECT rf FROM RetailFile rf WHERE " +
            "(:status IS NULL OR rf.status = :status)")
    Page<RetailFile> findWithFilters(
            @Param("status") FileProcessingStatus status,
            Pageable pageable
    );
}