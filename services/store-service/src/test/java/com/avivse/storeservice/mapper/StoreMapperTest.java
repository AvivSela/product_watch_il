package com.avivse.storeservice.mapper;

import com.avivse.storeservice.dto.StoreCreateDTO;
import com.avivse.storeservice.dto.StoreResponseDTO;
import com.avivse.storeservice.dto.StoreUpdateDTO;
import com.avivse.storeservice.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StoreMapperTest {

    private StoreMapper storeMapper;
    private UUID testId;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        storeMapper = new StoreMapper();
        testId = UUID.randomUUID();
        testTime = LocalDateTime.now();
    }

    @Test
    void toEntity_ShouldMapCreateDTOToEntity() {
        StoreCreateDTO dto = new StoreCreateDTO();
        dto.setStoreNumber(123);
        dto.setStoreType("MAIN");
        dto.setStoreName("Test Store");
        dto.setChainId("CHAIN001");
        dto.setSubChainId(1);
        dto.setCreatedBy("testuser");

        Store result = storeMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(123, result.getStoreNumber());
        assertEquals("MAIN", result.getStoreType());
        assertEquals("Test Store", result.getStoreName());
        assertEquals("CHAIN001", result.getChainId());
        assertEquals(1, result.getSubChainId());
        assertEquals("testuser", result.getCreatedBy());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenDTOIsNull() {
        Store result = storeMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toResponseDTO_ShouldMapEntityToResponseDTO() {
        Store entity = new Store();
        entity.setId(testId);
        entity.setCreatedAt(testTime);
        entity.setUpdatedAt(testTime);
        entity.setVersion(1);
        entity.setCreatedBy("creator");
        entity.setLastModifiedBy("modifier");
        entity.setStoreNumber(456);
        entity.setStoreType("OUTLET");
        entity.setStoreName("Outlet Store");
        entity.setChainId("CHAIN002");
        entity.setSubChainId(2);

        StoreResponseDTO result = storeMapper.toResponseDTO(entity);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testTime, result.getCreatedAt());
        assertEquals(testTime, result.getUpdatedAt());
        assertEquals(1, result.getVersion());
        assertEquals("creator", result.getCreatedBy());
        assertEquals("modifier", result.getLastModifiedBy());
        assertEquals(456, result.getStoreNumber());
        assertEquals("OUTLET", result.getStoreType());
        assertEquals("Outlet Store", result.getStoreName());
        assertEquals("CHAIN002", result.getChainId());
        assertEquals(2, result.getSubChainId());
    }

    @Test
    void toResponseDTO_ShouldReturnNull_WhenEntityIsNull() {
        StoreResponseDTO result = storeMapper.toResponseDTO(null);
        assertNull(result);
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateAllFields() {
        Store entity = new Store();
        entity.setStoreType("OLD_TYPE");
        entity.setStoreName("Old Name");
        entity.setSubChainId(1);
        entity.setLastModifiedBy("old_modifier");

        StoreUpdateDTO dto = new StoreUpdateDTO();
        dto.setStoreType("NEW_TYPE");
        dto.setStoreName("New Name");
        dto.setSubChainId(2);
        dto.setLastModifiedBy("new_modifier");

        storeMapper.updateEntityFromDTO(entity, dto);

        assertEquals("NEW_TYPE", entity.getStoreType());
        assertEquals("New Name", entity.getStoreName());
        assertEquals(2, entity.getSubChainId());
        assertEquals("new_modifier", entity.getLastModifiedBy());
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateOnlyNonNullFields() {
        Store entity = new Store();
        entity.setStoreType("OLD_TYPE");
        entity.setStoreName("Old Name");
        entity.setSubChainId(1);
        entity.setLastModifiedBy("old_modifier");

        StoreUpdateDTO dto = new StoreUpdateDTO();
        dto.setStoreType("NEW_TYPE");
        dto.setStoreName(null);
        dto.setSubChainId(null);
        dto.setLastModifiedBy(null);

        storeMapper.updateEntityFromDTO(entity, dto);

        assertEquals("NEW_TYPE", entity.getStoreType());
        assertEquals("Old Name", entity.getStoreName());
        assertEquals(1, entity.getSubChainId());
        assertEquals("old_modifier", entity.getLastModifiedBy());
    }

    @Test
    void updateEntityFromDTO_ShouldHandleNullEntity() {
        StoreUpdateDTO dto = new StoreUpdateDTO();
        dto.setStoreType("NEW_TYPE");

        assertDoesNotThrow(() -> storeMapper.updateEntityFromDTO(null, dto));
    }

    @Test
    void updateEntityFromDTO_ShouldHandleNullDTO() {
        Store entity = new Store();
        entity.setStoreType("OLD_TYPE");

        assertDoesNotThrow(() -> storeMapper.updateEntityFromDTO(entity, null));
        assertEquals("OLD_TYPE", entity.getStoreType());
    }

    @Test
    void updateEntityFromDTO_ShouldHandleBothNull() {
        assertDoesNotThrow(() -> storeMapper.updateEntityFromDTO(null, null));
    }
}