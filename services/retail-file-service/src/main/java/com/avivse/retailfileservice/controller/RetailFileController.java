package com.avivse.retailfileservice.controller;

import com.avivse.retailfileservice.dto.CreateRetailFileRequest;
import com.avivse.retailfileservice.dto.UpdateRetailFileRequest;
import com.avivse.retailfileservice.entity.RetailFile;
import com.avivse.retailfileservice.exception.RetailFileNotFoundException;
import com.avivse.retailfileservice.service.RetailFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Retail Files", description = "Operations for managing retail file records")
@RestController
@RequestMapping("/v1/retail-files")
@CrossOrigin(origins = "*")
public class RetailFileController {

    private final RetailFileService retailFileService;

    @Autowired
    public RetailFileController(RetailFileService retailFileService) {
        this.retailFileService = retailFileService;
    }

    /**
     * POST /v1/retail-files - Create a new retail file record
     */
    @Operation(summary = "Create a new retail file record", description = "Creates a new retail file record in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<RetailFile> createRetailFile(@Valid @RequestBody CreateRetailFileRequest request) {
        RetailFile createdFile = retailFileService.createRetailFile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFile);
    }

    /**
     * GET /v1/retail-files/{id} - Get retail file by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RetailFile> getRetailFileById(@PathVariable UUID id) {
        RetailFile retailFile = retailFileService.findById(id)
                .orElseThrow(() -> RetailFileNotFoundException.forId(id.toString()));

        return ResponseEntity.ok(retailFile);
    }

    /**
     * GET /v1/retail-files - List retail files with optional filters and pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listRetailFiles(
            @RequestParam(required = false) String chainId,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        // Validate page and limit
        if (page < 1) page = 1;
        if (limit < 1 || limit > 100) limit = 20;

        Page<RetailFile> result = retailFileService.findAllWithFilters(
                chainId, storeId, isProcessed, page, limit);

        // Build response according to API specification
        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getContent());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", result.getTotalElements());
        pagination.put("pages", result.getTotalPages());
        response.put("pagination", pagination);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /v1/retail-files/{id} - Update retail file
     */
    @PutMapping("/{id}")
    public ResponseEntity<RetailFile> updateRetailFile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRetailFileRequest request) {

        RetailFile result = retailFileService.updateRetailFile(id, request);

        if (result == null) {
            throw RetailFileNotFoundException.forId(id.toString());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /v1/retail-files/{id} - Delete retail file
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRetailFile(@PathVariable UUID id) {
        boolean deleted = retailFileService.deleteRetailFile(id);

        if (!deleted) {
            throw RetailFileNotFoundException.forId(id.toString());
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /v1/retail-files/{id}/process - Mark file as processed
     */
    @PatchMapping("/{id}/process")
    public ResponseEntity<RetailFile> markFileAsProcessed(@PathVariable UUID id) {
        RetailFile result = retailFileService.markAsProcessed(id);

        if (result == null) {
            throw RetailFileNotFoundException.forId(id.toString());
        }

        return ResponseEntity.ok(result);
    }
}