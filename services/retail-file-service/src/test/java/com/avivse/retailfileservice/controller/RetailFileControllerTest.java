package com.avivse.retailfileservice.controller;

import com.avivse.retailfileservice.dto.CreateRetailFileRequest;
import com.avivse.retailfileservice.dto.UpdateRetailFileRequest;
import com.avivse.retailfileservice.entity.RetailFile;
import com.avivse.retailfileservice.enums.FileProcessingStatus;
import com.avivse.retailfileservice.service.RetailFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

@WebMvcTest(RetailFileController.class)
class RetailFileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RetailFileService retailFileService;

    @Autowired
    ObjectMapper objectMapper;

    // Keep only fields used across multiple test methods
    private RetailFile testRetailFile;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Create test RetailFile entity (used in multiple tests)
        testRetailFile = new RetailFile();
        testRetailFile.setId(testId);
        testRetailFile.setFileName("test_file.csv");
        testRetailFile.setFileUrl("https://example.com/test_file.csv");
        testRetailFile.setFileSize(1024L);
        testRetailFile.setUploadDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        testRetailFile.setStatus(FileProcessingStatus.PENDING);
        testRetailFile.setCreatedAt(LocalDateTime.now());
        testRetailFile.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createRetailFile_ShouldReturn201_WhenValidRequest() throws Exception {
        // Given - create request locally since only used in this test
        CreateRetailFileRequest createRequest = new CreateRetailFileRequest();
        createRequest.setFileName("test_file.csv");
        createRequest.setFileUrl("https://example.com/test_file.csv");
        createRequest.setFileSize(1024L);
        createRequest.setStatus(FileProcessingStatus.PENDING);
        createRequest.setStoreNumber(123);
        createRequest.setChainId("CHAIN001");

        when(retailFileService.createRetailFile(any(CreateRetailFileRequest.class)))
                .thenReturn(testRetailFile);

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.file_name").value("test_file.csv"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(retailFileService, times(1)).createRetailFile(any(CreateRetailFileRequest.class));
    }

    @Test
    void createRetailFile_ShouldReturn400_WhenInvalidRequest() throws Exception {
        // Given - create invalid request locally
        CreateRetailFileRequest invalidRequest = new CreateRetailFileRequest();
        invalidRequest.setFileName(""); // Missing required field
        invalidRequest.setFileUrl("https://example.com/test_file.csv");
        invalidRequest.setStoreNumber(123);
        invalidRequest.setChainId("CHAIN001");

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(retailFileService, never()).createRetailFile(any(CreateRetailFileRequest.class));
    }

    @Test
    void getRetailFileById_ShouldReturn200_WhenFileExists() throws Exception {
        // Given
        when(retailFileService.findById(testId)).thenReturn(Optional.of(testRetailFile));

        // When & Then
        mockMvc.perform(get("/v1/retail-files/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.file_name").value("test_file.csv"));

        verify(retailFileService, times(1)).findById(testId);
    }

    @Test
    void getRetailFileById_ShouldReturn404_WhenFileNotExists() throws Exception {
        // Given
        when(retailFileService.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/v1/retail-files/{id}", testId))
                .andExpect(status().isNotFound());

        verify(retailFileService, times(1)).findById(testId);
    }

    @Test
    void listRetailFiles_ShouldReturn200WithPagination() throws Exception {
        // Given
        List<RetailFile> files = List.of(testRetailFile);
        Page<RetailFile> page = new PageImpl<>(files);

        when(retailFileService.findAllWithFilters(null, 1, 20))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/retail-files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(20))
                .andExpect(jsonPath("$.pagination.total").value(1));

        verify(retailFileService, times(1)).findAllWithFilters(null, 1, 20);
    }

    @Test
    void listRetailFiles_ShouldApplyFilters() throws Exception {
        // Given
        List<RetailFile> files = List.of(testRetailFile);
        Page<RetailFile> page = new PageImpl<>(files);

        when(retailFileService.findAllWithFilters(FileProcessingStatus.PENDING, 1, 20))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/retail-files")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(retailFileService, times(1)).findAllWithFilters(FileProcessingStatus.PENDING, 1, 20);
    }

    @Test
    void updateRetailFile_ShouldReturn200_WhenFileExists() throws Exception {
        // Given - create update request locally
        UpdateRetailFileRequest updateRequest = new UpdateRetailFileRequest();
        updateRequest.setFileSize(2048L);
        updateRequest.setStatus(FileProcessingStatus.COMPLETED);

        testRetailFile.setFileSize(2048L);
        testRetailFile.setStatus(FileProcessingStatus.COMPLETED);

        when(retailFileService.updateRetailFile(eq(testId), any(UpdateRetailFileRequest.class)))
                .thenReturn(testRetailFile);

        // When & Then
        mockMvc.perform(put("/v1/retail-files/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file_size").value(2048))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(retailFileService, times(1)).updateRetailFile(eq(testId), any(UpdateRetailFileRequest.class));
    }

    @Test
    void updateRetailFile_ShouldReturn404_WhenFileNotExists() throws Exception {
        // Given - create update request locally
        UpdateRetailFileRequest updateRequest = new UpdateRetailFileRequest();
        updateRequest.setFileSize(2048L);
        updateRequest.setStatus(FileProcessingStatus.COMPLETED);

        when(retailFileService.updateRetailFile(eq(testId), any(UpdateRetailFileRequest.class)))
                .thenThrow(new com.avivse.retailfileservice.exception.RetailFileNotFoundException("Retail file not found with id: " + testId));

        // When & Then
        mockMvc.perform(put("/v1/retail-files/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(retailFileService, times(1)).updateRetailFile(eq(testId), any(UpdateRetailFileRequest.class));
    }

    @Test
    void deleteRetailFile_ShouldReturn204_WhenFileExists() throws Exception {
        // Given
        when(retailFileService.deleteRetailFile(testId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/v1/retail-files/{id}", testId))
                .andExpect(status().isNoContent());

        verify(retailFileService, times(1)).deleteRetailFile(testId);
    }

    @Test
    void deleteRetailFile_ShouldReturn404_WhenFileNotExists() throws Exception {
        // Given
        when(retailFileService.deleteRetailFile(testId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/v1/retail-files/{id}", testId))
                .andExpect(status().isNotFound());

        verify(retailFileService, times(1)).deleteRetailFile(testId);
    }

    @Test
    void markFileAsProcessed_ShouldReturn200_WhenFileExists() throws Exception {
        // Given
        testRetailFile.setStatus(FileProcessingStatus.COMPLETED);
        when(retailFileService.updateFileStatus(testId, FileProcessingStatus.COMPLETED)).thenReturn(testRetailFile);

        // When & Then
        mockMvc.perform(patch("/v1/retail-files/{id}/status", testId)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(retailFileService, times(1)).updateFileStatus(testId, FileProcessingStatus.COMPLETED);
    }

    @Test
    void createRetailFile_ShouldReturn400_WhenStoreNumberIsNull() throws Exception {
        // Given
        CreateRetailFileRequest invalidRequest = new CreateRetailFileRequest();
        invalidRequest.setFileName("test_file.csv");
        invalidRequest.setFileUrl("https://example.com/test_file.csv");
        invalidRequest.setFileSize(1024L);
        invalidRequest.setStoreNumber(null); // Missing required field
        invalidRequest.setChainId("CHAIN001");

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(retailFileService, never()).createRetailFile(any(CreateRetailFileRequest.class));
    }

    @Test
    void createRetailFile_ShouldReturn400_WhenChainIdIsNull() throws Exception {
        // Given
        CreateRetailFileRequest invalidRequest = new CreateRetailFileRequest();
        invalidRequest.setFileName("test_file.csv");
        invalidRequest.setFileUrl("https://example.com/test_file.csv");
        invalidRequest.setFileSize(1024L);
        invalidRequest.setStoreNumber(123);
        invalidRequest.setChainId(null); // Missing required field

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(retailFileService, never()).createRetailFile(any(CreateRetailFileRequest.class));
    }

    @Test
    void createRetailFile_ShouldReturn400_WhenChainIdExceedsMaxLength() throws Exception {
        // Given
        CreateRetailFileRequest invalidRequest = new CreateRetailFileRequest();
        invalidRequest.setFileName("test_file.csv");
        invalidRequest.setFileUrl("https://example.com/test_file.csv");
        invalidRequest.setFileSize(1024L);
        invalidRequest.setStoreNumber(123);
        invalidRequest.setChainId("CHAIN_ID_TOO_LONG_EXCEEDS_20_CHARS"); // Exceeds 20 characters

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(retailFileService, never()).createRetailFile(any(CreateRetailFileRequest.class));
    }

    @Test
    void createRetailFile_ShouldReturn400_WhenStoreNumberIsMissing() throws Exception {
        // Given
        CreateRetailFileRequest invalidRequest = new CreateRetailFileRequest();
        invalidRequest.setFileName("test_file.csv");
        invalidRequest.setFileUrl("https://example.com/test_file.csv");
        invalidRequest.setFileSize(1024L);
        // storeNumber not set (null)
        invalidRequest.setChainId("CHAIN001");

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(retailFileService, never()).createRetailFile(any(CreateRetailFileRequest.class));
    }

    @Test
    void createRetailFile_ShouldReturn400_WhenChainIdIsMissing() throws Exception {
        // Given
        CreateRetailFileRequest invalidRequest = new CreateRetailFileRequest();
        invalidRequest.setFileName("test_file.csv");
        invalidRequest.setFileUrl("https://example.com/test_file.csv");
        invalidRequest.setFileSize(1024L);
        invalidRequest.setStoreNumber(123);
        // chainId not set (null)

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(retailFileService, never()).createRetailFile(any(CreateRetailFileRequest.class));
    }
}