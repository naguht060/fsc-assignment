package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject WarehouseRepository repository;

    private String code;

    @BeforeEach
    void setUp() {
        code = "MWH.TEST_" + System.currentTimeMillis();
    }

    // ---------- CREATE ----------

    @Test
    @Transactional
    void createWarehouse_success() {
        Warehouse warehouse = createActiveWarehouse(code);
        repository.create(warehouse);

        Warehouse found = repository.findByBusinessUnitCode(code);
        assertNotNull(found);
        assertEquals(code, found.businessUnitCode);
    }

    @Test
    @Transactional
    void createWarehouse_null_throwsException() {
        assertThrows(NullPointerException.class, () -> repository.create(null));
    }

    // ---------- FIND ----------

    @Test
    @Transactional
    void findByBusinessUnitCode_notFound_returnsNull() {
        assertNull(repository.findByBusinessUnitCode("UNKNOWN_CODE"));
    }

    @Test
    @Transactional
    void findByBusinessUnitCode_archivedWarehouse_returnsNull() {
        Warehouse warehouse = createArchivedWarehouse(code);
        repository.create(warehouse);

        Warehouse found = repository.findByBusinessUnitCode(code);
        assertNull(found);
    }

    // ---------- GET ALL ----------

    @Test
    @Transactional
    void getAll_returnsOnlyActiveWarehouses() {
        Warehouse active = createActiveWarehouse(code);
        Warehouse archived = createArchivedWarehouse(code + "_ARCH");

        repository.create(active);
        repository.create(archived);

        List<Warehouse> all = repository.getAll();

        assertTrue(
                all.stream().anyMatch(w -> w.businessUnitCode.equals(code)),
                "Active warehouse should be included");

        assertFalse(
                all.stream().anyMatch(w -> w.businessUnitCode.equals(code + "_ARCH")),
                "Archived warehouse should be excluded");
    }

    @Test
    @Transactional
    void getAll_containsMultipleActiveWarehouses() {
        Warehouse w1 = createActiveWarehouse(code);
        Warehouse w2 = createActiveWarehouse(code + "_2");

        repository.create(w1);
        repository.create(w2);

        List<Warehouse> all = repository.getAll();

        assertTrue(all.stream().anyMatch(w -> w.businessUnitCode.equals(code)));
        assertTrue(all.stream().anyMatch(w -> w.businessUnitCode.equals(code + "_2")));
    }

    // ---------- UPDATE ----------

    @Test
    @Transactional
    void updateWarehouse_success() {
        Warehouse warehouse = createActiveWarehouse(code);
        repository.create(warehouse);

        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 200;
        warehouse.stock = 100;

        repository.update(warehouse);

        Warehouse updated = repository.findByBusinessUnitCode(code);
        assertNotNull(updated);
        assertEquals("AMSTERDAM-001", updated.location);
        assertEquals(200, updated.capacity);
        assertEquals(100, updated.stock);
    }

    @Test
    @Transactional
    void updateWarehouse_notFound_throwsException() {
        Warehouse warehouse = createActiveWarehouse("UNKNOWN");

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> repository.update(warehouse));

        assertTrue(ex.getMessage().contains("UNKNOWN"));
    }

    @Test
    @Transactional
    void updateWarehouse_archived_throwsException() {
        Warehouse warehouse = createActiveWarehouse(code);
        repository.create(warehouse);

        warehouse.archivedAt = LocalDateTime.now();
        repository.update(warehouse);

        warehouse.location = "AMSTERDAM-001";

        assertThrows(IllegalStateException.class, () -> repository.update(warehouse));
    }

    @Test
    @Transactional
    void updateWarehouse_null_throwsException() {
        assertThrows(NullPointerException.class, () -> repository.update(null));
    }

    // ---------- REMOVE ----------

    @Test
    @Transactional
    void removeWarehouse_success() {
        Warehouse warehouse = createActiveWarehouse(code);
        repository.create(warehouse);

        repository.remove(warehouse);

        Warehouse found = repository.findByBusinessUnitCode(code);
        assertNull(found);
    }

    @Test
    @Transactional
    void removeWarehouse_notFound_doesNothing() {
        Warehouse warehouse = createActiveWarehouse("UNKNOWN");
        assertDoesNotThrow(() -> repository.remove(warehouse));
    }

    @Test
    @Transactional
    void removeWarehouse_archived_doesNothing() {
        Warehouse warehouse = createArchivedWarehouse(code);
        repository.create(warehouse);

        assertDoesNotThrow(() -> repository.remove(warehouse));
    }

    @Test
    @Transactional
    void removeWarehouse_null_throwsException() {
        assertThrows(NullPointerException.class, () -> repository.remove(null));
    }

    // ---------- HELPERS ----------

    private Warehouse createActiveWarehouse(String businessUnitCode) {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        warehouse.createdAt = LocalDateTime.now();
        warehouse.archivedAt = null;
        return warehouse;
    }

    private Warehouse createArchivedWarehouse(String businessUnitCode) {
        Warehouse warehouse = createActiveWarehouse(businessUnitCode);
        warehouse.archivedAt = LocalDateTime.now();
        return warehouse;
    }
}