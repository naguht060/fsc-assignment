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
}


