package com.avivse.storeservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class StoreCreateDTO {

    @NotNull(message = "Store number is required")
    private Integer storeNumber;

    @NotNull(message = "Store type is required")
    @Size(max = 10, message = "Store type cannot exceed 10 characters")
    private String storeType;

    @NotNull(message = "Store name is required")
    @Size(max = 100, message = "Store name cannot exceed 100 characters")
    private String storeName;

    @NotNull(message = "Chain ID is required")
    @Size(max = 20, message = "Chain ID cannot exceed 20 characters")
    private String chainId;

    @NotNull(message = "Sub-chain ID is required")
    @Positive(message = "Sub-chain ID must be positive")
    private Integer subChainId;

    @Size(max = 100, message = "Created by cannot exceed 100 characters")
    private String createdBy;

    public StoreCreateDTO() {
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}