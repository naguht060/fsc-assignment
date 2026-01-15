package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductPojoTest {

  @Test
  void productConstructorsAndFields() {
    Product p = new Product("Widget");
    assertEquals("Widget", p.name);

    Product empty = new Product();
    assertNull(empty.name);
    assertNull(empty.description);
    assertNull(empty.price);
    assertEquals(0, empty.stock);
  }
}
