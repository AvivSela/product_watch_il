package com.avivse.retailfileservice.integration;

import com.avivse.retailfileservice.client.StoreServiceClient;
import com.avivse.retailfileservice.dto.CreateRetailFileRequest;
import com.avivse.retailfileservice.dto.UpdateRetailFileRequest;
import com.avivse.retailfileservice.entity.RetailFile;
import com.avivse.retailfileservice.enums.FileProcessingStatus;
import com.avivse.retailfileservice.repository.RetailFileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.Mockito.when;
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

    @MockBean
    private StoreServiceClient storeServiceClient;

    @BeforeEach
    void setUp() {
        retailFileRepository.deleteAll();

        // Mock store service client to return a UUID for any chainId and storeNumber
        when(storeServiceClient.getOrCreateStoreId("CHAIN001", 123))
                .thenReturn(UUID.randomUUID());
    }

    @Test
    @Transactional
    void createRetailFile_ShouldCreateAndPersistToDatabase() throws Exception {
        // Given
        CreateRetailFileRequest request = new CreateRetailFileRequest();
        request.setFileName("integration_test.csv");
        request.setFileUrl("https://example.com/integration_test.csv");
        request.setFileSize(1024L);
        request.setUploadDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        request.setStatus(FileProcessingStatus.PENDING);
        request.setStoreNumber(123);
        request.setChainId("CHAIN001");

        // When
        MvcResult result = mockMvc.perform(post("/v1/retail-files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.file_name").value("integration_test.csv"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        // Then - Verify in database
        String response = result.getResponse().getContentAsString();
        RetailFile responseFile = objectMapper.readValue(response, RetailFile.class);

        RetailFile savedFile = retailFileRepository.findById(responseFile.getId()).orElse(null);
        assertNotNull(savedFile);
        assertEquals("integration_test.csv", savedFile.getFileName());
        assertEquals(FileProcessingStatus.PENDING, savedFile.getStatus());
        assertEquals(1024L, savedFile.getFileSize());
        assertNotNull(savedFile.getId());
    }


    @Test
    @Transactional
    void listRetailFiles_ShouldApplyFilters() throws Exception {
        // Given
        createTestFile("file1.csv", FileProcessingStatus.PENDING);
        createTestFile("file2.csv", FileProcessingStatus.COMPLETED);
        createTestFile("file3.csv", FileProcessingStatus.PENDING);

        // When & Then - Filter by chainId
        mockMvc.perform(get("/v1/retail-files")
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.pagination.total").value(3));

        // When & Then - Filter by processing status
        mockMvc.perform(get("/v1/retail-files")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.pagination.total").value(2));
    }

    @Test
    @Transactional
    void updateRetailFile_ShouldUpdateInDatabase() throws Exception {
        // Given - Create file in database
        RetailFile originalFile = createTestFile("original.csv", FileProcessingStatus.PENDING);
        Long originalFileSize = originalFile.getFileSize();
        FileProcessingStatus originalStatus = originalFile.getStatus();

        UpdateRetailFileRequest updateRequest = new UpdateRetailFileRequest();
        updateRequest.setFileSize(4096L);
        updateRequest.setStatus(FileProcessingStatus.COMPLETED);

        // When
        mockMvc.perform(put("/v1/retail-files/{id}", originalFile.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file_size").value(4096))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.file_name").value("original.csv"));

        // Then - Verify in database
        RetailFile updatedFile = retailFileRepository.findById(originalFile.getId()).orElse(null);
        assertNotNull(updatedFile);
        assertEquals(4096L, updatedFile.getFileSize());
        assertEquals(FileProcessingStatus.COMPLETED, updatedFile.getStatus());
        assertEquals("original.csv", updatedFile.getFileName());

        // Verify the values actually changed
        assertNotEquals(originalFileSize, updatedFile.getFileSize());
        assertNotEquals(originalStatus, updatedFile.getStatus());
    }


    @Test
    @Transactional
    void deleteRetailFile_ShouldRemoveFromDatabase() throws Exception {
        // Given
        RetailFile file = createTestFile("delete_test.csv", FileProcessingStatus.PENDING);
        UUID fileId = file.getId();

        // When
        mockMvc.perform(delete("/v1/retail-files/{id}", fileId))
                .andExpect(status().isNoContent());

        // Then - Verify removed from database
        assertFalse(retailFileRepository.existsById(fileId));
    }


    // Helper method to create test files in database
    private RetailFile createTestFile(String fileName, FileProcessingStatus status) {
        RetailFile file = new RetailFile();
        file.setFileName(fileName);
        file.setFileUrl("https://example.com/" + fileName);
        file.setFileSize(1024L);
        file.setUploadDate(LocalDateTime.now());
        file.setStatus(status);
        return retailFileRepository.save(file);
    }
}