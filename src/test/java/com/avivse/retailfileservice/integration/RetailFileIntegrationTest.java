package com.avivse.retailfileservice.integration;

import com.avivse.retailfileservice.dto.CreateRetailFileRequest;
import com.avivse.retailfileservice.dto.UpdateRetailFileRequest;
import com.avivse.retailfileservice.entity.RetailFile;
import com.avivse.retailfileservice.repository.RetailFileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RetailFileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RetailFileRepository retailFileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        retailFileRepository.deleteAll();
    }

    @Test
    @Transactional
    void createRetailFile_ShouldCreateAndPersistToDatabase() throws Exception {
        // Given
        CreateRetailFileRequest request = new CreateRetailFileRequest();
        request.setChainId("chain_001");
        request.setStoreId(123);
        request.setFileName("integration_test.csv");
        request.setFileUrl("https://example.com/integration_test.csv");
        request.setFileSize(1024L);
        request.setUploadDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        request.setIsProcessed(false);

        // When
        MvcResult result = mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chainId").value("chain_001"))
                .andExpect(jsonPath("$.fileName").value("integration_test.csv"))
                .andExpect(jsonPath("$.isProcessed").value(false))
                .andReturn();

        // Then - Verify in database
        String response = result.getResponse().getContentAsString();
        RetailFile responseFile = objectMapper.readValue(response, RetailFile.class);

        RetailFile savedFile = retailFileRepository.findById(responseFile.getId()).orElse(null);
        assertNotNull(savedFile);
        assertEquals("chain_001", savedFile.getChainId());
        assertEquals("integration_test.csv", savedFile.getFileName());
        assertEquals(false, savedFile.getIsProcessed());
        assertEquals(1024L, savedFile.getFileSize());
        assertNotNull(savedFile.getId());
    }

    @Test
    void createRetailFile_ShouldRejectInvalidData() throws Exception {
        // Given - missing required chainId
        CreateRetailFileRequest invalidRequest = new CreateRetailFileRequest();
        invalidRequest.setFileName("test.csv");
        invalidRequest.setFileUrl("https://example.com/test.csv");

        // When & Then
        mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify nothing was saved to database
        assertEquals(0, retailFileRepository.count());
    }

    @Test
    @Transactional
    void getRetailFile_ShouldReturnFileFromDatabase() throws Exception {
        // Given - Create file directly in database
        RetailFile file = createTestFile("chain_002", 456, "database_test.csv", true);

        // When & Then
        mockMvc.perform(get("/v1/retail-files/{id}", file.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(file.getId().toString()))
                .andExpect(jsonPath("$.chainId").value("chain_002"))
                .andExpect(jsonPath("$.fileName").value("database_test.csv"))
                .andExpect(jsonPath("$.isProcessed").value(true));
    }

    @Test
    void getNonExistentFile_ShouldReturn404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/v1/retail-files/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void listRetailFiles_ShouldReturnAllFilesWithPagination() throws Exception {
        // Given - Create multiple files in database
        createTestFile("chain_001", 123, "file1.csv", false);
        createTestFile("chain_001", 124, "file2.csv", true);
        createTestFile("chain_002", 125, "file3.csv", false);

        // When & Then
        mockMvc.perform(get("/v1/retail-files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.pagination.total").value(3))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(20));
    }

    @Test
    @Transactional
    void listRetailFiles_ShouldApplyFilters() throws Exception {
        // Given
        createTestFile("chain_001", 123, "file1.csv", false);
        createTestFile("chain_001", 124, "file2.csv", true);
        createTestFile("chain_002", 125, "file3.csv", false);

        // When & Then - Filter by chainId
        mockMvc.perform(get("/v1/retail-files")
                        .param("chainId", "chain_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.pagination.total").value(2));

        // When & Then - Filter by processing status
        mockMvc.perform(get("/v1/retail-files")
                        .param("isProcessed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.pagination.total").value(2));
    }

    @Test
    @Transactional
    void updateRetailFile_ShouldUpdateInDatabase() throws Exception {
        // Given - Create file in database
        RetailFile originalFile = createTestFile("chain_001", 123, "original.csv", false);
        Long originalFileSize = originalFile.getFileSize();
        Boolean originalProcessed = originalFile.getIsProcessed();

        UpdateRetailFileRequest updateRequest = new UpdateRetailFileRequest();
        updateRequest.setFileSize(4096L);
        updateRequest.setIsProcessed(true);

        // When
        mockMvc.perform(put("/v1/retail-files/{id}", originalFile.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileSize").value(4096))
                .andExpect(jsonPath("$.isProcessed").value(true))
                .andExpect(jsonPath("$.fileName").value("original.csv"));

        // Then - Verify in database
        RetailFile updatedFile = retailFileRepository.findById(originalFile.getId()).orElse(null);
        assertNotNull(updatedFile);
        assertEquals(4096L, updatedFile.getFileSize());
        assertEquals(true, updatedFile.getIsProcessed());
        assertEquals("original.csv", updatedFile.getFileName());

        // Verify the values actually changed
        assertNotEquals(originalFileSize, updatedFile.getFileSize());
        assertNotEquals(originalProcessed, updatedFile.getIsProcessed());
    }

    @Test
    @Transactional
    void markAsProcessed_ShouldUpdateProcessingStatus() throws Exception {
        // Given
        RetailFile file = createTestFile("chain_001", 123, "process_test.csv", false);

        // When
        mockMvc.perform(patch("/v1/retail-files/{id}/process", file.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isProcessed").value(true));

        // Then - Verify in database
        RetailFile updatedFile = retailFileRepository.findById(file.getId()).orElse(null);
        assertNotNull(updatedFile);
        assertEquals(true, updatedFile.getIsProcessed());
    }

    @Test
    @Transactional
    void deleteRetailFile_ShouldRemoveFromDatabase() throws Exception {
        // Given
        RetailFile file = createTestFile("chain_001", 123, "delete_test.csv", false);
        UUID fileId = file.getId();

        // When
        mockMvc.perform(delete("/v1/retail-files/{id}", fileId))
                .andExpect(status().isNoContent());

        // Then - Verify removed from database
        assertFalse(retailFileRepository.existsById(fileId));
    }

    @Test
    void deleteNonExistentFile_ShouldReturn404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/v1/retail-files/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    // Helper method to create test files in database
    private RetailFile createTestFile(String chainId, Integer storeId, String fileName, Boolean isProcessed) {
        RetailFile file = new RetailFile();
        file.setChainId(chainId);
        file.setStoreId(storeId);
        file.setFileName(fileName);
        file.setFileUrl("https://example.com/" + fileName);
        file.setFileSize(1024L);
        file.setUploadDate(LocalDateTime.now());
        file.setIsProcessed(isProcessed);
        return retailFileRepository.save(file);
    }
}