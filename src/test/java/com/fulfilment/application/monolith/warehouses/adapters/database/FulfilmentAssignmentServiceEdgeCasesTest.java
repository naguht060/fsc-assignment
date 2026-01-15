package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for FulfilmentAssignmentService null checks.
 */
public class FulfilmentAssignmentServiceEdgeCasesTest {

  private FulfilmentAssignmentService service = new FulfilmentAssignmentService();

  @Test
  public void testAssignNullStoreThrows() {
    Product p = new Product();
    DbWarehouse w = new DbWarehouse();
    assertThrows(
        IllegalArgumentException.class,
        () -> service.assign(null, p, w),
        "Should throw for null store");
  }

  @Test
  public void testAssignNullProductThrows() {
    Store s = new Store();
    DbWarehouse w = new DbWarehouse();
    assertThrows(
        IllegalArgumentException.class,
        () -> service.assign(s, null, w),
        "Should throw for null product");
  }

  @Test
  public void testAssignNullWarehouseThrows() {
    Store s = new Store();
    Product p = new Product();
    assertThrows(
        IllegalArgumentException.class,
        () -> service.assign(s, p, null),
        "Should throw for null warehouse");
  }

  @Test
  public void testAssignAllNullThrows() {
    assertThrows(IllegalArgumentException.class, () -> service.assign(null, null, null));
  }
}
