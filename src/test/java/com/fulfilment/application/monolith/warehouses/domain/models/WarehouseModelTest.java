package com.fulfilment.application.monolith.warehouses.domain.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class WarehouseModelTest {

  @Test
  void equalsAndHashCode_contracts() {
    Warehouse a = new Warehouse();
    a.businessUnitCode = "BU.1";
    a.location = "LOC-1";
    a.capacity = 10;
    a.stock = 5;
    a.createdAt = LocalDateTime.now();

    Warehouse b = new Warehouse();
    b.businessUnitCode = a.businessUnitCode;
    b.location = a.location;
    b.capacity = a.capacity;
    b.stock = a.stock;
    b.createdAt = a.createdAt;

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    // change a field
    b.stock = 99;
    assertNotEquals(a, b);
  }
}
