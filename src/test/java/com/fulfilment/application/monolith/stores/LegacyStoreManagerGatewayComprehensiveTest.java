package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

/**
 * Tests for LegacyStoreManagerGateway to ensure it can create and update stores on the legacy system.
 * Tests: createStoreOnLegacySystem, updateStoreOnLegacySystem with various store data.
 */
@QuarkusTest
public class LegacyStoreManagerGatewayComprehensiveTest {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Test
  public void testCreateStoreOnLegacySystemWithValidStore() {
    Store store = new Store();
    store.name = "LEGACY_CREATE_TEST";
    store.quantityProductsInStock = 100;

    // Should not throw any exception
    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreOnLegacySystemWithZeroQuantity() {
    Store store = new Store();
    store.name = "LEGACY_CREATE_ZERO";
    store.quantityProductsInStock = 0;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreOnLegacySystemWithLargeQuantity() {
    Store store = new Store();
    store.name = "LEGACY_CREATE_LARGE";
    store.quantityProductsInStock = 999999;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreOnLegacySystemWithSpecialCharactersInName() {
    Store store = new Store();
    store.name = "LEGACY_CREATE_@#$%^&*()";
    store.quantityProductsInStock = 50;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateStoreOnLegacySystemWithLongName() {
    Store store = new Store();
    store.name = "LEGACY_" + "A".repeat(200);
    store.quantityProductsInStock = 75;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreOnLegacySystemWithValidStore() {
    Store store = new Store();
    store.name = "LEGACY_UPDATE_TEST";
    store.quantityProductsInStock = 150;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreOnLegacySystemWithZeroQuantity() {
    Store store = new Store();
    store.name = "LEGACY_UPDATE_ZERO";
    store.quantityProductsInStock = 0;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreOnLegacySystemWithLargeQuantity() {
    Store store = new Store();
    store.name = "LEGACY_UPDATE_LARGE";
    store.quantityProductsInStock = 500000;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreOnLegacySystemWithSpecialCharactersInName() {
    Store store = new Store();
    store.name = "LEGACY_UPDATE_!@#$%";
    store.quantityProductsInStock = 200;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreOnLegacySystemMultipleTimes() {
    Store store = new Store();
    store.name = "LEGACY_UPDATE_MULTIPLE";
    store.quantityProductsInStock = 100;

    // Call multiple times to ensure consistency
    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
    store.quantityProductsInStock = 200;
    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
    store.quantityProductsInStock = 300;
    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }

  @Test
  public void testCreateAndUpdateCycleMultipleTimes() {
    Store store = new Store();
    store.name = "LEGACY_CYCLE_TEST";
    store.quantityProductsInStock = 100;

    for (int i = 0; i < 5; i++) {
      store.quantityProductsInStock = 100 + (i * 50);
      assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
      assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
    }
  }

  @Test
  public void testCreateStoreOnLegacySystemWithUnicodeCharacters() {
    Store store = new Store();
    store.name = "LEGACY_UNICODE_\u00E9\u00F1\u00F7";
    store.quantityProductsInStock = 88;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
  }

  @Test
  public void testUpdateStoreOnLegacySystemWithUnicodeCharacters() {
    Store store = new Store();
    store.name = "LEGACY_UPDATE_\u00FC\u00F6\u00E4";
    store.quantityProductsInStock = 99;

    assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
  }
}
