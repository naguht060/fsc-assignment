package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for DbWarehouse POJO to improve coverage of fields and conversion.
 */
public class DbWarehousePojoEdgeCasesTest {

  @Test
  public void testDbWarehouseDefaultConstructor() {
    DbWarehouse warehouse = new DbWarehouse();
    assertNull(warehouse.id);
    assertNull(warehouse.businessUnitCode);
    assertNull(warehouse.location);
    assertNull(warehouse.capacity);
    assertNull(warehouse.stock);
    assertNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testDbWarehouseFieldAssignment() {
    DbWarehouse warehouse = new DbWarehouse();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime archived = now.plusDays(30);

    warehouse.id = 1L;
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = now;
    warehouse.archivedAt = archived;

    assertEquals(1L, warehouse.id);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
    assertEquals(100, warehouse.capacity);
    assertEquals(50, warehouse.stock);
    assertEquals(now, warehouse.createdAt);
    assertEquals(archived, warehouse.archivedAt);
  }

  @Test
  public void testDbWarehouseToWarehouseConversion() {
    DbWarehouse dbWarehouse = new DbWarehouse();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime archived = now.plusDays(30);

    dbWarehouse.businessUnitCode = "MWH.001";
    dbWarehouse.location = "ZWOLLE-001";
    dbWarehouse.capacity = 100;
    dbWarehouse.stock = 50;
    dbWarehouse.createdAt = now;
    dbWarehouse.archivedAt = archived;

    Warehouse warehouse = dbWarehouse.toWarehouse();

    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
    assertEquals(100, warehouse.capacity);
    assertEquals(50, warehouse.stock);
    assertEquals(now, warehouse.createdAt);
    assertEquals(archived, warehouse.archivedAt);
  }

  @Test
  public void testDbWarehouseToWarehouseConversionWithNullFields() {
    DbWarehouse dbWarehouse = new DbWarehouse();
    Warehouse warehouse = dbWarehouse.toWarehouse();

    assertNull(warehouse.businessUnitCode);
    assertNull(warehouse.location);
    assertNull(warehouse.capacity);
    assertNull(warehouse.stock);
    assertNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testDbWarehouseZeroCapacity() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.capacity = 0;
    assertEquals(0, warehouse.capacity);

    Warehouse converted = warehouse.toWarehouse();
    assertEquals(0, converted.capacity);
  }

  @Test
  public void testDbWarehouseNegativeStock() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.stock = -10;
    assertEquals(-10, warehouse.stock);

    Warehouse converted = warehouse.toWarehouse();
    assertEquals(-10, converted.stock);
  }

  @Test
  public void testDbWarehouseLargeCapacity() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.capacity = 999999;
    assertEquals(999999, warehouse.capacity);

    Warehouse converted = warehouse.toWarehouse();
    assertEquals(999999, converted.capacity);
  }

  @Test
  public void testDbWarehouseEmptyBusinessUnitCode() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.businessUnitCode = "";
    assertEquals("", warehouse.businessUnitCode);

    Warehouse converted = warehouse.toWarehouse();
    assertEquals("", converted.businessUnitCode);
  }

  @Test
  public void testDbWarehouseSpecialCharactersInCode() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.businessUnitCode = "MWH-#@$%";
    assertEquals("MWH-#@$%", warehouse.businessUnitCode);

    Warehouse converted = warehouse.toWarehouse();
    assertEquals("MWH-#@$%", converted.businessUnitCode);
  }

  @Test
  public void testDbWarehouseMultipleInstances() {
    DbWarehouse w1 = new DbWarehouse();
    DbWarehouse w2 = new DbWarehouse();

    w1.id = 1L;
    w1.businessUnitCode = "MWH.001";
    w2.id = 2L;
    w2.businessUnitCode = "MWH.002";

    assertNotEquals(w1.id, w2.id);
    assertNotEquals(w1.businessUnitCode, w2.businessUnitCode);
  }

  @Test
  public void testDbWarehouseNullToWarehouseConversionCreatesSeparateObject() {
    DbWarehouse dbWarehouse = new DbWarehouse();
    Warehouse warehouse1 = dbWarehouse.toWarehouse();
    Warehouse warehouse2 = dbWarehouse.toWarehouse();

    assertNotSame(warehouse1, warehouse2);
  }

  @Test
  public void testDbWarehouseArchivedAtBeforeCreatedAt() {
    DbWarehouse warehouse = new DbWarehouse();
    LocalDateTime created = LocalDateTime.now();
    LocalDateTime archived = created.minusDays(5);

    warehouse.createdAt = created;
    warehouse.archivedAt = archived;

    assertTrue(warehouse.archivedAt.isBefore(warehouse.createdAt));

    Warehouse converted = warehouse.toWarehouse();
    assertTrue(converted.archivedAt.isBefore(converted.createdAt));
  }

  @Test
  public void testDbWarehouseStockEqualsCapacity() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.capacity = 500;
    warehouse.stock = 500;

    assertEquals(warehouse.capacity, warehouse.stock);
  }

  @Test
  public void testDbWarehouseStockExceedsCapacity() {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.capacity = 100;
    warehouse.stock = 150;

    assertTrue(warehouse.stock > warehouse.capacity);
  }
}
