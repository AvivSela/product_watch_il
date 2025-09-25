package com.avivse.storeservice.controller;

import com.avivse.storeservice.dto.StoreCreateDTO;
import com.avivse.storeservice.dto.StoreResponseDTO;
import com.avivse.storeservice.dto.StoreUpdateDTO;
import com.avivse.storeservice.entity.Store;
import com.avivse.storeservice.mapper.StoreMapper;
import com.avivse.storeservice.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@Validated
@Tag(name = "Store Management", description = "Operations for managing stores")
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;
    private final StoreMapper storeMapper;

    public StoreController(StoreService storeService, StoreMapper storeMapper) {
        this.storeService = storeService;
        this.storeMapper = storeMapper;
    }

    @PostMapping
    @Operation(summary = "Create a new store", description = "Creates a new store with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Store created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Store already exists with the same chainId and storeNumber")
    })
    public ResponseEntity<StoreResponseDTO> createStore(
            @Valid @RequestBody StoreCreateDTO createDTO,
            @RequestHeader(value = "X-Service-Name", required = false) String serviceName) {
        logger.info("Creating store with chainId: {} and storeNumber: {} from service: {}",
                   createDTO.getChainId(), createDTO.getStoreNumber(), serviceName);

        // Set createdBy based on service header, default to "unknown" if not provided
        createDTO.setCreatedBy(serviceName != null && !serviceName.trim().isEmpty() ? serviceName : "unknown");

        Store createdStore = storeService.createStore(createDTO);
        StoreResponseDTO response = storeMapper.toResponseDTO(createdStore);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get store by ID", description = "Retrieves a store by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Store found"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<StoreResponseDTO> getStoreById(
            @Parameter(description = "Store UUID") @PathVariable UUID id) {
        logger.debug("Getting store by ID: {}", id);

        Optional<Store> store = storeService.findById(id);
        if (store.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StoreResponseDTO response = storeMapper.toResponseDTO(store.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-natural-key")
    @Operation(summary = "Get store by natural key", description = "Retrieves a store by its chainId and storeNumber")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Store found"),
        @ApiResponse(responseCode = "400", description = "Missing required parameters"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<StoreResponseDTO> getStoreByNaturalKey(
            @Parameter(description = "Chain ID", required = true)
            @RequestParam("chain_id") @NotBlank @Size(max = 20) String chainId,
            @Parameter(description = "Store number", required = true)
            @RequestParam("store_number") @NotNull Integer storeNumber) {
        logger.debug("Getting store by chainId: {} and storeNumber: {}", chainId, storeNumber);

        Optional<Store> store = storeService.findByChainIdAndStoreNumber(chainId, storeNumber);
        if (store.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StoreResponseDTO response = storeMapper.toResponseDTO(store.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List stores", description = "Retrieves a paginated list of stores with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stores retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> listStores(
            @Parameter(description = "Filter by chain ID")
            @RequestParam(value = "chain_id", required = false) String chainId,
            @Parameter(description = "Filter by store type")
            @RequestParam(value = "store_type", required = false) String storeType,
            @Parameter(description = "Filter by sub-chain ID")
            @RequestParam(value = "sub_chain_id", required = false) Integer subChainId,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") @Positive int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Positive int size) {

        logger.debug("Listing stores - page: {}, size: {}, chainId: {}, storeType: {}, subChainId: {}",
                    page, size, chainId, storeType, subChainId);

        Page<Store> storePage = storeService.findAllWithFilters(chainId, storeType, subChainId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("data", storePage.getContent().stream()
                .map(storeMapper::toResponseDTO)
                .toList());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", storePage.getNumber() + 1);
        pagination.put("size", storePage.getSize());
        pagination.put("total", storePage.getTotalElements());
        pagination.put("totalPages", storePage.getTotalPages());
        response.put("pagination", pagination);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update store", description = "Updates an existing store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Store updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<StoreResponseDTO> updateStore(
            @Parameter(description = "Store UUID") @PathVariable UUID id,
            @Valid @RequestBody StoreUpdateDTO updateDTO) {
        logger.info("Updating store with ID: {}", id);

        Store updatedStore = storeService.updateStore(id, updateDTO);
        StoreResponseDTO response = storeMapper.toResponseDTO(updatedStore);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete store", description = "Deletes a store by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Store deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<Void> deleteStore(
            @Parameter(description = "Store UUID") @PathVariable UUID id) {
        logger.info("Deleting store with ID: {}", id);

        boolean deleted = storeService.deleteStore(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}