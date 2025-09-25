package com.avivse.storeservice.integration;

import com.avivse.storeservice.dto.StoreCreateDTO;
import com.avivse.storeservice.dto.StoreResponseDTO;
import com.avivse.storeservice.dto.StoreUpdateDTO;
import com.avivse.storeservice.entity.Store;
import com.avivse.storeservice.repository.StoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StoreIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private StoreCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/stores";

        createDTO = new StoreCreateDTO();
        createDTO.setStoreNumber(123);
        createDTO.setStoreType("MAIN");
        createDTO.setStoreName("Integration Test Store");
        createDTO.setChainId("CHAIN001");
        createDTO.setSubChainId(1);
        createDTO.setCreatedBy("integrationtest");

        storeRepository.deleteAll();
    }

    @Test
    void createStore_ShouldCreateStoreSuccessfully() {
        ResponseEntity<StoreResponseDTO> response = restTemplate.postForEntity(
                baseUrl, createDTO, StoreResponseDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        StoreResponseDTO responseDTO = response.getBody();
        assertNotNull(responseDTO.getId());
        assertEquals(123, responseDTO.getStoreNumber());
        assertEquals("MAIN", responseDTO.getStoreType());
        assertEquals("Integration Test Store", responseDTO.getStoreName());
        assertEquals("CHAIN001", responseDTO.getChainId());
        assertEquals(1, responseDTO.getSubChainId());
        assertEquals("integrationtest", responseDTO.getCreatedBy());
        assertNotNull(responseDTO.getCreatedAt());
        assertNotNull(responseDTO.getUpdatedAt());
        assertEquals(0, responseDTO.getVersion());

        assertEquals(1, storeRepository.count());
    }

    @Test
    void createStore_ShouldReturn409_WhenDuplicateStore() {
        restTemplate.postForEntity(baseUrl, createDTO, StoreResponseDTO.class);

        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
                baseUrl, createDTO, String.class);

        assertEquals(HttpStatus.CONFLICT, duplicateResponse.getStatusCode());
    }

    @Test
    void createStore_ShouldReturn400_WhenInvalidData() {
        StoreCreateDTO invalidDTO = new StoreCreateDTO();
        invalidDTO.setStoreNumber(null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidDTO, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, storeRepository.count());
    }

    @Test
    void getStoreById_ShouldReturnStore_WhenExists() {
        ResponseEntity<StoreResponseDTO> createResponse = restTemplate.postForEntity(
                baseUrl, createDTO, StoreResponseDTO.class);
        UUID storeId = createResponse.getBody().getId();

        ResponseEntity<StoreResponseDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + storeId, StoreResponseDTO.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(storeId, getResponse.getBody().getId());
    }

    @Test
    void getStoreById_ShouldReturn404_WhenNotExists() {
        UUID nonExistentId = UUID.randomUUID();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/" + nonExistentId, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getStoreByNaturalKey_ShouldReturnStore_WhenExists() {
        restTemplate.postForEntity(baseUrl, createDTO, StoreResponseDTO.class);

        String naturalKeyUrl = baseUrl + "/by-natural-key?chainId=CHAIN001&storeNumber=123";
        ResponseEntity<StoreResponseDTO> response = restTemplate.getForEntity(
                naturalKeyUrl, StoreResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CHAIN001", response.getBody().getChainId());
        assertEquals(123, response.getBody().getStoreNumber());
    }

    @Test
    void getStoreByNaturalKey_ShouldReturn404_WhenNotExists() {
        String naturalKeyUrl = baseUrl + "/by-natural-key?chainId=NONEXISTENT&storeNumber=999";
        ResponseEntity<String> response = restTemplate.getForEntity(
                naturalKeyUrl, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getStoreByNaturalKey_ShouldReturn400_WhenMissingParameters() {
        String naturalKeyUrl = baseUrl + "/by-natural-key?chainId=CHAIN001";
        ResponseEntity<String> response = restTemplate.getForEntity(
                naturalKeyUrl, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void listStores_ShouldReturnPaginatedResults() {
        restTemplate.postForEntity(baseUrl, createDTO, StoreResponseDTO.class);

        StoreCreateDTO secondStore = new StoreCreateDTO();
        secondStore.setStoreNumber(456);
        secondStore.setStoreType("OUTLET");
        secondStore.setStoreName("Second Store");
        secondStore.setChainId("CHAIN002");
        secondStore.setSubChainId(2);
        restTemplate.postForEntity(baseUrl, secondStore, StoreResponseDTO.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertTrue(responseBody.containsKey("data"));
        assertTrue(responseBody.containsKey("pagination"));

        @SuppressWarnings("unchecked")
        List<Object> data = (List<Object>) responseBody.get("data");
        assertEquals(2, data.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> pagination = (Map<String, Object>) responseBody.get("pagination");
        assertEquals(1, pagination.get("page"));
        assertEquals(20, pagination.get("size"));
        assertEquals(2, (Integer) pagination.get("total"));
    }

    @Test
    void listStores_ShouldApplyFilters() {
        restTemplate.postForEntity(baseUrl, createDTO, StoreResponseDTO.class);

        StoreCreateDTO secondStore = new StoreCreateDTO();
        secondStore.setStoreNumber(456);
        secondStore.setStoreType("OUTLET");
        secondStore.setStoreName("Second Store");
        secondStore.setChainId("CHAIN002");
        secondStore.setSubChainId(2);
        restTemplate.postForEntity(baseUrl, secondStore, StoreResponseDTO.class);

        String filteredUrl = baseUrl + "?chainId=CHAIN001";
        ResponseEntity<Map> response = restTemplate.getForEntity(filteredUrl, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Object> data = (List<Object>) response.getBody().get("data");
        assertEquals(1, data.size());
    }

    @Test
    void updateStore_ShouldUpdateSuccessfully() {
        ResponseEntity<StoreResponseDTO> createResponse = restTemplate.postForEntity(
                baseUrl, createDTO, StoreResponseDTO.class);
        UUID storeId = createResponse.getBody().getId();

        StoreUpdateDTO updateDTO = new StoreUpdateDTO();
        updateDTO.setStoreType("UPDATED");
        updateDTO.setStoreName("Updated Store Name");
        updateDTO.setLastModifiedBy("updater");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StoreUpdateDTO> updateEntity = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<StoreResponseDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + storeId, HttpMethod.PUT, updateEntity, StoreResponseDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("UPDATED", updateResponse.getBody().getStoreType());
        assertEquals("Updated Store Name", updateResponse.getBody().getStoreName());
        assertEquals("updater", updateResponse.getBody().getLastModifiedBy());
    }

    @Test
    void updateStore_ShouldReturn404_WhenNotExists() {
        UUID nonExistentId = UUID.randomUUID();

        StoreUpdateDTO updateDTO = new StoreUpdateDTO();
        updateDTO.setStoreType("UPDATED");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StoreUpdateDTO> updateEntity = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId, HttpMethod.PUT, updateEntity, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteStore_ShouldDeleteSuccessfully() {
        ResponseEntity<StoreResponseDTO> createResponse = restTemplate.postForEntity(
                baseUrl, createDTO, StoreResponseDTO.class);
        UUID storeId = createResponse.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + storeId, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        assertEquals(0, storeRepository.count());
    }

    @Test
    void deleteStore_ShouldReturn404_WhenNotExists() {
        UUID nonExistentId = UUID.randomUUID();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void fullWorkflow_ShouldWorkEndToEnd() {
        ResponseEntity<StoreResponseDTO> createResponse = restTemplate.postForEntity(
                baseUrl, createDTO, StoreResponseDTO.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        UUID storeId = createResponse.getBody().getId();

        ResponseEntity<StoreResponseDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + storeId, StoreResponseDTO.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        String naturalKeyUrl = baseUrl + "/by-natural-key?chainId=CHAIN001&storeNumber=123";
        ResponseEntity<StoreResponseDTO> naturalKeyResponse = restTemplate.getForEntity(
                naturalKeyUrl, StoreResponseDTO.class);
        assertEquals(HttpStatus.OK, naturalKeyResponse.getStatusCode());

        StoreUpdateDTO updateDTO = new StoreUpdateDTO();
        updateDTO.setStoreType("PREMIUM");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StoreUpdateDTO> updateEntity = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<StoreResponseDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + storeId, HttpMethod.PUT, updateEntity, StoreResponseDTO.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("PREMIUM", updateResponse.getBody().getStoreType());

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + storeId, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getAfterDeleteResponse = restTemplate.getForEntity(
                baseUrl + "/" + storeId, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getAfterDeleteResponse.getStatusCode());
    }
}