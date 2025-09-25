package com.avivse.storeservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stores", uniqueConstraints = {
    @UniqueConstraint(name = "uk_store_number_chain_id", columnNames = {"storeNumber", "chainId"})
})
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @NotNull(message = "Store number is required")
    @Column(name = "store_number", nullable = false)
    private Integer storeNumber;

    @NotNull(message = "Store type is required")
    @Size(max = 10, message = "Store type cannot exceed 10 characters")
    @Column(name = "store_type", nullable = false, length = 10)
    private String storeType;

    @NotNull(message = "Store name is required")
    @Size(max = 100, message = "Store name cannot exceed 100 characters")
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @NotNull(message = "Chain ID is required")
    @Size(max = 20, message = "Chain ID cannot exceed 20 characters")
    @Column(name = "chain_id", nullable = false, length = 20)
    private String chainId;

    @NotNull(message = "Sub-chain ID is required")
    @Positive(message = "Sub-chain ID must be positive")
    @Column(name = "sub_chain_id", nullable = false)
    private Integer subChainId;

    public Store() {
    }

    public Store(Integer storeNumber, String storeType, String storeName, String chainId, Integer subChainId) {
        this.storeNumber = storeNumber;
        this.storeType = storeType;
        this.storeName = storeName;
        this.chainId = chainId;
        this.subChainId = subChainId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Integer getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(Integer storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public Integer getSubChainId() {
        return subChainId;
    }

    public void setSubChainId(Integer subChainId) {
        this.subChainId = subChainId;
    }
}