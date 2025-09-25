package com.avivse.storeservice.repository;

import com.avivse.storeservice.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StoreRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StoreRepository storeRepository;

    private Store testStore1;
    private Store testStore2;

    @BeforeEach
    void setUp() {
        testStore1 = new Store();
        testStore1.setStoreNumber(123);
        testStore1.setStoreType("MAIN");
        testStore1.setStoreName("Main Store");
        testStore1.setChainId("CHAIN001");
        testStore1.setSubChainId(1);
        testStore1.setCreatedBy("testuser");

        testStore2 = new Store();
        testStore2.setStoreNumber(456);
        testStore2.setStoreType("OUTLET");
        testStore2.setStoreName("Outlet Store");
        testStore2.setChainId("CHAIN002");
        testStore2.setSubChainId(2);
        testStore2.setCreatedBy("testuser2");
    }

    @Test
    void save_ShouldPersistStore() {
        Store savedStore = storeRepository.save(testStore1);
        entityManager.flush();

        assertNotNull(savedStore.getId());
        assertNotNull(savedStore.getCreatedAt());
        assertNotNull(savedStore.getUpdatedAt());
        assertEquals(0, savedStore.getVersion());
        assertEquals("CHAIN001", savedStore.getChainId());
        assertEquals(Integer.valueOf(123), savedStore.getStoreNumber());
    }

    @Test
    void save_ShouldEnforceUniqueConstraint() {
        storeRepository.save(testStore1);
        entityManager.flush();

        Store duplicateStore = new Store();
        duplicateStore.setStoreNumber(123);
        duplicateStore.setChainId("CHAIN001");
        duplicateStore.setStoreType("DIFFERENT");
        duplicateStore.setStoreName("Different Name");
        duplicateStore.setSubChainId(99);

        assertThrows(RuntimeException.class, () -> {
            storeRepository.save(duplicateStore);
            entityManager.flush();
        });
    }

    @Test
    void findByChainIdAndStoreNumber_ShouldReturnStore_WhenExists() {
        entityManager.persistAndFlush(testStore1);

        Optional<Store> result = storeRepository.findByChainIdAndStoreNumber("CHAIN001", 123);

        assertTrue(result.isPresent());
        assertEquals("Main Store", result.get().getStoreName());
        assertEquals("MAIN", result.get().getStoreType());
    }

    @Test
    void findByChainIdAndStoreNumber_ShouldReturnEmpty_WhenNotExists() {
        entityManager.persistAndFlush(testStore1);

        Optional<Store> result = storeRepository.findByChainIdAndStoreNumber("CHAIN999", 123);

        assertFalse(result.isPresent());
    }

    @Test
    void existsByChainIdAndStoreNumber_ShouldReturnTrue_WhenExists() {
        entityManager.persistAndFlush(testStore1);

        boolean exists = storeRepository.existsByChainIdAndStoreNumber("CHAIN001", 123);

        assertTrue(exists);
    }

    @Test
    void existsByChainIdAndStoreNumber_ShouldReturnFalse_WhenNotExists() {
        entityManager.persistAndFlush(testStore1);

        boolean exists = storeRepository.existsByChainIdAndStoreNumber("CHAIN999", 123);

        assertFalse(exists);
    }

    @Test
    void findWithFilters_ShouldReturnAllStores_WhenNoFilters() {
        entityManager.persistAndFlush(testStore1);
        entityManager.persistAndFlush(testStore2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Store> result = storeRepository.findWithFilters(null, null, null, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findWithFilters_ShouldFilterByChainId() {
        entityManager.persistAndFlush(testStore1);
        entityManager.persistAndFlush(testStore2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Store> result = storeRepository.findWithFilters("CHAIN001", null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Main Store", result.getContent().get(0).getStoreName());
    }

    @Test
    void findWithFilters_ShouldFilterByStoreType() {
        entityManager.persistAndFlush(testStore1);
        entityManager.persistAndFlush(testStore2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Store> result = storeRepository.findWithFilters(null, "OUTLET", null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Outlet Store", result.getContent().get(0).getStoreName());
    }

    @Test
    void findWithFilters_ShouldFilterBySubChainId() {
        entityManager.persistAndFlush(testStore1);
        entityManager.persistAndFlush(testStore2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Store> result = storeRepository.findWithFilters(null, null, 2, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Outlet Store", result.getContent().get(0).getStoreName());
    }

    @Test
    void findWithFilters_ShouldFilterByMultipleCriteria() {
        entityManager.persistAndFlush(testStore1);
        entityManager.persistAndFlush(testStore2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Store> result = storeRepository.findWithFilters("CHAIN001", "MAIN", 1, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Main Store", result.getContent().get(0).getStoreName());
    }

    @Test
    void findWithFilters_ShouldReturnEmpty_WhenNoMatches() {
        entityManager.persistAndFlush(testStore1);
        entityManager.persistAndFlush(testStore2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Store> result = storeRepository.findWithFilters("NONEXISTENT", null, null, pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findWithFilters_ShouldSupportPagination() {
        for (int i = 0; i < 5; i++) {
            Store store = new Store();
            store.setStoreNumber(i);
            store.setStoreType("TEST");
            store.setStoreName("Test Store " + i);
            store.setChainId("TEST_CHAIN");
            store.setSubChainId(1);
            entityManager.persistAndFlush(store);
        }

        Pageable pageable = PageRequest.of(0, 2);
        Page<Store> result = storeRepository.findWithFilters("TEST_CHAIN", null, null, pageable);

        assertEquals(5, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalPages());
    }
}