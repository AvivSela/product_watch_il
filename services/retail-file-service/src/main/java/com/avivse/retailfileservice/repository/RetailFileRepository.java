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

    // Find files by chain ID
    List<RetailFile> findByChainId(String chainId);

    // Find files by chain ID and store ID
    List<RetailFile> findByChainIdAndStoreId(String chainId, Integer storeId);

    // Find files by processing status
    List<RetailFile> findByStatus(FileProcessingStatus status);

    // Find files by chain ID and processing status
    List<RetailFile> findByChainIdAndStatus(String chainId, FileProcessingStatus status);

    // Find files by chain ID, store ID, and processing status
    List<RetailFile> findByChainIdAndStoreIdAndStatus(String chainId, Integer storeId, FileProcessingStatus status);

    // Duplicate detection methods
    Optional<RetailFile> findByChainIdAndFileNameAndFileUrl(String chainId, String fileName, String fileUrl);

    Optional<RetailFile> findByChecksum(String checksum);

    boolean existsByChainIdAndFileNameAndFileUrl(String chainId, String fileName, String fileUrl);

    boolean existsByChecksum(String checksum);

    // Paginated query for all files with optional filters
    @Query("SELECT rf FROM RetailFile rf WHERE " +
            "(:chainId IS NULL OR rf.chainId = :chainId) AND " +
            "(:storeId IS NULL OR rf.storeId = :storeId) AND " +
            "(:status IS NULL OR rf.status = :status)")
    Page<RetailFile> findWithFilters(
            @Param("chainId") String chainId,
            @Param("storeId") Integer storeId,
            @Param("status") FileProcessingStatus status,
            Pageable pageable
    );
}