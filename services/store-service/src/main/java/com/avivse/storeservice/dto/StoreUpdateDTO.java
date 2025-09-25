package com.avivse.storeservice.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class StoreUpdateDTO {

    @Size(max = 10, message = "Store type cannot exceed 10 characters")
    private String storeType;

    @Size(max = 100, message = "Store name cannot exceed 100 characters")
    private String storeName;

    @Positive(message = "Sub-chain ID must be positive")
    private Integer subChainId;

    @Size(max = 100, message = "Last modified by cannot exceed 100 characters")
    private String lastModifiedBy;

    public StoreUpdateDTO() {
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

    public Integer getSubChainId() {
        return subChainId;
    }

    public void setSubChainId(Integer subChainId) {
        this.subChainId = subChainId;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}