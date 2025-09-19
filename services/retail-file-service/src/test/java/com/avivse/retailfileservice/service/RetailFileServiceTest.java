package com.avivse.retailfileservice.service;

import com.avivse.retailfileservice.dto.CreateRetailFileRequest;
import com.avivse.retailfileservice.dto.UpdateRetailFileRequest;
import com.avivse.retailfileservice.entity.RetailFile;
import com.avivse.retailfileservice.repository.RetailFileRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class RetailFileServiceTest {

    @Mock
    private RetailFileRepository retailFileRepository;

    private SimpleMeterRegistry meterRegistry;

    private RetailFileService retailFileService;

    private RetailFile testRetailFile;
    private CreateRetailFileRequest createRequest;
    private UpdateRetailFileRequest updateRequest;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        // Create test RetailFile entity
        testRetailFile = new RetailFile();
        testRetailFile.setId(testId);
        testRetailFile.setChainId("chain_001");
        testRetailFile.setStoreId(123);
        testRetailFile.setFileName("test_file.csv");
        testRetailFile.setFileUrl("https://example.com/test_file.csv");
        testRetailFile.setFileSize(1024L);
        testRetailFile.setUploadDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        testRetailFile.setIsProcessed(false);
        testRetailFile.setCreatedAt(LocalDateTime.now());
        testRetailFile.setUpdatedAt(LocalDateTime.now());

        // Create test CreateRetailFileRequest
        createRequest = new CreateRetailFileRequest();
        createRequest.setChainId("chain_001");
        createRequest.setStoreId(123);
        createRequest.setFileName("test_file.csv");
        createRequest.setFileUrl("https://example.com/test_file.csv");
        createRequest.setFileSize(1024L);
        createRequest.setUploadDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        createRequest.setIsProcessed(false);

        // Create test UpdateRetailFileRequest
        updateRequest = new UpdateRetailFileRequest();
        updateRequest.setFileSize(2048L);
        updateRequest.setIsProcessed(true);

        // Create a real SimpleMeterRegistry for metrics
        meterRegistry = new SimpleMeterRegistry();

        // Manually create the service with mocked dependencies
        retailFileService = new RetailFileService(retailFileRepository, meterRegistry);
    }

    @Test
    void createRetailFile_ShouldCreateAndReturnRetailFile() {
        // Given
        when(retailFileRepository.save(any(RetailFile.class))).thenReturn(testRetailFile);

        // When
        RetailFile result = retailFileService.createRetailFile(createRequest);

        // Then
        assertNotNull(result);
        assertEquals("chain_001", result.getChainId());
        assertEquals("test_file.csv", result.getFileName());
        assertEquals(false, result.getIsProcessed());

        verify(retailFileRepository, times(1)).save(any(RetailFile.class));
    }

    @Test
    void createRetailFile_ShouldSetDefaultValues_WhenNotProvided() {
        // Given
        createRequest.setUploadDate(null);
        createRequest.setIsProcessed(null);

        when(retailFileRepository.save(any(RetailFile.class))).thenReturn(testRetailFile);

        // When
        RetailFile result = retailFileService.createRetailFile(createRequest);

        // Then
        assertNotNull(result);
        verify(retailFileRepository, times(1)).save(any(RetailFile.class));
    }

    @Test
    void findById_ShouldReturnRetailFile_WhenExists() {
        // Given
        when(retailFileRepository.findById(testId)).thenReturn(Optional.of(testRetailFile));

        // When
        Optional<RetailFile> result = retailFileService.findById(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRetailFile.getId(), result.get().getId());
        assertEquals("chain_001", result.get().getChainId());

        verify(retailFileRepository, times(1)).findById(testId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(retailFileRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        Optional<RetailFile> result = retailFileService.findById(testId);

        // Then
        assertFalse(result.isPresent());
        verify(retailFileRepository, times(1)).findById(testId);
    }

    @Test
    void findAllWithFilters_ShouldReturnPagedResults() {
        // Given
        List<RetailFile> files = List.of(testRetailFile);
        Page<RetailFile> page = new PageImpl<>(files);
        Pageable pageable = PageRequest.of(0, 20);

        when(retailFileRepository.findWithFilters(eq("chain_001"), eq(123), eq(false), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<RetailFile> result = retailFileService.findAllWithFilters("chain_001", 123, false, 1, 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testRetailFile.getId(), result.getContent().get(0).getId());

        verify(retailFileRepository, times(1)).findWithFilters(eq("chain_001"), eq(123), eq(false), any(Pageable.class));
    }

    @Test
    void updateRetailFile_ShouldUpdateAndReturnFile_WhenExists() {
        // Given
        when(retailFileRepository.findById(testId)).thenReturn(Optional.of(testRetailFile));
        when(retailFileRepository.save(any(RetailFile.class))).thenReturn(testRetailFile);

        // When
        RetailFile result = retailFileService.updateRetailFile(testId, updateRequest);

        // Then
        assertNotNull(result);
        verify(retailFileRepository, times(1)).findById(testId);
        verify(retailFileRepository, times(1)).save(any(RetailFile.class));
    }

    @Test
    void updateRetailFile_ShouldReturnNull_WhenNotExists() {
        // Given
        when(retailFileRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        RetailFile result = retailFileService.updateRetailFile(testId, updateRequest);

        // Then
        assertNull(result);
        verify(retailFileRepository, times(1)).findById(testId);
        verify(retailFileRepository, never()).save(any(RetailFile.class));
    }

    @Test
    void markAsProcessed_ShouldUpdateProcessingStatus_WhenExists() {
        // Given
        when(retailFileRepository.findById(testId)).thenReturn(Optional.of(testRetailFile));
        when(retailFileRepository.save(any(RetailFile.class))).thenReturn(testRetailFile);

        // When
        RetailFile result = retailFileService.markAsProcessed(testId);

        // Then
        assertNotNull(result);
        verify(retailFileRepository, times(1)).findById(testId);
        verify(retailFileRepository, times(1)).save(any(RetailFile.class));
    }

    @Test
    void deleteRetailFile_ShouldReturnTrue_WhenExists() {
        // Given
        when(retailFileRepository.existsById(testId)).thenReturn(true);
        doNothing().when(retailFileRepository).deleteById(testId);

        // When
        boolean result = retailFileService.deleteRetailFile(testId);

        // Then
        assertTrue(result);
        verify(retailFileRepository, times(1)).existsById(testId);
        verify(retailFileRepository, times(1)).deleteById(testId);
    }

    @Test
    void deleteRetailFile_ShouldReturnFalse_WhenNotExists() {
        // Given
        when(retailFileRepository.existsById(testId)).thenReturn(false);

        // When
        boolean result = retailFileService.deleteRetailFile(testId);

        // Then
        assertFalse(result);
        verify(retailFileRepository, times(1)).existsById(testId);
        verify(retailFileRepository, never()).deleteById(testId);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        // Given
        when(retailFileRepository.existsById(testId)).thenReturn(true);

        // When
        boolean result = retailFileService.existsById(testId);

        // Then
        assertTrue(result);
        verify(retailFileRepository, times(1)).existsById(testId);
    }
}