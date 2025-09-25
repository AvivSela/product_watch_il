package com.avivse.retailfileservice.client;

import com.avivse.retailfileservice.dto.CreateStoreDto;
import com.avivse.retailfileservice.dto.StoreDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

@Component
public class StoreServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(StoreServiceClient.class);

    private final RestTemplate restTemplate;
    private final String storeServiceBaseUrl;

    public StoreServiceClient(RestTemplate restTemplate,
                             @Value("${app.store-service.base-url:http://localhost:9090}") String storeServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.storeServiceBaseUrl = storeServiceBaseUrl;
    }

    public Optional<StoreDto> getStoreByChainIdAndStoreNumber(String chainId, Integer storeNumber) {
        try {
            logger.debug("Getting store by chainId: {} and storeNumber: {}", chainId, storeNumber);

            String url = UriComponentsBuilder.fromUriString(storeServiceBaseUrl)
                    .path("/api/v1/stores/by-natural-key")
                    .queryParam("chainId", chainId)
                    .queryParam("storeNumber", storeNumber)
                    .toUriString();

            ResponseEntity<StoreDto> response = restTemplate.getForEntity(url, StoreDto.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.debug("Found existing store with ID: {}", response.getBody().getId());
                return Optional.of(response.getBody());
            }

            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            logger.debug("Store not found for chainId: {} and storeNumber: {}", chainId, storeNumber);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error getting store by chainId: {} and storeNumber: {}", chainId, storeNumber, e);
            throw new RuntimeException("Failed to get store from store service", e);
        }
    }

    public StoreDto createStore(CreateStoreDto createStoreDto) {
        try {
            logger.info("Creating store with chainId: {} and storeNumber: {}",
                       createStoreDto.getChainId(), createStoreDto.getStoreNumber());

            String url = storeServiceBaseUrl + "/api/v1/stores";

            ResponseEntity<StoreDto> response = restTemplate.postForEntity(url, createStoreDto, StoreDto.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                logger.info("Created new store with ID: {}", response.getBody().getId());
                return response.getBody();
            } else {
                throw new RuntimeException("Unexpected response when creating store");
            }
        } catch (HttpClientErrorException.Conflict e) {
            logger.warn("Store already exists for chainId: {} and storeNumber: {}, attempting to get existing store",
                       createStoreDto.getChainId(), createStoreDto.getStoreNumber());
            Optional<StoreDto> existingStore = getStoreByChainIdAndStoreNumber(
                    createStoreDto.getChainId(), createStoreDto.getStoreNumber());
            if (existingStore.isPresent()) {
                return existingStore.get();
            } else {
                throw new RuntimeException("Store exists but couldn't retrieve it", e);
            }
        } catch (Exception e) {
            logger.error("Error creating store with chainId: {} and storeNumber: {}",
                        createStoreDto.getChainId(), createStoreDto.getStoreNumber(), e);
            throw new RuntimeException("Failed to create store in store service", e);
        }
    }

    public UUID getOrCreateStoreId(String chainId, Integer storeNumber) {
        Optional<StoreDto> existingStore = getStoreByChainIdAndStoreNumber(chainId, storeNumber);

        if (existingStore.isPresent()) {
            return existingStore.get().getId();
        }

        CreateStoreDto createStoreDto = new CreateStoreDto();
        createStoreDto.setStoreNumber(storeNumber);
        createStoreDto.setChainId(chainId);

        StoreDto createdStore = createStore(createStoreDto);
        return createdStore.getId();
    }
}