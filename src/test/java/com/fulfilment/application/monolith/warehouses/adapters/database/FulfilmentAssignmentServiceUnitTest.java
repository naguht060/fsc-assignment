package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import org.junit.jupiter.api.Test;

class FulfilmentAssignmentServiceUnitTest {

  @Test
  void assign_throwsOnNulls() {
    FulfilmentAssignmentService svc = new FulfilmentAssignmentService();

    Product p = new Product();
    Store s = new Store();

    // all null
    assertThrows(IllegalArgumentException.class, () -> svc.assign(null, null, null));

    // null store
    assertThrows(IllegalArgumentException.class, () -> svc.assign(null, p, new DbWarehouse()));

    // null product
    assertThrows(IllegalArgumentException.class, () -> svc.assign(s, null, new DbWarehouse()));

    // null warehouse
    assertThrows(IllegalArgumentException.class, () -> svc.assign(s, p, null));
  }
}
