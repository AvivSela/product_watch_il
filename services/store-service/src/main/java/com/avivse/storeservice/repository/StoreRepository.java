package com.avivse.storeservice.repository;

import com.avivse.storeservice.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByChainIdAndStoreNumber(String chainId, Integer storeNumber);

    @Query("SELECT s FROM Store s WHERE " +
           "(:chainId IS NULL OR s.chainId = :chainId) AND " +
           "(:storeType IS NULL OR s.storeType = :storeType) AND " +
           "(:subChainId IS NULL OR s.subChainId = :subChainId)")
    Page<Store> findWithFilters(@Param("chainId") String chainId,
                               @Param("storeType") String storeType,
                               @Param("subChainId") Integer subChainId,
                               Pageable pageable);

    boolean existsByChainIdAndStoreNumber(String chainId, Integer storeNumber);
}