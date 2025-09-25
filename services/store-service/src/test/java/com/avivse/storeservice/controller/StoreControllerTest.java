package com.avivse.storeservice.controller;

import com.avivse.storeservice.dto.StoreCreateDTO;
import com.avivse.storeservice.dto.StoreResponseDTO;
import com.avivse.storeservice.dto.StoreUpdateDTO;
import com.avivse.storeservice.entity.Store;
import com.avivse.storeservice.exception.StoreAlreadyExistsException;
import com.avivse.storeservice.exception.StoreNotFoundException;
import com.avivse.storeservice.mapper.StoreMapper;
import com.avivse.storeservice.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    StoreService storeService;

    @MockBean
    StoreMapper storeMapper;

    @Autowired
    ObjectMapper objectMapper;

    private Store testStore;
    private StoreResponseDTO responseDTO;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        testStore = new Store();
        testStore.setId(testId);
        testStore.setStoreNumber(123);
        testStore.setStoreType("MAIN");
        testStore.setStoreName("Test Store");
        testStore.setChainId("CHAIN001");
        testStore.setSubChainId(1);
        testStore.setCreatedBy("testuser");
        testStore.setCreatedAt(LocalDateTime.now());
        testStore.setUpdatedAt(LocalDateTime.now());
        testStore.setVersion(0);

        responseDTO = new StoreResponseDTO();
        responseDTO.setId(testId);
        responseDTO.setStoreNumber(123);
        responseDTO.setStoreType("MAIN");
        responseDTO.setStoreName("Test Store");
        responseDTO.setChainId("CHAIN001");
        responseDTO.setSubChainId(1);
        responseDTO.setCreatedBy("testuser");
        responseDTO.setCreatedAt(testStore.getCreatedAt());
        responseDTO.setUpdatedAt(testStore.getUpdatedAt());
        responseDTO.setVersion(0);
    }

    @Test
    void createStore_ShouldReturn201_WhenValidRequest() throws Exception {
        StoreCreateDTO createDTO = new StoreCreateDTO();
        createDTO.setStoreNumber(123);
        createDTO.setStoreType("MAIN");
        createDTO.setStoreName("Test Store");
        createDTO.setChainId("CHAIN001");
        createDTO.setSubChainId(1);
        createDTO.setCreatedBy("testuser");

        when(storeService.createStore(any(StoreCreateDTO.class))).thenReturn(testStore);
        when(storeMapper.toResponseDTO(testStore)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.store_number").value(123))
                .andExpect(jsonPath("$.chain_id").value("CHAIN001"))
                .andExpect(jsonPath("$.store_name").value("Test Store"));

        verify(storeService).createStore(any(StoreCreateDTO.class));
        verify(storeMapper).toResponseDTO(testStore);
    }

    @Test
    void createStore_ShouldReturn400_WhenInvalidRequest() throws Exception {
        StoreCreateDTO invalidDTO = new StoreCreateDTO();
        invalidDTO.setStoreNumber(null);

        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(storeService, never()).createStore(any());
    }

    @Test
    void createStore_ShouldReturn409_WhenStoreAlreadyExists() throws Exception {
        StoreCreateDTO createDTO = new StoreCreateDTO();
        createDTO.setStoreNumber(123);
        createDTO.setStoreType("MAIN");
        createDTO.setStoreName("Test Store");
        createDTO.setChainId("CHAIN001");
        createDTO.setSubChainId(1);

        when(storeService.createStore(any(StoreCreateDTO.class)))
                .thenThrow(new StoreAlreadyExistsException("Store already exists"));

        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STORE_ALREADY_EXISTS"));
    }

    @Test
    void getStoreById_ShouldReturn200_WhenStoreExists() throws Exception {
        when(storeService.findById(testId)).thenReturn(Optional.of(testStore));
        when(storeMapper.toResponseDTO(testStore)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/stores/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.store_number").value(123))
                .andExpect(jsonPath("$.chain_id").value("CHAIN001"));

        verify(storeService).findById(testId);
        verify(storeMapper).toResponseDTO(testStore);
    }

    @Test
    void getStoreById_ShouldReturn404_WhenStoreNotExists() throws Exception {
        when(storeService.findById(testId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/stores/{id}", testId))
                .andExpect(status().isNotFound());

        verify(storeService).findById(testId);
        verify(storeMapper, never()).toResponseDTO(any());
    }

    @Test
    void getStoreByNaturalKey_ShouldReturn200_WhenStoreExists() throws Exception {
        when(storeService.findByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(Optional.of(testStore));
        when(storeMapper.toResponseDTO(testStore)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/stores/by-natural-key")
                        .param("chainId", "CHAIN001")
                        .param("storeNumber", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.store_number").value(123))
                .andExpect(jsonPath("$.chain_id").value("CHAIN001"));

        verify(storeService).findByChainIdAndStoreNumber("CHAIN001", 123);
        verify(storeMapper).toResponseDTO(testStore);
    }

    @Test
    void getStoreByNaturalKey_ShouldReturn404_WhenStoreNotExists() throws Exception {
        when(storeService.findByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/stores/by-natural-key")
                        .param("chainId", "CHAIN001")
                        .param("storeNumber", "123"))
                .andExpect(status().isNotFound());

        verify(storeService).findByChainIdAndStoreNumber("CHAIN001", 123);
        verify(storeMapper, never()).toResponseDTO(any());
    }

    @Test
    void getStoreByNaturalKey_ShouldReturn400_WhenMissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/stores/by-natural-key")
                        .param("chainId", "CHAIN001"))
                .andExpect(status().isBadRequest());

        verify(storeService, never()).findByChainIdAndStoreNumber(any(), any());
    }

    @Test
    void listStores_ShouldReturn200WithPagination() throws Exception {
        List<Store> stores = List.of(testStore);
        Page<Store> page = new PageImpl<>(stores, PageRequest.of(0, 20), 1);

        when(storeService.findAllWithFilters(null, null, null, 1, 20)).thenReturn(page);
        when(storeMapper.toResponseDTO(testStore)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(20))
                .andExpect(jsonPath("$.pagination.total").value(1));

        verify(storeService).findAllWithFilters(null, null, null, 1, 20);
    }

    @Test
    void listStores_ShouldApplyFilters() throws Exception {
        List<Store> stores = List.of(testStore);
        Page<Store> page = new PageImpl<>(stores, PageRequest.of(0, 20), 1);

        when(storeService.findAllWithFilters("CHAIN001", "MAIN", 1, 1, 20)).thenReturn(page);
        when(storeMapper.toResponseDTO(testStore)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/stores")
                        .param("chainId", "CHAIN001")
                        .param("storeType", "MAIN")
                        .param("subChainId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(storeService).findAllWithFilters("CHAIN001", "MAIN", 1, 1, 20);
    }

    @Test
    void updateStore_ShouldReturn200_WhenStoreExists() throws Exception {
        StoreUpdateDTO updateDTO = new StoreUpdateDTO();
        updateDTO.setStoreType("UPDATED");
        updateDTO.setStoreName("Updated Store");

        Store updatedStore = new Store();
        updatedStore.setId(testId);
        updatedStore.setStoreType("UPDATED");
        updatedStore.setStoreName("Updated Store");

        StoreResponseDTO updatedResponseDTO = new StoreResponseDTO();
        updatedResponseDTO.setId(testId);
        updatedResponseDTO.setStoreType("UPDATED");
        updatedResponseDTO.setStoreName("Updated Store");

        when(storeService.updateStore(eq(testId), any(StoreUpdateDTO.class))).thenReturn(updatedStore);
        when(storeMapper.toResponseDTO(updatedStore)).thenReturn(updatedResponseDTO);

        mockMvc.perform(put("/api/v1/stores/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.store_type").value("UPDATED"))
                .andExpect(jsonPath("$.store_name").value("Updated Store"));

        verify(storeService).updateStore(eq(testId), any(StoreUpdateDTO.class));
        verify(storeMapper).toResponseDTO(updatedStore);
    }

    @Test
    void updateStore_ShouldReturn404_WhenStoreNotExists() throws Exception {
        StoreUpdateDTO updateDTO = new StoreUpdateDTO();
        updateDTO.setStoreType("UPDATED");

        when(storeService.updateStore(eq(testId), any(StoreUpdateDTO.class)))
                .thenThrow(new StoreNotFoundException("Store not found"));

        mockMvc.perform(put("/api/v1/stores/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"));
    }

    @Test
    void deleteStore_ShouldReturn204_WhenStoreExists() throws Exception {
        when(storeService.deleteStore(testId)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/stores/{id}", testId))
                .andExpect(status().isNoContent());

        verify(storeService).deleteStore(testId);
    }

    @Test
    void deleteStore_ShouldReturn404_WhenStoreNotExists() throws Exception {
        when(storeService.deleteStore(testId)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/stores/{id}", testId))
                .andExpect(status().isNotFound());

        verify(storeService).deleteStore(testId);
    }
}