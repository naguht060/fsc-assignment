package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreServiceIntegrationTest {

  @Inject StoreService storeService;

  private Store store;

  @BeforeEach
  @Transactional
  void setUp() {
    store = new Store();
    store.name = "TEST_STORE_" + System.currentTimeMillis();
    store.quantityProductsInStock = 50;
    store.persist();
  }

  @Test
  @Transactional
  void createStore_success() {
    Store newStore = new Store();
    newStore.name = "NEW_STORE_" + System.currentTimeMillis();
    newStore.quantityProductsInStock = 75;

    Store created = storeService.create(newStore);
    
    assertNotNull(created.id);
    assertEquals(newStore.name, created.name);
    assertEquals(75, created.quantityProductsInStock);
  }

  @Test
  @Transactional
  void updateStore_success() {
    Store updated = new Store();
    updated.name = "UPDATED_" + store.name;
    updated.quantityProductsInStock = 150;

    Store result = storeService.update(store.id, updated);
    
    assertEquals("UPDATED_" + store.name, result.name);
    assertEquals(150, result.quantityProductsInStock);
  }

  @Test
  @Transactional
  void patchStore_success() {
    Store patch = new Store();
    patch.name = "PATCHED_" + store.name;
    patch.quantityProductsInStock = 200;

    Store result = storeService.patch(store.id, patch);
    
    assertEquals("PATCHED_" + store.name, result.name);
    assertEquals(200, result.quantityProductsInStock);
  }

  @Test
  @Transactional
  void deleteStore_success() {
    Long id = store.id;
    storeService.delete(id);
    
    Store deleted = Store.findById(id);
    assertNull(deleted);
  }

  @Test
  @Transactional
  void findByIdOrThrow_success() {
    Store retrieved = storeService.findByIdOrThrow(store.id);
    assertNotNull(retrieved);
    assertEquals(store.id, retrieved.id);
  }

  @Test
  @Transactional
  void listAll_success() {
    var stores = storeService.listAll();
    assertNotNull(stores);
    assertTrue(stores.size() > 0);
  }

  @Test
  @Transactional
  void updateStore_withoutName_throwsException() {
    Store update = new Store();
    update.quantityProductsInStock = 100;

    assertThrows(Exception.class, () -> storeService.update(store.id, update));
  }

  @Test
  @Transactional
  void patchStore_withoutName_throwsException() {
    Store patch = new Store();
    patch.quantityProductsInStock = 100;

    assertThrows(Exception.class, () -> storeService.patch(store.id, patch));
  }

  @Test
  @Transactional
  void createStore_withId_throwsException() {
    Store newStore = new Store();
    newStore.id = 999L;
    newStore.name = "SHOULD_FAIL";
    newStore.quantityProductsInStock = 75;

    assertThrows(Exception.class, () -> storeService.create(newStore));
  }
}

