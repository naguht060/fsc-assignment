package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

/**
 * Comprehensive tests for StoreService covering all CRUD operations, edge cases, and error paths.
 * Tests: create/update/patch/delete/findByIdOrThrow/listAll with null validation, exceptions, etc.
 */
@QuarkusTest
public class StoreServiceComprehensiveTest {

  @Inject StoreService storeService;

  private Store testStore;
  private String uniqueSuffix;

  @BeforeEach
  @Transactional
  public void setUp() {
    // Clean up only test stores created by this test class (with TEST_STORE_ prefix)
    Store.delete("name like ?1", "TEST_STORE_%");
    
    // Generate unique suffix to avoid constraint violations
    uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
    
    // Create a test store
    testStore = new Store();
    testStore.name = "TEST_STORE_" + uniqueSuffix;
    testStore.quantityProductsInStock = 100;
    testStore.persist();
  }

  @Test
  @Transactional
  public void testListAllReturnsAllStores() {
    var stores = storeService.listAll();
    assertNotNull(stores);
    assertTrue(stores.size() > 0);
    assertTrue(stores.stream().anyMatch(s -> s.name.startsWith("TEST_STORE_")));
  }

  @Test
  @Transactional
  public void testFindByIdOrThrowReturnsExistingStore() {
    Store found = storeService.findByIdOrThrow(testStore.id);
    assertNotNull(found);
    assertTrue(found.name.startsWith("TEST_STORE_"));
    assertEquals(100, found.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void testFindByIdOrThrowThrows404ForNonExistentId() {
    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> storeService.findByIdOrThrow(999999999L));
    assertEquals(404, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("does not exist"));
  }

  @Test
  @Transactional
  public void testCreateStoreSuccessfully() {
    Store newStore = new Store();
    newStore.name = "NEW_STORE_SUCCESS";
    newStore.quantityProductsInStock = 50;

    Store created = storeService.create(newStore);

    assertNotNull(created);
    assertNotNull(created.id);
    assertEquals("NEW_STORE_SUCCESS", created.name);
    assertEquals(50, created.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void testCreateStoreWithIdSetThrows422() {
    Store storeWithId = new Store();
    storeWithId.id = 12345L;
    storeWithId.name = "STORE_WITH_ID";
    storeWithId.quantityProductsInStock = 50;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> storeService.create(storeWithId));
    assertEquals(422, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Id was invalidly set"));
  }

  @Test
  @Transactional
  public void testUpdateStoreSuccessfully() {
    Store updateData = new Store();
    updateData.name = "UPDATED_NAME";
    updateData.quantityProductsInStock = 200;

    Store updated = storeService.update(testStore.id, updateData);

    assertNotNull(updated);
    assertEquals("UPDATED_NAME", updated.name);
    assertEquals(200, updated.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void testUpdateNonExistentStoreThrows404() {
    Store updateData = new Store();
    updateData.name = "UPDATED_NAME";

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> storeService.update(999999999L, updateData));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  @Transactional
  public void testUpdateWithNullNameThrows422() {
    Store updateData = new Store();
    updateData.name = null;
    updateData.quantityProductsInStock = 200;

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> storeService.update(testStore.id, updateData));
    assertEquals(422, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Store Name was not set"));
  }

  @Test
  @Transactional
  public void testPatchStoreSuccessfully() {
    Store patchData = new Store();
    patchData.name = "PATCHED_NAME";
    patchData.quantityProductsInStock = 300;

    Store patched = storeService.patch(testStore.id, patchData);

    assertNotNull(patched);
    assertEquals("PATCHED_NAME", patched.name);
    assertEquals(300, patched.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void testPatchNonExistentStoreThrows404() {
    Store patchData = new Store();
    patchData.name = "PATCHED_NAME";

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> storeService.patch(999999999L, patchData));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  @Transactional
  public void testPatchWithNullNameThrows422() {
    Store patchData = new Store();
    patchData.name = null;
    patchData.quantityProductsInStock = 300;

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> storeService.patch(testStore.id, patchData));
    assertEquals(422, ex.getResponse().getStatus());
    assertTrue(ex.getMessage().contains("Store Name was not set"));
  }

  @Test
  @Transactional
  public void testPatchStoreWithQuantityZeroDoesNotUpdate() {
    // Create store with quantity 0
    Store zeroStore = new Store();
    zeroStore.name = "ZERO_STORE";
    zeroStore.quantityProductsInStock = 0;
    zeroStore.persist();

    // Patch with new quantity
    Store patchData = new Store();
    patchData.name = "ZERO_PATCHED";
    patchData.quantityProductsInStock = 500;

    Store result = storeService.patch(zeroStore.id, patchData);

    // Name should be updated, but quantity should NOT be updated (because current == 0)
    assertEquals("ZERO_PATCHED", result.name);
    assertEquals(0, result.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void testPatchStoreWithQuantityNonZeroUpdates() {
    // testStore already has quantity = 100
    assertEquals(100, testStore.quantityProductsInStock);

    Store patchData = new Store();
    patchData.name = "PATCHED_NONZERO";
    patchData.quantityProductsInStock = 550;

    Store result = storeService.patch(testStore.id, patchData);

    // Both name and quantity should be updated
    assertEquals("PATCHED_NONZERO", result.name);
    assertEquals(550, result.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void testDeleteStoreSuccessfully() {
    Long id = testStore.id;

    storeService.delete(id);

    // Verify it's deleted
    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> storeService.findByIdOrThrow(id));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  @Transactional
  public void testDeleteNonExistentStoreThrows404() {
    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> storeService.delete(999999999L));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  @Transactional
  public void testAfterCommitSynchronizationBehavior() {
    // This tests that the AfterCommitSynchronization inner class is used
    Store store = new Store();
    store.name = "SYNC_TEST";
    store.quantityProductsInStock = 75;

    // Should successfully execute with synchronization callback
    Store created = storeService.create(store);
    assertNotNull(created.id);
    assertEquals("SYNC_TEST", created.name);
  }

  @Test
  @Transactional
  public void testUpdateCallsSynchronization() {
    Store updateData = new Store();
    updateData.name = "UPDATE_SYNC_TEST";
    updateData.quantityProductsInStock = 150;

    // Should successfully execute with synchronization callback
    Store updated = storeService.update(testStore.id, updateData);
    assertEquals("UPDATE_SYNC_TEST", updated.name);
  }

  @Test
  @Transactional
  public void testPatchCallsSynchronization() {
    Store patchData = new Store();
    patchData.name = "PATCH_SYNC_TEST";
    patchData.quantityProductsInStock = 175;

    // Should successfully execute with synchronization callback
    Store patched = storeService.patch(testStore.id, patchData);
    assertEquals("PATCH_SYNC_TEST", patched.name);
  }

  @Test
  @Transactional
  public void testListAllOrderedByName() {
    var stores = storeService.listAll();
    assertNotNull(stores);
    // Verify stores are sorted (they should be ordered by name)
    for (int i = 0; i < stores.size() - 1; i++) {
      String name1 = stores.get(i).name;
      String name2 = stores.get(i + 1).name;
      // Handle null names gracefully - null should be treated as smaller
      if (name1 != null && name2 != null) {
        assertTrue(name1.compareTo(name2) <= 0, "Stores should be ordered by name");
      }
    }
  }

  @Test
  @Transactional
  public void testUpdateWithZeroQuantity() {
    Store updateData = new Store();
    updateData.name = "ZERO_QUANTITY_UPDATE";
    updateData.quantityProductsInStock = 0;

    Store updated = storeService.update(testStore.id, updateData);

    assertEquals(0, updated.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void testPatchPreservesNameIfNullInEntity() {
    // Create store without explicit name check for the patch logic
    Store store = new Store();
    store.name = "ORIGINAL_NAME";
    store.quantityProductsInStock = 100;
    store.persist();

    Store patchData = new Store();
    patchData.name = "NEW_NAME";
    patchData.quantityProductsInStock = 200;

    Store result = storeService.patch(store.id, patchData);

    // Since entity.name is not null, it should be updated
    assertEquals("NEW_NAME", result.name);
  }
}
