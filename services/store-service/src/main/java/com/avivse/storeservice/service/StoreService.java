package com.avivse.storeservice.service;

import com.avivse.storeservice.dto.StoreCreateDTO;
import com.avivse.storeservice.dto.StoreUpdateDTO;
import com.avivse.storeservice.entity.Store;
import com.avivse.storeservice.exception.StoreAlreadyExistsException;
import com.avivse.storeservice.exception.StoreNotFoundException;
import com.avivse.storeservice.mapper.StoreMapper;
import com.avivse.storeservice.repository.StoreRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final MeterRegistry meterRegistry;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, MeterRegistry meterRegistry) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.meterRegistry = meterRegistry;
    }

    public Store createStore(StoreCreateDTO createDTO) {
        logger.info("Creating new store with chainId: {} and storeNumber: {}",
                   createDTO.getChainId(), createDTO.getStoreNumber());

        if (storeRepository.existsByChainIdAndStoreNumber(createDTO.getChainId(), createDTO.getStoreNumber())) {
            throw new StoreAlreadyExistsException(
                String.format("Store already exists with chainId '%s' and storeNumber '%d'",
                            createDTO.getChainId(), createDTO.getStoreNumber()));
        }

        try {
            Store store = storeMapper.toEntity(createDTO);
            Store savedStore = storeRepository.save(store);

            meterRegistry.counter("store.created", "chain_id", createDTO.getChainId()).increment();
            logger.info("Successfully created store with ID: {}", savedStore.getId());

            return savedStore;
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating store", e);
            throw new StoreAlreadyExistsException(
                String.format("Store already exists with chainId '%s' and storeNumber '%d'",
                            createDTO.getChainId(), createDTO.getStoreNumber()), e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Store> findById(UUID id) {
        logger.debug("Finding store by ID: {}", id);
        return storeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Store> findByChainIdAndStoreNumber(String chainId, Integer storeNumber) {
        logger.debug("Finding store by chainId: {} and storeNumber: {}", chainId, storeNumber);
        return storeRepository.findByChainIdAndStoreNumber(chainId, storeNumber);
    }

    @Transactional(readOnly = true)
    public Page<Store> findAllWithFilters(String chainId, String storeType, Integer subChainId,
                                         int page, int size) {
        logger.debug("Finding stores with filters - chainId: {}, storeType: {}, subChainId: {}, page: {}, size: {}",
                    chainId, storeType, subChainId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return storeRepository.findWithFilters(chainId, storeType, subChainId, pageable);
    }

    public Store updateStore(UUID id, StoreUpdateDTO updateDTO) {
        logger.info("Updating store with ID: {}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException("Store not found with id: " + id));

        storeMapper.updateEntityFromDTO(store, updateDTO);

        Store updatedStore = storeRepository.save(store);
        meterRegistry.counter("store.updated", "chain_id", store.getChainId()).increment();
        logger.info("Successfully updated store with ID: {}", id);

        return updatedStore;
    }

    public boolean deleteStore(UUID id) {
        logger.info("Deleting store with ID: {}", id);

        if (!storeRepository.existsById(id)) {
            return false;
        }

        Optional<Store> store = storeRepository.findById(id);
        storeRepository.deleteById(id);

        store.ifPresent(s -> meterRegistry.counter("store.deleted", "chain_id", s.getChainId()).increment());
        logger.info("Successfully deleted store with ID: {}", id);

        return true;
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return storeRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByChainIdAndStoreNumber(String chainId, Integer storeNumber) {
        return storeRepository.existsByChainIdAndStoreNumber(chainId, storeNumber);
    }
}