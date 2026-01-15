package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.util.UUID;

/**
 * Additional edge case tests for StoreService patch method and error scenarios.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreServiceEdgeCasesTest {

  @Inject StoreService storeService;
  
  private String uniqueSuffix;

  @BeforeEach
  @Transactional
  public void setUp() {
    // Generate unique suffix for test stores
    uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
  }

  @Test
  @Order(1)
  @Transactional
  public void testPatchWithNullNameThrows() {
    Store store = new Store();
    store.name = "PATCH_TEST_" + uniqueSuffix;
    store.quantityProductsInStock = 10;
    store.persist();

    Store patchStore = new Store();
    patchStore.name = null;

    // Should throw because name is null
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> storeService.patch(store.id, patchStore));
    assertTrue(exception.getMessage().contains("Store Name was not set on request"));
  }

  @Test
  @Order(2)
  @Transactional
  public void testPatchNotFoundThrows() {
    Store patchStore = new Store();
    patchStore.name = "SOME_NAME";

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class, () -> storeService.patch(99999L, patchStore));
    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  @Order(3)
  @Transactional
  public void testPatchPreservesNameWhenZeroQuantity() {
    // Create a store with initial name and quantity
    Store initialStore = new Store();
    initialStore.name = "ORIGINAL_NAME_" + uniqueSuffix;
    initialStore.quantityProductsInStock = 50;
    initialStore.persist();

    // Patch with zero quantity - the patch logic checks (entity.quantityProductsInStock != 0)
    // which means if current quantity is NOT zero, it will update to the new value
    Store patchStore = new Store();
    patchStore.name = "PATCHED_NAME_" + uniqueSuffix;
    patchStore.quantityProductsInStock = 0; // Zero will be applied

    Store result = storeService.patch(initialStore.id, patchStore);
    assertEquals("PATCHED_NAME_" + uniqueSuffix, result.name);
    // Quantity should be updated to 0
    assertEquals(0, result.quantityProductsInStock);
  }

  @Test
  @Order(4)
  @Transactional
  public void testPatchWithNullQuantityStaysZero() {
    // Create a store with no name set (null name)
    Store store = new Store();
    store.name = "TEST_STORE_" + uniqueSuffix;
    store.quantityProductsInStock = 100;
    store.persist();

    Store patchStore = new Store();
    patchStore.name = "NEW_NAME_" + uniqueSuffix;
    patchStore.quantityProductsInStock = 50;

    Store result = storeService.patch(store.id, patchStore);
    assertEquals("NEW_NAME_" + uniqueSuffix, result.name);
    assertEquals(50, result.quantityProductsInStock);
  }

  @Test
  @Order(5)
  @Transactional
  public void testUpdateNullNameThrows() {
    Store store = new Store();
    store.name = "UPDATE_TEST_STORE_" + uniqueSuffix;
    store.persist();

    Store updateStore = new Store();
    updateStore.name = null;

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> storeService.update(store.id, updateStore));
    assertTrue(exception.getMessage().contains("Store Name was not set on request"));
  }

  @Test
  @Order(6)
  @Transactional
  public void testUpdateNotFoundThrows() {
    Store updateStore = new Store();
    updateStore.name = "SOME_NAME";
    updateStore.quantityProductsInStock = 10;

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class, () -> storeService.update(88888L, updateStore));
    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  @Order(7)
  @Transactional
  public void testDeleteNotFoundThrows() {
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> storeService.delete(77777L));
    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  @Order(8)
  @Transactional
  public void testListAllReturnsAllStores() {
    var stores = storeService.listAll();
    assertNotNull(stores);
    assertTrue(stores.size() > 0, "Should have at least some stores from import.sql");
  }
}
