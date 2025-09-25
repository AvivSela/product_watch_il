package com.avivse.storeservice.mapper;

import com.avivse.storeservice.dto.StoreCreateDTO;
import com.avivse.storeservice.dto.StoreResponseDTO;
import com.avivse.storeservice.dto.StoreUpdateDTO;
import com.avivse.storeservice.entity.Store;
import org.springframework.stereotype.Component;

@Component
public class StoreMapper {

    public Store toEntity(StoreCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Store store = new Store();
        store.setStoreNumber(dto.getStoreNumber());
        store.setStoreType(dto.getStoreType());
        store.setStoreName(dto.getStoreName());
        store.setChainId(dto.getChainId());
        store.setSubChainId(dto.getSubChainId());
        store.setCreatedBy(dto.getCreatedBy());
        store.setLastModifiedBy(dto.getCreatedBy()); // Set same as createdBy on creation

        return store;
    }

    public StoreResponseDTO toResponseDTO(Store entity) {
        if (entity == null) {
            return null;
        }

        StoreResponseDTO dto = new StoreResponseDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setVersion(entity.getVersion());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setStoreNumber(entity.getStoreNumber());
        dto.setStoreType(entity.getStoreType());
        dto.setStoreName(entity.getStoreName());
        dto.setChainId(entity.getChainId());
        dto.setSubChainId(entity.getSubChainId());

        return dto;
    }

    public void updateEntityFromDTO(Store entity, StoreUpdateDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        if (dto.getStoreType() != null) {
            entity.setStoreType(dto.getStoreType());
        }

        if (dto.getStoreName() != null) {
            entity.setStoreName(dto.getStoreName());
        }

        if (dto.getSubChainId() != null) {
            entity.setSubChainId(dto.getSubChainId());
        }

        if (dto.getLastModifiedBy() != null) {
            entity.setLastModifiedBy(dto.getLastModifiedBy());
        }
    }
}