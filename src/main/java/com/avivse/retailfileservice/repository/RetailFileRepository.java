package com.avivse.retailfileservice.repository;

import com.avivse.retailfileservice.entity.RetailFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RetailFileRepository extends JpaRepository<RetailFile, UUID> {

    // Find files by chain ID
    List<RetailFile> findByChainId(String chainId);

    // Find files by chain ID and store ID
    List<RetailFile> findByChainIdAndStoreId(String chainId, Integer storeId);

    // Find files by processing status
    List<RetailFile> findByIsProcessed(Boolean isProcessed);

    // Find files by chain ID and processing status
    List<RetailFile> findByChainIdAndIsProcessed(String chainId, Boolean isProcessed);

    // Find files by chain ID, store ID, and processing status
    List<RetailFile> findByChainIdAndStoreIdAndIsProcessed(String chainId, Integer storeId, Boolean isProcessed);

    // Paginated query for all files with optional filters
    @Query("SELECT rf FROM RetailFile rf WHERE " +
            "(:chainId IS NULL OR rf.chainId = :chainId) AND " +
            "(:storeId IS NULL OR rf.storeId = :storeId) AND " +
            "(:isProcessed IS NULL OR rf.isProcessed = :isProcessed)")
    Page<RetailFile> findWithFilters(
            @Param("chainId") String chainId,
            @Param("storeId") Integer storeId,
            @Param("isProcessed") Boolean isProcessed,
            Pageable pageable
    );
}