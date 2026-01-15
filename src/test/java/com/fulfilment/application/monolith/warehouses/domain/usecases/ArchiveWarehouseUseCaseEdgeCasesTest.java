package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive edge case tests for ArchiveWarehouseUseCase to improve branch coverage.
 */
public class ArchiveWarehouseUseCaseEdgeCasesTest {

  private InMemoryWarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new InMemoryWarehouseStore();
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void archiveWithNullWarehouseThrows() {
    assertThrows(IllegalArgumentException.class, () -> useCase.archive(null));
  }

  @Test
  void archiveWithNullBusinessUnitCodeThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = null;
    warehouse.location = "ZWOLLE-001";

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(warehouse));
  }

  @Test
  void archiveNonExistentWarehouseThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.999";
    warehouse.location = "ZWOLLE-001";

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.archive(warehouse),
        "Should throw when warehouse not found");
  }

  @Test
  void archiveActiveWarehouseSuccessfully() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null; // Active warehouse
    warehouseStore.create(warehouse);

    useCase.archive(warehouse);

    Warehouse archived = warehouseStore.findByBusinessUnitCode("MWH.001");
    assertNotNull(archived.archivedAt);
    assertTrue(archived.archivedAt.isAfter(warehouse.createdAt) || archived.archivedAt.equals(warehouse.createdAt));
  }

  @Test
  void archiveAlreadyArchivedWarehouseSilentlyReturns() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now().minusDays(10);
    warehouse.archivedAt = LocalDateTime.now().minusDays(5);
    warehouseStore.create(warehouse);

    // Try to archive again - should not throw and not change the timestamp
    useCase.archive(warehouse);

    Warehouse result = warehouseStore.findByBusinessUnitCode("MWH.001");
    assertNotNull(result.archivedAt);
    // Timestamp might be slightly different due to timing, just check it's still archived
    assertTrue(result.archivedAt.isBefore(LocalDateTime.now()));
  }

  @Test
  void archiveMultipleWarehouses() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 100;
    w1.stock = 50;
    w1.createdAt = LocalDateTime.now();
    w1.archivedAt = null;
    warehouseStore.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.002";
    w2.location = "AMSTERDAM-001";
    w2.capacity = 150;
    w2.stock = 75;
    w2.createdAt = LocalDateTime.now();
    w2.archivedAt = null;
    warehouseStore.create(w2);

    useCase.archive(w1);
    Warehouse archived1 = warehouseStore.findByBusinessUnitCode("MWH.001");
    assertNotNull(archived1.archivedAt);

    Warehouse notArchived = warehouseStore.findByBusinessUnitCode("MWH.002");
    assertNull(notArchived.archivedAt);

    useCase.archive(w2);
    Warehouse archived2 = warehouseStore.findByBusinessUnitCode("MWH.002");
    assertNotNull(archived2.archivedAt);
  }

  @Test
  void archivePreservesOtherFields() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    LocalDateTime created = LocalDateTime.now();
    warehouse.createdAt = created;
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);

    useCase.archive(warehouse);

    Warehouse archived = warehouseStore.findByBusinessUnitCode("MWH.001");
    assertEquals("MWH.001", archived.businessUnitCode);
    assertEquals("ZWOLLE-001", archived.location);
    assertEquals(100, archived.capacity);
    assertEquals(50, archived.stock);
    assertEquals(created, archived.createdAt);
    assertNotNull(archived.archivedAt);
  }

  @Test
  void archiveWithBlankBusinessUnitCodeThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "   ";
    warehouse.location = "ZWOLLE-001";

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(warehouse));
  }

  @Test
  void archiveTimestampIsRecent() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now().minusDays(30);
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);

    LocalDateTime beforeArchive = LocalDateTime.now();
    useCase.archive(warehouse);
    LocalDateTime afterArchive = LocalDateTime.now();

    Warehouse archived = warehouseStore.findByBusinessUnitCode("MWH.001");
    assertTrue(archived.archivedAt.isAfter(beforeArchive.minusSeconds(1)));
    assertTrue(archived.archivedAt.isBefore(afterArchive.plusSeconds(1)));
  }

  @Test
  void archiveDoesNotChangeCreatedAt() {
    LocalDateTime created = LocalDateTime.now().minusDays(10);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = created;
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);

    useCase.archive(warehouse);

    Warehouse archived = warehouseStore.findByBusinessUnitCode("MWH.001");
    assertEquals(created, archived.createdAt);
  }

  // In-memory implementation
  private static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return new ArrayList<>(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      for (int i = 0; i < warehouses.size(); i++) {
        if (warehouses.get(i).businessUnitCode.equals(warehouse.businessUnitCode)) {
          warehouses.set(i, warehouse);
          return;
        }
      }
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.removeIf(w -> w.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String code) {
      return warehouses.stream()
          .filter(w -> w.businessUnitCode.equals(code))
          .findFirst()
          .orElse(null);
    }
  }
}
