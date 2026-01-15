package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreServiceTest {

  @Inject StoreService storeService;

  @BeforeEach
  @Transactional
  void setUp() {
    // Use existing data from import.sql (TONSTAD, KALLAX, BESTÅ)
    // Tests will work with IDs 1, 2, 3 from import.sql
    // Don't delete data as it's shared with other tests
  }

  @Test
  @Order(1)
  @Transactional
  public void testListAll() {
    var stores = storeService.listAll();
    assertNotNull(stores);
    // Should have at least the stores from import.sql
    assertEquals(true, stores.size() >= 3);
  }

  @Test
  @Order(2)
  @Transactional
  public void testFindByIdOrThrow() {
    Store store = storeService.findByIdOrThrow(1L);
    assertNotNull(store);
    assertNotNull(store.name);
    assertTrue(store.name.length() > 0);
  }

  @Test
  @Order(3)
  @Transactional
  public void testFindByIdOrThrowNotFound() {
    assertThrows(WebApplicationException.class, () -> storeService.findByIdOrThrow(999L));
  }

  @Test
  @Order(4)
  @Transactional
  public void testCreate() {
    Store newStore = new Store();
    newStore.name = "NEW_STORE_SERVICE";
    newStore.quantityProductsInStock = 30;

    Store created = storeService.create(newStore);
    assertNotNull(created);
    assertNotNull(created.id);
    assertEquals("NEW_STORE_SERVICE", created.name);
    assertEquals(30, created.quantityProductsInStock);
  }

  @Test
  @Order(5)
  @Transactional
  public void testCreateWithIdShouldFail() {
    Store store = new Store();
    store.id = 999L;
    store.name = "INVALID_STORE";

    assertThrows(WebApplicationException.class, () -> storeService.create(store));
  }

  @Test
  @Order(6)
  @Transactional
  public void testUpdate() {
    Store updatedStore = new Store();
    updatedStore.name = "UPDATED_STORE";
    updatedStore.quantityProductsInStock = 50;

    Store result = storeService.update(1L, updatedStore);
    assertNotNull(result);
    assertEquals("UPDATED_STORE", result.name);
    assertEquals(50, result.quantityProductsInStock);
  }

  @Test
  @Order(7)
  @Transactional
  public void testUpdateWithoutNameShouldFail() {
    Store store = new Store();
    store.quantityProductsInStock = 50;

    assertThrows(WebApplicationException.class, () -> storeService.update(1L, store));
  }

  @Test
  @Order(8)
  @Transactional
  public void testPatch() {
    Store patchedStore = new Store();
    patchedStore.name = "PATCHED_STORE";
    patchedStore.quantityProductsInStock = 60;

    Store result = storeService.patch(1L, patchedStore);
    assertNotNull(result);
    assertEquals("PATCHED_STORE", result.name);
  }

  @Test
  @Order(9)
  @Transactional
  public void testDelete() {
    // Create a store to delete
    Store storeToDelete = new Store();
    storeToDelete.name = "STORE_TO_DELETE";
    storeToDelete.quantityProductsInStock = 10;
    Store created = storeService.create(storeToDelete);
    Long idToDelete = created.id;

    storeService.delete(idToDelete);

    // Verify it's deleted
    assertThrows(WebApplicationException.class, () -> storeService.findByIdOrThrow(idToDelete));
  }
}
