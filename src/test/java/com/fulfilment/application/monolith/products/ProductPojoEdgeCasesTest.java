package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Tests for Product POJO to ensure field coverage including constructors, getters/setters.
 */
public class ProductPojoEdgeCasesTest {

  @Test
  public void testProductConstructorWithName() {
    Product product = new Product("TEST_NAME");
    assertEquals("TEST_NAME", product.name);
    assertNull(product.id);
    assertNull(product.description);
    assertNull(product.price);
    assertEquals(0, product.stock);
  }

  @Test
  public void testProductDefaultConstructor() {
    Product product = new Product();
    assertNull(product.id);
    assertNull(product.name);
    assertNull(product.description);
    assertNull(product.price);
    assertEquals(0, product.stock);
  }

  @Test
  public void testProductFieldAssignment() {
    Product product = new Product();
    product.id = 1L;
    product.name = "Product Name";
    product.description = "Product Description";
    product.price = new BigDecimal("99.99");
    product.stock = 50;

    assertEquals(1L, product.id);
    assertEquals("Product Name", product.name);
    assertEquals("Product Description", product.description);
    assertEquals(0, new BigDecimal("99.99").compareTo(product.price));
    assertEquals(50, product.stock);
  }

  @Test
  public void testProductNullValues() {
    Product product = new Product();
    product.id = null;
    product.name = null;
    product.description = null;
    product.price = null;
    product.stock = 0;

    assertNull(product.id);
    assertNull(product.name);
    assertNull(product.description);
    assertNull(product.price);
    assertEquals(0, product.stock);
  }

  @Test
  public void testProductNegativeStock() {
    Product product = new Product();
    product.stock = -5;
    assertEquals(-5, product.stock);
  }

  @Test
  public void testProductLargeStockValue() {
    Product product = new Product();
    product.stock = Integer.MAX_VALUE;
    assertEquals(Integer.MAX_VALUE, product.stock);
  }

  @Test
  public void testProductZeroPrice() {
    Product product = new Product();
    product.price = BigDecimal.ZERO;
    assertEquals(0, BigDecimal.ZERO.compareTo(product.price));
  }

  @Test
  public void testProductNegativePrice() {
    Product product = new Product();
    product.price = new BigDecimal("-50.00");
    assertEquals(0, new BigDecimal("-50.00").compareTo(product.price));
  }

  @Test
  public void testProductMaxPrice() {
    Product product = new Product();
    product.price = new BigDecimal("9999999.99");
    assertEquals(0, new BigDecimal("9999999.99").compareTo(product.price));
  }

  @Test
  public void testProductEmptyName() {
    Product product = new Product("");
    assertEquals("", product.name);
  }

  @Test
  public void testProductLongDescription() {
    Product product = new Product();
    String longDesc =
        "This is a very long description that should exceed normal limits. "
            + "It contains many characters to test that the description field "
            + "can hold large amounts of text without issues. The database "
            + "column allows up to 255 characters, so this is well within that limit.";
    product.description = longDesc;
    assertEquals(longDesc, product.description);
  }

  @Test
  public void testProductMultipleInstances() {
    Product p1 = new Product("Product1");
    Product p2 = new Product("Product2");

    assertNotEquals(p1.name, p2.name);
    p1.stock = 10;
    p2.stock = 20;
    assertEquals(10, p1.stock);
    assertEquals(20, p2.stock);
  }
}
