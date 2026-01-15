package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LegacyStoreManagerGatewayTest {

  @Inject LegacyStoreManagerGateway gateway;

  @Test
  public void testCreateStoreOnLegacySystem() {
    Store store = new Store();
    store.name = "TEST_STORE_CREATE_" + System.currentTimeMillis();
    store.quantityProductsInStock = 100;

    // Should not throw exception
    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreOnLegacySystem() {
    Store store = new Store();
    store.name = "UPDATED_STORE_" + System.currentTimeMillis();
    store.quantityProductsInStock = 200;

    // Should not throw exception
    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithZeroQuantity() {
    Store store = new Store();
    store.name = "ZERO_QUANTITY_" + System.currentTimeMillis();
    store.quantityProductsInStock = 0;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithNegativeQuantity() {
    Store store = new Store();
    store.name = "NEGATIVE_QUANTITY_" + System.currentTimeMillis();
    store.quantityProductsInStock = -50;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithLargeQuantity() {
    Store store = new Store();
    store.name = "LARGE_QUANTITY_" + System.currentTimeMillis();
    store.quantityProductsInStock = Integer.MAX_VALUE;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreWithZeroQuantity() {
    Store store = new Store();
    store.name = "UPDATE_ZERO_" + System.currentTimeMillis();
    store.quantityProductsInStock = 0;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreWithNegativeQuantity() {
    Store store = new Store();
    store.name = "UPDATE_NEGATIVE_" + System.currentTimeMillis();
    store.quantityProductsInStock = -100;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreWithLargeQuantity() {
    Store store = new Store();
    store.name = "UPDATE_LARGE_" + System.currentTimeMillis();
    store.quantityProductsInStock = Integer.MAX_VALUE;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithSpecialCharactersInName() {
    Store store = new Store();
    store.name = "STORE_@#$%_" + System.currentTimeMillis();
    store.quantityProductsInStock = 50;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithLongName() {
    Store store = new Store();
    store.name = "VERY_LONG_STORE_NAME_" + "X".repeat(200) + "_" + System.currentTimeMillis();
    store.quantityProductsInStock = 75;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithSpaces() {
    Store store = new Store();
    store.name = "STORE WITH MULTIPLE SPACES " + System.currentTimeMillis();
    store.quantityProductsInStock = 100;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreWithSpecialCharactersInName() {
    Store store = new Store();
    store.name = "UPDATE_@#$!_" + System.currentTimeMillis();
    store.quantityProductsInStock = 60;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithNumericName() {
    Store store = new Store();
    store.name = "12345_" + System.currentTimeMillis();
    store.quantityProductsInStock = 80;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreWithNumericName() {
    Store store = new Store();
    store.name = "UPDATE_54321_" + System.currentTimeMillis();
    store.quantityProductsInStock = 90;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testMultipleCreateOperations() {
    for (int i = 0; i < 5; i++) {
      Store store = new Store();
      store.name = "BULK_CREATE_" + i + "_" + System.currentTimeMillis();
      store.quantityProductsInStock = 100 + i;

      assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }
  }

  @Test
  public void testMultipleUpdateOperations() {
    for (int i = 0; i < 5; i++) {
      Store store = new Store();
      store.name = "BULK_UPDATE_" + i + "_" + System.currentTimeMillis();
      store.quantityProductsInStock = 50 + (i * 10);

      assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
    }
  }

  @Test
  public void testCreateAndUpdateSequence() {
    Store store = new Store();
    store.name = "CREATE_THEN_UPDATE_" + System.currentTimeMillis();
    store.quantityProductsInStock = 100;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));

    store.quantityProductsInStock = 150;
    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreWithUnicodeCharacters() {
    Store store = new Store();
    store.name = "STORE_日本_" + System.currentTimeMillis();
    store.quantityProductsInStock = 65;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreWithUnicodeCharacters() {
    Store store = new Store();
    store.name = "UPDATE_привет_" + System.currentTimeMillis();
    store.quantityProductsInStock = 85;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }
}


