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
    void init() {
        code = "MWH.TEST_" + System.currentTimeMillis();
    }

    // ---------- CREATE ----------

    @Test
    @Transactional
    void createWarehouse_success() {
        Warehouse w = createActiveWarehouse(code);
        repository.create(w);

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
        assertNull(repository.findByBusinessUnitCode("UNKNOWN"));
    }

    @Test
    @Transactional
    void findByBusinessUnitCode_archivedWarehouse_returnsNull() {
        Warehouse w = createArchivedWarehouse(code);
        repository.create(w);

        Warehouse found = repository.findByBusinessUnitCode(code);
        assertNull(found);
    }

    // ---------- GET ALL ----------

    @Test
    @Transactional
    void getAll_returnsOnlyActiveWarehouses() {
        repository.create(createActiveWarehouse(code));
        repository.create(createArchivedWarehouse(code + "_ARCH"));

        List<Warehouse> all = repository.getAll();

        assertEquals(1, all.size());
        assertEquals(code, all.get(0).businessUnitCode);
    }

    @Test
    @Transactional
    void getAll_allArchived_returnsEmptyList() {
        repository.create(createArchivedWarehouse(code));

        List<Warehouse> all = repository.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    @Transactional
    void getAll_multipleActiveWarehouses() {
        repository.create(createActiveWarehouse(code));
        repository.create(createActiveWarehouse(code + "_2"));

        List<Warehouse> all = repository.getAll();
        assertEquals(2, all.size());
    }

    // ---------- UPDATE ----------

    @Test
    @Transactional
    void updateWarehouse_success() {
        Warehouse w = createActiveWarehouse(code);
        repository.create(w);

        w.location = "AMSTERDAM-001";
        w.capacity = 200;
        w.stock = 100;

        repository.update(w);

        Warehouse updated = repository.findByBusinessUnitCode(code);
        assertEquals("AMSTERDAM-001", updated.location);
        assertEquals(200, updated.capacity);
        assertEquals(100, updated.stock);
    }

    @Test
    @Transactional
    void updateWarehouse_noChanges_stillSucceeds() {
        Warehouse w = createActiveWarehouse(code);
        repository.create(w);

        assertDoesNotThrow(() -> repository.update(w));
    }

    @Test
    @Transactional
    void updateWarehouse_notFound_throwsException() {
        Warehouse w = createActiveWarehouse("UNKNOWN");

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> repository.update(w));
        assertTrue(ex.getMessage().contains("UNKNOWN"));
    }

    @Test
    @Transactional
    void updateWarehouse_archived_throwsException() {
        Warehouse w = createActiveWarehouse(code);
        repository.create(w);

        w.archivedAt = LocalDateTime.now();
        repository.update(w);

        w.location = "AMSTERDAM-001";

        assertThrows(IllegalStateException.class, () -> repository.update(w));
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
        Warehouse w = createActiveWarehouse(code);
        repository.create(w);

        repository.remove(w);

        assertNull(repository.findByBusinessUnitCode(code));
    }

    @Test
    @Transactional
    void removeWarehouse_notFound_doesNothing() {
        Warehouse w = createActiveWarehouse("UNKNOWN");
        assertDoesNotThrow(() -> repository.remove(w));
    }

    @Test
    @Transactional
    void removeWarehouse_archived_doesNothing() {
        Warehouse w = createArchivedWarehouse(code);
        repository.create(w);

        assertDoesNotThrow(() -> repository.remove(w));
    }

    @Test
    @Transactional
    void removeWarehouse_null_doesNothing() {
        assertDoesNotThrow(() -> repository.remove(null));
    }

    // ---------- HELPERS ----------

    private Warehouse createActiveWarehouse(String code) {
        Warehouse w = new Warehouse();
        w.businessUnitCode = code;
        w.location = "ZWOLLE-001";
        w.capacity = 100;
        w.stock = 50;
        w.createdAt = LocalDateTime.now();
        w.archivedAt = null;
        return w;
    }

    private Warehouse createArchivedWarehouse(String code) {
        Warehouse w = createActiveWarehouse(code);
        w.archivedAt = LocalDateTime.now();
        return w;
    }
}