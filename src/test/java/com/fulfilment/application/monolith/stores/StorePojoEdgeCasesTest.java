package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Store POJO to ensure field coverage including constructors, getters/setters.
 */
public class StorePojoEdgeCasesTest {

  @Test
  public void testStoreConstructorWithName() {
    Store store = new Store("TEST_STORE");
    assertEquals("TEST_STORE", store.name);
    assertNull(store.id);
    assertEquals(0, store.quantityProductsInStock);
  }

  @Test
  public void testStoreDefaultConstructor() {
    Store store = new Store();
    assertNull(store.id);
    assertNull(store.name);
    assertEquals(0, store.quantityProductsInStock);
  }

  @Test
  public void testStoreFieldAssignment() {
    Store store = new Store();
    store.id = 1L;
    store.name = "Store Name";
    store.quantityProductsInStock = 100;

    assertEquals(1L, store.id);
    assertEquals("Store Name", store.name);
    assertEquals(100, store.quantityProductsInStock);
  }

  @Test
  public void testStoreNullValues() {
    Store store = new Store();
    store.id = null;
    store.name = null;
    store.quantityProductsInStock = 0;

    assertNull(store.id);
    assertNull(store.name);
    assertEquals(0, store.quantityProductsInStock);
  }

  @Test
  public void testStoreNegativeQuantity() {
    Store store = new Store();
    store.quantityProductsInStock = -5;
    assertEquals(-5, store.quantityProductsInStock);
  }

  @Test
  public void testStoreLargeQuantity() {
    Store store = new Store();
    store.quantityProductsInStock = Integer.MAX_VALUE;
    assertEquals(Integer.MAX_VALUE, store.quantityProductsInStock);
  }

  @Test
  public void testStoreEmptyName() {
    Store store = new Store("");
    assertEquals("", store.name);
  }

  @Test
  public void testStoreLongName() {
    Store store = new Store();
    String longName = "VERY_LONG_STORE_NAME_THAT_EXCEEDS_NORMAL_LIMITS";
    store.name = longName;
    assertEquals(longName, store.name);
  }

  @Test
  public void testStoreMultipleInstances() {
    Store s1 = new Store("Store1");
    Store s2 = new Store("Store2");

    assertNotEquals(s1.name, s2.name);
    s1.quantityProductsInStock = 50;
    s2.quantityProductsInStock = 100;
    assertEquals(50, s1.quantityProductsInStock);
    assertEquals(100, s2.quantityProductsInStock);
  }

  @Test
  public void testStoreZeroQuantity() {
    Store store = new Store();
    store.quantityProductsInStock = 0;
    assertEquals(0, store.quantityProductsInStock);
  }

  @Test
  public void testStoreSpecialCharactersInName() {
    Store store = new Store("STORE-@#$%");
    assertEquals("STORE-@#$%", store.name);
  }

  @Test
  public void testStoreUnicodeCharactersInName() {
    Store store = new Store("STORE-ÅÄÖÉ");
    assertEquals("STORE-ÅÄÖÉ", store.name);
  }

  @Test
  public void testStoreConstructorInitializesOnlyName() {
    Store store = new Store("InitStore");
    // Constructor should only set name, not touch id or quantityProductsInStock
    assertNull(store.id);
    assertEquals(0, store.quantityProductsInStock);
    assertEquals("InitStore", store.name);
  }
}
