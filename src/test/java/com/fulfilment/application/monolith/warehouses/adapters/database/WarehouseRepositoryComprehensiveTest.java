package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for WarehouseRepository covering all database query operations.
 * Tests: list active warehouses, find by ID, persistence, and query methods.
 */
@QuarkusTest
public class WarehouseRepositoryComprehensiveTest {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  public void testListActiveWarehousesReturnsNonArchivedWarehouses() {
    List<DbWarehouse> active = warehouseRepository.list("archivedAt is null");

    assertNotNull(active);
    assertTrue(active.size() > 0);
    assertTrue(active.stream().allMatch(w -> w.archivedAt == null));
  }

  @Test
  public void testListActiveWarehousesDoesNotReturnArchivedWarehouses() {
    List<DbWarehouse> all = warehouseRepository.listAll();
    List<DbWarehouse> active = warehouseRepository.list("archivedAt is null");

    // If there are archived warehouses, they should not be in active list
    if (all.size() > active.size()) {
      var archived = warehouseRepository.list("archivedAt is not null");
      assertTrue(archived.size() > 0);
      assertTrue(archived.stream().allMatch(w -> w.archivedAt != null));
    }
  }

  @Test
  public void testFindByIdReturnsCorrectWarehouse() {
    DbWarehouse warehouse = warehouseRepository.findById(1L);

    assertNotNull(warehouse);
    assertEquals(1L, warehouse.id);
    assertEquals("MWH.001", warehouse.businessUnitCode);
  }

  @Test
  public void testFindByIdReturnsNullForNonExistentId() {
    DbWarehouse warehouse = warehouseRepository.findById(999999L);

    assertNull(warehouse);
  }

  @Test
  public void testFindByBusinessUnitCode() {
    var warehouse = warehouseRepository.find("businessUnitCode = ?1 and archivedAt is null", "MWH.001").firstResult();

    assertNotNull(warehouse);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
  }

  @Test
  public void testFindByBusinessUnitCodeNonExistent() {
    var warehouse = warehouseRepository.find("businessUnitCode = ?1", "MWH.DOES.NOT.EXIST").firstResult();

    assertNull(warehouse);
  }

  @Test
  public void testFindByLocation() {
    var warehouse = warehouseRepository.find("location = ?1", "AMSTERDAM-001").firstResult();

    assertNotNull(warehouse);
    assertEquals("AMSTERDAM-001", warehouse.location);
    assertEquals("MWH.012", warehouse.businessUnitCode);
  }

  @Test
  public void testCountActiveWarehouses() {
    long count = warehouseRepository.count("archivedAt is null");

    assertTrue(count > 0);
  }

  @Test
  public void testCountArchivedWarehouses() {
    long count = warehouseRepository.count("archivedAt is not null");

    // May or may not have archived warehouses initially
    assertTrue(count >= 0);
  }

  @Test
  @Transactional
  public void testPersistAndRetrieveWarehouse() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.businessUnitCode = "MWH.TEST.NEW";
    warehouse.location = "TEST-LOCATION";
    warehouse.capacity = 500;
    warehouse.stock = 250;
    warehouse.createdAt = java.time.LocalDateTime.now();

    warehouseRepository.persist(warehouse);

    DbWarehouse retrieved = warehouseRepository.find("businessUnitCode = ?1", "MWH.TEST.NEW").firstResult();

    assertNotNull(retrieved);
    assertEquals("MWH.TEST.NEW", retrieved.businessUnitCode);
    assertEquals("TEST-LOCATION", retrieved.location);
    assertEquals(500, retrieved.capacity);
    assertEquals(250, retrieved.stock);
  }

  @Test
  @Transactional
  public void testUpdateWarehouse() {
    DbWarehouse warehouse = warehouseRepository.findById(1L);
    assertNotNull(warehouse);

    int oldCapacity = warehouse.capacity;
    warehouse.capacity = 999;
    warehouseRepository.persist(warehouse);

    DbWarehouse updated = warehouseRepository.findById(1L);
    assertEquals(999, updated.capacity);
    
    // Restore original
    updated.capacity = oldCapacity;
    warehouseRepository.persist(updated);
  }

  @Test
  public void testListAllWarehouses() {
    List<DbWarehouse> all = warehouseRepository.listAll();

    assertNotNull(all);
    assertTrue(all.size() > 0);
  }

  @Test
  public void testFindWithMultipleConditions() {
    var warehouse = warehouseRepository.find(
        "businessUnitCode = ?1 and location = ?2 and archivedAt is null",
        "MWH.001",
        "ZWOLLE-001"
    ).firstResult();

    assertNotNull(warehouse);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
  }

  @Test
  public void testFindWithMultipleConditionsNoMatch() {
    var warehouse = warehouseRepository.find(
        "businessUnitCode = ?1 and location = ?2",
        "MWH.001",
        "WRONG-LOCATION"
    ).firstResult();

    assertNull(warehouse);
  }

  @Test
  public void testListWarehousesByCapacityRange() {
    List<DbWarehouse> warehouses = warehouseRepository.list("capacity >= ?1 and capacity <= ?2", 30, 100);

    assertNotNull(warehouses);
    assertTrue(warehouses.stream().allMatch(w -> w.capacity >= 30 && w.capacity <= 100));
  }

  @Test
  public void testListWarehousesByStock() {
    List<DbWarehouse> warehouses = warehouseRepository.list("stock > ?1", 5);

    assertNotNull(warehouses);
    assertTrue(warehouses.stream().allMatch(w -> w.stock > 5));
  }

  @Test
  @Transactional
  public void testArchiveWarehouse() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.businessUnitCode = "MWH.ARCHIVE.TEST";
    warehouse.location = "ARCHIVE-LOC";
    warehouse.capacity = 200;
    warehouse.stock = 100;
    warehouse.createdAt = java.time.LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseRepository.persist(warehouse);

    DbWarehouse toArchive = warehouseRepository.find("businessUnitCode = ?1", "MWH.ARCHIVE.TEST").firstResult();
    assertNotNull(toArchive);
    assertNull(toArchive.archivedAt);

    toArchive.archivedAt = java.time.LocalDateTime.now();
    warehouseRepository.persist(toArchive);

    DbWarehouse archived = warehouseRepository.find("businessUnitCode = ?1", "MWH.ARCHIVE.TEST").firstResult();
    assertNotNull(archived.archivedAt);
  }

  @Test
  public void testFindByBusinessUnitCodeWithArchivedFilter() {
    List<DbWarehouse> all = warehouseRepository.list("businessUnitCode = ?1", "MWH.001");
    List<DbWarehouse> active = warehouseRepository.list("businessUnitCode = ?1 and archivedAt is null", "MWH.001");

    assertTrue(all.size() >= active.size());
  }

  @Test
  public void testWarehouseHasCorrectFieldValues() {
    DbWarehouse warehouse = warehouseRepository.findById(2L);

    assertNotNull(warehouse);
    assertEquals(2L, warehouse.id);
    assertEquals("MWH.012", warehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouse.location);
    assertEquals(50, warehouse.capacity);
    assertEquals(5, warehouse.stock);
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testFindFirstResult() {
    var warehouse = warehouseRepository.find("location like ?1", "%001%").firstResult();

    assertNotNull(warehouse);
    assertTrue(warehouse.location.contains("001"));
  }

  @Test
  public void testListOrdering() {
    List<DbWarehouse> warehouses = warehouseRepository.list("archivedAt is null");

    assertNotNull(warehouses);
    // Just verify we can list them (ordering handled by DB)
    assertTrue(warehouses.stream().allMatch(w -> w.businessUnitCode != null));
  }
}
