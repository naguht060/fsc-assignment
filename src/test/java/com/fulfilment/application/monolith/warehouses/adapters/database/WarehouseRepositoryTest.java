package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseRepositoryTest {

  @Inject WarehouseRepository repository;

  private String uniqueBusinessUnitCode;

  @BeforeEach
  @Transactional
  void setUp() {
    uniqueBusinessUnitCode = "MWH.REPO_TEST_" + System.currentTimeMillis();
  }

  @Test
  @Order(1)
  @Transactional
  void testCreateWarehouse() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = uniqueBusinessUnitCode;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    // When
    repository.create(warehouse);

    // Then
    Warehouse found = repository.findByBusinessUnitCode(uniqueBusinessUnitCode);
    assertNotNull(found);
    assertEquals(uniqueBusinessUnitCode, found.businessUnitCode);
    assertEquals("ZWOLLE-001", found.location);
    assertEquals(100, found.capacity);
    assertEquals(50, found.stock);
  }

  @Test
  @Order(2)
  @Transactional
  void testGetAllExcludesArchivedWarehouses() {
    // Given - create active warehouse
    Warehouse active = new Warehouse();
    active.businessUnitCode = uniqueBusinessUnitCode;
    active.location = "ZWOLLE-001";
    active.capacity = 100;
    active.stock = 50;
    active.createdAt = LocalDateTime.now();
    active.archivedAt = null;
    repository.create(active);

    // Create archived warehouse
    Warehouse archived = new Warehouse();
    archived.businessUnitCode = uniqueBusinessUnitCode + "_ARCHIVED";
    archived.location = "ZWOLLE-001";
    archived.capacity = 50;
    archived.stock = 25;
    archived.createdAt = LocalDateTime.now();
    archived.archivedAt = LocalDateTime.now();
    repository.create(archived);

    // When
    List<Warehouse> all = repository.getAll();

    // Then
    assertTrue(
        all.stream().anyMatch(w -> w.businessUnitCode.equals(uniqueBusinessUnitCode)),
        "Active warehouse should be included");
    assertFalse(
        all.stream().anyMatch(w -> w.businessUnitCode.equals(uniqueBusinessUnitCode + "_ARCHIVED")),
        "Archived warehouse should be excluded");
  }

  @Test
  @Order(3)
  @Transactional
  void testUpdateWarehouse() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = uniqueBusinessUnitCode;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    repository.create(warehouse);

    // When - update the warehouse
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 150;
    warehouse.stock = 75;
    repository.update(warehouse);

    // Then
    Warehouse updated = repository.findByBusinessUnitCode(uniqueBusinessUnitCode);
    assertNotNull(updated);
    assertEquals("AMSTERDAM-001", updated.location);
    assertEquals(150, updated.capacity);
    assertEquals(75, updated.stock);
  }

  @Test
  @Order(4)
  @Transactional
  void testUpdateThrowsExceptionWhenWarehouseNotFound() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NON_EXISTENT_CODE";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    // When/Then
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> repository.update(warehouse));
    assertTrue(
        exception.getMessage().contains("NON_EXISTENT_CODE"),
        "Exception should mention the business unit code");
  }

  @Test
  @Order(5)
  @Transactional
  void testUpdateOnlyUpdatesNonArchivedWarehouse() {
    // Given - create and archive a warehouse
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = uniqueBusinessUnitCode;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    repository.create(warehouse);

    // Archive it
    warehouse.archivedAt = LocalDateTime.now();
    repository.update(warehouse);

    // When - try to update archived warehouse
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 150;

    // Then - should throw exception because archived warehouses are filtered out
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> repository.update(warehouse));
    assertTrue(
        exception.getMessage().contains(uniqueBusinessUnitCode),
        "Exception should mention the business unit code");
  }

  @Test
  @Order(6)
  @Transactional
  void testRemoveWarehouse() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = uniqueBusinessUnitCode;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    repository.create(warehouse);

    // When
    repository.remove(warehouse);

    // Then - should not be found
    Warehouse found = repository.findByBusinessUnitCode(uniqueBusinessUnitCode);
    assertNull(found, "Removed warehouse should not be found");
  }

  @Test
  @Order(7)
  @Transactional
  void testRemoveNonExistentWarehouseDoesNothing() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NON_EXISTENT";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    // When/Then - should not throw exception
    assertDoesNotThrow(() -> repository.remove(warehouse));
  }

  @Test
  @Order(8)
  @Transactional
  void testFindByBusinessUnitCodeReturnsNullWhenNotFound() {
    // When
    Warehouse found = repository.findByBusinessUnitCode("NON_EXISTENT_CODE");

    // Then
    assertNull(found);
  }

  @Test
  @Order(9)
  @Transactional
  void testFindByBusinessUnitCodeExcludesArchivedWarehouses() {
    // Given - create and archive a warehouse
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = uniqueBusinessUnitCode;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    repository.create(warehouse);

    // Archive it
    warehouse.archivedAt = LocalDateTime.now();
    repository.update(warehouse);

    // When
    Warehouse found = repository.findByBusinessUnitCode(uniqueBusinessUnitCode);

    // Then - should return null because archived warehouses are filtered out
    assertNull(found, "Archived warehouse should not be found");
  }
}
