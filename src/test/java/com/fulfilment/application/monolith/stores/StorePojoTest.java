package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StorePojoTest {

  @Test
  void storeConstructorsAndFields() {
    Store s = new Store("CornerStore");
    assertEquals("CornerStore", s.name);

    Store empty = new Store();
    assertNull(empty.name);
    assertEquals(0, empty.quantityProductsInStock);
  }
}
