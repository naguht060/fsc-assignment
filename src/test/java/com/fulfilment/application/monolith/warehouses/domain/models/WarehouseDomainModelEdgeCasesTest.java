package com.fulfilment.application.monolith.warehouses.domain.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for Warehouse and Location domain models to improve coverage.
 */
public class WarehouseDomainModelEdgeCasesTest {

  @Test
  public void testWarehouseDefaultConstructor() {
    Warehouse warehouse = new Warehouse();
    assertNull(warehouse.businessUnitCode);
    assertNull(warehouse.location);
    assertNull(warehouse.capacity);
    assertNull(warehouse.stock);
    assertNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testWarehouseFieldAssignment() {
    Warehouse warehouse = new Warehouse();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime archived = now.plusDays(30);

    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouse.createdAt = now;
    warehouse.archivedAt = archived;

    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
    assertEquals(100, warehouse.capacity);
    assertEquals(50, warehouse.stock);
    assertEquals(now, warehouse.createdAt);
    assertEquals(archived, warehouse.archivedAt);
  }

  @Test
  public void testWarehouseEqualsReflexive() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 100;
    w1.stock = 50;

    assertEquals(w1, w1);
  }

  @Test
  public void testWarehouseEqualsSymmetric() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 100;
    w1.stock = 50;

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.001";
    w2.location = "ZWOLLE-001";
    w2.capacity = 100;
    w2.stock = 50;

    assertEquals(w1, w2);
    assertEquals(w2, w1);
  }

  @Test
  public void testWarehouseEqualsTransitive() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.001";

    Warehouse w3 = new Warehouse();
    w3.businessUnitCode = "MWH.001";

    assertEquals(w1, w2);
    assertEquals(w2, w3);
    assertEquals(w1, w3);
  }

  @Test
  public void testWarehouseEqualsDifferentValues() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.002";

    assertNotEquals(w1, w2);
  }

  @Test
  public void testWarehouseEqualsWithNull() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";

    assertNotEquals(w1, null);
  }

  @Test
  public void testWarehouseEqualsDifferentType() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";

    assertNotEquals(w1, "MWH.001");
  }

  @Test
  public void testWarehouseHashCodeConsistency() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";

    int hash1 = w1.hashCode();
    int hash2 = w1.hashCode();

    assertEquals(hash1, hash2);
  }

  @Test
  public void testWarehouseHashCodeEqual() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 100;
    w1.stock = 50;

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.001";
    w2.location = "ZWOLLE-001";
    w2.capacity = 100;
    w2.stock = 50;

    assertEquals(w1.hashCode(), w2.hashCode());
  }

  @Test
  public void testWarehouseHashCodeNotEqualForDifferent() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.002";

    assertNotEquals(w1.hashCode(), w2.hashCode());
  }

  @Test
  public void testLocationConstructor() {
    Location location = new Location("ZWOLLE-001", 3, 500);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(3, location.maxNumberOfWarehouses);
    assertEquals(500, location.maxCapacity);
  }

  @Test
  public void testLocationWithDifferentValues() {
    Location loc1 = new Location("ZWOLLE-001", 3, 500);
    Location loc2 = new Location("AMSTERDAM-001", 5, 1000);

    assertNotEquals(loc1.identification, loc2.identification);
    assertNotEquals(loc1.maxNumberOfWarehouses, loc2.maxNumberOfWarehouses);
    assertNotEquals(loc1.maxCapacity, loc2.maxCapacity);
  }

  @Test
  public void testLocationZeroMaxWarehouses() {
    Location location = new Location("TEST-LOC", 0, 500);
    assertEquals(0, location.maxNumberOfWarehouses);
  }

  @Test
  public void testLocationZeroCapacity() {
    Location location = new Location("TEST-LOC", 5, 0);
    assertEquals(0, location.maxCapacity);
  }

  @Test
  public void testWarehouseWithAllNullFields() {
    Warehouse w1 = new Warehouse();
    Warehouse w2 = new Warehouse();
    assertEquals(w1, w2);
  }

  @Test
  public void testWarehouseWithPartialNullFields() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = null;

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.001";
    w2.location = null;

    assertEquals(w1, w2);
  }

  @Test
  public void testWarehouseEqualsDifferentByOneField() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 100;

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.001";
    w2.location = "ZWOLLE-001";
    w2.capacity = 200;

    assertNotEquals(w1, w2);
  }

  @Test
  public void testWarehouseTimestampEquality() {
    LocalDateTime now = LocalDateTime.now();

    Warehouse w1 = new Warehouse();
    w1.createdAt = now;

    Warehouse w2 = new Warehouse();
    w2.createdAt = now;

    assertEquals(w1, w2);
  }

  @Test
  public void testWarehouseTimestampDifference() {
    LocalDateTime now = LocalDateTime.now();

    Warehouse w1 = new Warehouse();
    w1.createdAt = now;

    Warehouse w2 = new Warehouse();
    w2.createdAt = now.plusDays(1);

    assertNotEquals(w1, w2);
  }
}
