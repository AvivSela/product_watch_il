package com.avivse.retailfileservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateStoreDto {

    @JsonProperty("store_number")
    private Integer storeNumber;
    @JsonProperty("store_type")
    private String storeType;
    @JsonProperty("store_name")
    private String storeName;
    @JsonProperty("chain_id")
    private String chainId;
    @JsonProperty("sub_chain_id")
    private Integer subChainId;

    public CreateStoreDto() {
    }

    public CreateStoreDto(Integer storeNumber, String storeType, String storeName, String chainId, Integer subChainId) {
        this.storeNumber = storeNumber;
        this.storeType = storeType;
        this.storeName = storeName;
        this.chainId = chainId;
        this.subChainId = subChainId;
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