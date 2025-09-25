package com.avivse.storeservice.service;

import com.avivse.storeservice.dto.StoreCreateDTO;
import com.avivse.storeservice.dto.StoreUpdateDTO;
import com.avivse.storeservice.entity.Store;
import com.avivse.storeservice.exception.StoreAlreadyExistsException;
import com.avivse.storeservice.exception.StoreNotFoundException;
import com.avivse.storeservice.mapper.StoreMapper;
import com.avivse.storeservice.repository.StoreRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreMapper storeMapper;

    private SimpleMeterRegistry meterRegistry;

    private StoreService storeService;

    private Store testStore;
    private StoreCreateDTO createDTO;
    private StoreUpdateDTO updateDTO;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testStore = new Store();
        testStore.setId(testId);
        testStore.setStoreNumber(123);
        testStore.setStoreType("MAIN");
        testStore.setStoreName("Test Store");
        testStore.setChainId("CHAIN001");
        testStore.setSubChainId(1);
        testStore.setCreatedAt(LocalDateTime.now());
        testStore.setUpdatedAt(LocalDateTime.now());
        testStore.setVersion(0);

        createDTO = new StoreCreateDTO();
        createDTO.setStoreNumber(123);
        createDTO.setStoreType("MAIN");
        createDTO.setStoreName("Test Store");
        createDTO.setChainId("CHAIN001");
        createDTO.setSubChainId(1);
        createDTO.setCreatedBy("testuser");

        updateDTO = new StoreUpdateDTO();
        updateDTO.setStoreType("UPDATED");
        updateDTO.setStoreName("Updated Store");
        updateDTO.setLastModifiedBy("modifier");

        meterRegistry = new SimpleMeterRegistry();
        storeService = new StoreService(storeRepository, storeMapper, meterRegistry);
    }

    @Test
    void createStore_ShouldCreateAndReturnStore() {
        when(storeRepository.existsByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(false);
        when(storeMapper.toEntity(createDTO)).thenReturn(testStore);
        when(storeRepository.save(any(Store.class))).thenReturn(testStore);

        Store result = storeService.createStore(createDTO);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("CHAIN001", result.getChainId());
        assertEquals(Integer.valueOf(123), result.getStoreNumber());

        verify(storeRepository).existsByChainIdAndStoreNumber("CHAIN001", 123);
        verify(storeMapper).toEntity(createDTO);
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void createStore_ShouldThrowException_WhenStoreAlreadyExists() {
        when(storeRepository.existsByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(true);

        assertThrows(StoreAlreadyExistsException.class, () -> storeService.createStore(createDTO));

        verify(storeRepository).existsByChainIdAndStoreNumber("CHAIN001", 123);
        verify(storeMapper, never()).toEntity(any());
        verify(storeRepository, never()).save(any());
    }

    @Test
    void createStore_ShouldThrowException_OnDataIntegrityViolation() {
        when(storeRepository.existsByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(false);
        when(storeMapper.toEntity(createDTO)).thenReturn(testStore);
        when(storeRepository.save(any(Store.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));

        assertThrows(StoreAlreadyExistsException.class, () -> storeService.createStore(createDTO));

        verify(storeRepository).existsByChainIdAndStoreNumber("CHAIN001", 123);
        verify(storeMapper).toEntity(createDTO);
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void findById_ShouldReturnStore_WhenExists() {
        when(storeRepository.findById(testId)).thenReturn(Optional.of(testStore));

        Optional<Store> result = storeService.findById(testId);

        assertTrue(result.isPresent());
        assertEquals(testStore.getId(), result.get().getId());
        verify(storeRepository).findById(testId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(storeRepository.findById(testId)).thenReturn(Optional.empty());

        Optional<Store> result = storeService.findById(testId);

        assertFalse(result.isPresent());
        verify(storeRepository).findById(testId);
    }

    @Test
    void findByChainIdAndStoreNumber_ShouldReturnStore_WhenExists() {
        when(storeRepository.findByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(Optional.of(testStore));

        Optional<Store> result = storeService.findByChainIdAndStoreNumber("CHAIN001", 123);

        assertTrue(result.isPresent());
        assertEquals(testStore.getId(), result.get().getId());
        verify(storeRepository).findByChainIdAndStoreNumber("CHAIN001", 123);
    }

    @Test
    void findByChainIdAndStoreNumber_ShouldReturnEmpty_WhenNotExists() {
        when(storeRepository.findByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(Optional.empty());

        Optional<Store> result = storeService.findByChainIdAndStoreNumber("CHAIN001", 123);

        assertFalse(result.isPresent());
        verify(storeRepository).findByChainIdAndStoreNumber("CHAIN001", 123);
    }

    @Test
    void findAllWithFilters_ShouldReturnPagedResults() {
        List<Store> stores = List.of(testStore);
        Page<Store> page = new PageImpl<>(stores);
        Pageable expectedPageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("createdAt").descending());

        when(storeRepository.findWithFilters(eq("CHAIN001"), eq("MAIN"), eq(1), any(Pageable.class)))
                .thenReturn(page);

        Page<Store> result = storeService.findAllWithFilters("CHAIN001", "MAIN", 1, 1, 20);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testStore.getId(), result.getContent().get(0).getId());

        verify(storeRepository).findWithFilters(eq("CHAIN001"), eq("MAIN"), eq(1), any(Pageable.class));
    }

    @Test
    void updateStore_ShouldUpdateAndReturnStore_WhenExists() {
        when(storeRepository.findById(testId)).thenReturn(Optional.of(testStore));
        when(storeRepository.save(any(Store.class))).thenReturn(testStore);

        Store result = storeService.updateStore(testId, updateDTO);

        assertNotNull(result);
        assertEquals(testId, result.getId());

        verify(storeRepository).findById(testId);
        verify(storeMapper).updateEntityFromDTO(testStore, updateDTO);
        verify(storeRepository).save(testStore);
    }

    @Test
    void updateStore_ShouldThrowException_WhenNotExists() {
        when(storeRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class, () -> storeService.updateStore(testId, updateDTO));

        verify(storeRepository).findById(testId);
        verify(storeMapper, never()).updateEntityFromDTO(any(), any());
        verify(storeRepository, never()).save(any());
    }

    @Test
    void deleteStore_ShouldReturnTrue_WhenExists() {
        when(storeRepository.existsById(testId)).thenReturn(true);
        when(storeRepository.findById(testId)).thenReturn(Optional.of(testStore));

        boolean result = storeService.deleteStore(testId);

        assertTrue(result);
        verify(storeRepository).existsById(testId);
        verify(storeRepository).deleteById(testId);
    }

    @Test
    void deleteStore_ShouldReturnFalse_WhenNotExists() {
        when(storeRepository.existsById(testId)).thenReturn(false);

        boolean result = storeService.deleteStore(testId);

        assertFalse(result);
        verify(storeRepository).existsById(testId);
        verify(storeRepository, never()).deleteById(any());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(storeRepository.existsById(testId)).thenReturn(true);

        boolean result = storeService.existsById(testId);

        assertTrue(result);
        verify(storeRepository).existsById(testId);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        when(storeRepository.existsById(testId)).thenReturn(false);

        boolean result = storeService.existsById(testId);

        assertFalse(result);
        verify(storeRepository).existsById(testId);
    }

    @Test
    void existsByChainIdAndStoreNumber_ShouldReturnTrue_WhenExists() {
        when(storeRepository.existsByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(true);

        boolean result = storeService.existsByChainIdAndStoreNumber("CHAIN001", 123);

        assertTrue(result);
        verify(storeRepository).existsByChainIdAndStoreNumber("CHAIN001", 123);
    }

    @Test
    void existsByChainIdAndStoreNumber_ShouldReturnFalse_WhenNotExists() {
        when(storeRepository.existsByChainIdAndStoreNumber("CHAIN001", 123)).thenReturn(false);

        boolean result = storeService.existsByChainIdAndStoreNumber("CHAIN001", 123);

        assertFalse(result);
        verify(storeRepository).existsByChainIdAndStoreNumber("CHAIN001", 123);
    }
}