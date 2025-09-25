package com.avivse.retailfileservice.client;

import com.avivse.retailfileservice.dto.CreateStoreDto;
import com.avivse.retailfileservice.dto.StoreDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private StoreServiceClient storeServiceClient;

    private final String storeServiceBaseUrl = "http://localhost:9090";

    @BeforeEach
    void setUp() {
        storeServiceClient = new StoreServiceClient(restTemplate, storeServiceBaseUrl);
    }

    @Test
    void createStore_ShouldIncludeServiceNameHeader() {
        // Arrange
        CreateStoreDto createStoreDto = new CreateStoreDto();
        createStoreDto.setStoreNumber(123);
        createStoreDto.setChainId("CHAIN001");

        StoreDto expectedResponse = new StoreDto();
        expectedResponse.setId(UUID.randomUUID());
        expectedResponse.setStoreNumber(123);
        expectedResponse.setChainId("CHAIN001");

        ResponseEntity<StoreDto> mockResponse = new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);

        ArgumentCaptor<HttpEntity<CreateStoreDto>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(eq(storeServiceBaseUrl + "/api/v1/stores"), httpEntityCaptor.capture(), eq(StoreDto.class)))
                .thenReturn(mockResponse);

        // Act
        StoreDto result = storeServiceClient.createStore(createStoreDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(123, result.getStoreNumber());
        assertEquals("CHAIN001", result.getChainId());

        // Verify the HTTP entity contains the correct headers
        HttpEntity<CreateStoreDto> capturedEntity = httpEntityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertTrue(headers.containsKey("X-Service-Name"));
        assertEquals("retail-file-service", headers.getFirst("X-Service-Name"));

        // Verify the body is correct
        CreateStoreDto capturedBody = capturedEntity.getBody();
        assertNotNull(capturedBody);
        assertEquals(123, capturedBody.getStoreNumber());
        assertEquals("CHAIN001", capturedBody.getChainId());

        verify(restTemplate).postForEntity(eq(storeServiceBaseUrl + "/api/v1/stores"), any(HttpEntity.class), eq(StoreDto.class));
    }

    @Test
    void getOrCreateStoreId_ShouldSetServiceHeaderWhenCreating() {
        // Arrange
        String chainId = "CHAIN001";
        Integer storeNumber = 123;
        UUID expectedStoreId = UUID.randomUUID();

        // Mock 404 response for getStoreByChainIdAndStoreNumber
        when(restTemplate.getForEntity(anyString(), eq(StoreDto.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", new HttpHeaders(), new byte[0], null));

        // Mock successful create response
        StoreDto createdStore = new StoreDto();
        createdStore.setId(expectedStoreId);
        createdStore.setStoreNumber(storeNumber);
        createdStore.setChainId(chainId);

        ResponseEntity<StoreDto> createResponse = new ResponseEntity<>(createdStore, HttpStatus.CREATED);

        ArgumentCaptor<HttpEntity<CreateStoreDto>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(eq(storeServiceBaseUrl + "/api/v1/stores"), httpEntityCaptor.capture(), eq(StoreDto.class)))
                .thenReturn(createResponse);

        // Act
        UUID result = storeServiceClient.getOrCreateStoreId(chainId, storeNumber);

        // Assert
        assertEquals(expectedStoreId, result);

        // Verify the create request included the service header
        HttpEntity<CreateStoreDto> capturedEntity = httpEntityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertTrue(headers.containsKey("X-Service-Name"));
        assertEquals("retail-file-service", headers.getFirst("X-Service-Name"));

        verify(restTemplate).getForEntity(anyString(), eq(StoreDto.class));
        verify(restTemplate).postForEntity(eq(storeServiceBaseUrl + "/api/v1/stores"), any(HttpEntity.class), eq(StoreDto.class));
    }

    @Test
    void createStore_ShouldHandleConflictWithServiceHeader() {
        // Arrange
        CreateStoreDto createStoreDto = new CreateStoreDto();
        createStoreDto.setStoreNumber(123);
        createStoreDto.setChainId("CHAIN001");

        StoreDto existingStore = new StoreDto();
        existingStore.setId(UUID.randomUUID());
        existingStore.setStoreNumber(123);
        existingStore.setChainId("CHAIN001");

        // Mock conflict response for create
        ArgumentCaptor<HttpEntity<CreateStoreDto>> createCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(eq(storeServiceBaseUrl + "/api/v1/stores"), createCaptor.capture(), eq(StoreDto.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", new HttpHeaders(), new byte[0], null));

        // Mock successful get response
        ResponseEntity<StoreDto> getResponse = new ResponseEntity<>(existingStore, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(StoreDto.class)))
                .thenReturn(getResponse);

        // Act
        StoreDto result = storeServiceClient.createStore(createStoreDto);

        // Assert
        assertNotNull(result);
        assertEquals(existingStore.getId(), result.getId());

        // Verify the create request still included the service header
        HttpEntity<CreateStoreDto> capturedEntity = createCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertTrue(headers.containsKey("X-Service-Name"));
        assertEquals("retail-file-service", headers.getFirst("X-Service-Name"));

        verify(restTemplate).postForEntity(eq(storeServiceBaseUrl + "/api/v1/stores"), any(HttpEntity.class), eq(StoreDto.class));
        verify(restTemplate).getForEntity(anyString(), eq(StoreDto.class));
    }
}