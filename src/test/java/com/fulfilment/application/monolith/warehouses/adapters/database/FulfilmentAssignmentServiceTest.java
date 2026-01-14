package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FulfilmentAssignmentServiceTest {

  @Inject FulfilmentAssignmentService service;

  @Inject ProductRepository productRepository;

  @Inject WarehouseRepository warehouseRepository;

  private Store store;
  private Product product;
  private DbWarehouse warehouse;

  @BeforeEach
  @Transactional
  void setUp() {
    // Create test data with unique names to avoid conflicts
    long timestamp = System.currentTimeMillis();
    store = new Store();
    store.name = "TEST_STORE_" + timestamp;
    store.quantityProductsInStock = 10;
    store.persist();

    product = new Product();
    product.name = "TEST_PRODUCT_" + timestamp;
    product.stock = 20;
    productRepository.persist(product);

    warehouse = new DbWarehouse();
    warehouse.businessUnitCode = "MWH.TEST_" + timestamp;
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;
    warehouseRepository.persist(warehouse);
  }

  @Test
  @Order(1)
  @Transactional
  public void testAssignSuccessfully() {
    StoreWarehouseProduct assignment = service.assign(store, product, warehouse);
    assertNotNull(assignment);
    assertNotNull(assignment.id);
    assertEquals(store.id, assignment.store.id);
    assertEquals(product.id, assignment.product.id);
    assertEquals(warehouse.id, assignment.warehouse.id);
  }

  @Test
  @Order(2)
  @Transactional
  public void testAssignDuplicateReturnsExisting() {
    StoreWarehouseProduct first = service.assign(store, product, warehouse);
    StoreWarehouseProduct second = service.assign(store, product, warehouse);
    
    assertNotNull(first);
    assertNotNull(second);
    assertEquals(first.id, second.id);
  }

  @Test
  @Order(3)
  @Transactional
  public void testAssignWithNullStoreShouldFail() {
    assertThrows(
        IllegalArgumentException.class, () -> service.assign(null, product, warehouse));
  }

  @Test
  @Order(4)
  @Transactional
  public void testAssignWithNullProductShouldFail() {
    assertThrows(
        IllegalArgumentException.class, () -> service.assign(store, null, warehouse));
  }

  @Test
  @Order(5)
  @Transactional
  public void testAssignWithNullWarehouseShouldFail() {
    assertThrows(
        IllegalArgumentException.class, () -> service.assign(store, product, null));
  }

  @Test
  @Order(6)
  @Transactional
  public void testAssignMaxTwoWarehousesPerProductPerStore() {
    long timestamp = System.currentTimeMillis();
    // Create second warehouse
    DbWarehouse warehouse2 = new DbWarehouse();
    warehouse2.businessUnitCode = "MWH.TEST2_" + timestamp;
    warehouse2.location = "AMSTERDAM-001";
    warehouse2.capacity = 100;
    warehouse2.stock = 50;
    warehouseRepository.persist(warehouse2);

    // Create third warehouse
    DbWarehouse warehouse3 = new DbWarehouse();
    warehouse3.businessUnitCode = "MWH.TEST3_" + timestamp;
    warehouse3.location = "AMSTERDAM-001";
    warehouse3.capacity = 100;
    warehouse3.stock = 50;
    warehouseRepository.persist(warehouse3);

    // Assign first two warehouses - should succeed
    service.assign(store, product, warehouse);
    service.assign(store, product, warehouse2);

    // Try to assign third warehouse - should fail
    assertThrows(
        IllegalArgumentException.class,
        () -> service.assign(store, product, warehouse3),
        "A product can be fulfilled by at most 2 warehouses per store");
  }

  @Test
  @Order(7)
  @Transactional
  public void testAssignMaxThreeWarehousesPerStore() {
    long timestamp = System.currentTimeMillis();
    // Create additional warehouses
    DbWarehouse warehouse2 = new DbWarehouse();
    warehouse2.businessUnitCode = "MWH.TEST2_" + timestamp;
    warehouse2.location = "AMSTERDAM-001";
    warehouse2.capacity = 100;
    warehouse2.stock = 50;
    warehouseRepository.persist(warehouse2);

    DbWarehouse warehouse3 = new DbWarehouse();
    warehouse3.businessUnitCode = "MWH.TEST3_" + timestamp;
    warehouse3.location = "AMSTERDAM-001";
    warehouse3.capacity = 100;
    warehouse3.stock = 50;
    warehouseRepository.persist(warehouse3);

    DbWarehouse warehouse4 = new DbWarehouse();
    warehouse4.businessUnitCode = "MWH.TEST4_" + timestamp;
    warehouse4.location = "AMSTERDAM-001";
    warehouse4.capacity = 100;
    warehouse4.stock = 50;
    warehouseRepository.persist(warehouse4);

    // Create different products
    Product product2 = new Product();
    product2.name = "TEST_PRODUCT2_" + timestamp;
    product2.stock = 20;
    productRepository.persist(product2);

    // Assign first three warehouses with different products - should succeed
    service.assign(store, product, warehouse);
    service.assign(store, product2, warehouse2);
    service.assign(store, product, warehouse3);

    // Try to assign fourth warehouse - should fail
    assertThrows(
        IllegalArgumentException.class,
        () -> service.assign(store, product2, warehouse4),
        "A store can be fulfilled by at most 3 different warehouses");
  }

  @Test
  @Order(8)
  @Transactional
  public void testAssignMaxFiveProductTypesPerWarehouse() {
    long timestamp = System.currentTimeMillis();
    // Create additional products
    Product product2 = new Product();
    product2.name = "TEST_PRODUCT2_" + timestamp;
    product2.stock = 20;
    productRepository.persist(product2);

    Product product3 = new Product();
    product3.name = "TEST_PRODUCT3_" + timestamp;
    product3.stock = 20;
    productRepository.persist(product3);

    Product product4 = new Product();
    product4.name = "TEST_PRODUCT4_" + timestamp;
    product4.stock = 20;
    productRepository.persist(product4);

    Product product5 = new Product();
    product5.name = "TEST_PRODUCT5_" + timestamp;
    product5.stock = 20;
    productRepository.persist(product5);

    Product product6 = new Product();
    product6.name = "TEST_PRODUCT6_" + timestamp;
    product6.stock = 20;
    productRepository.persist(product6);

    // Create additional store
    Store store2 = new Store();
    store2.name = "TEST_STORE2_" + timestamp;
    store2.quantityProductsInStock = 10;
    store2.persist();

    // Assign first five products - should succeed
    service.assign(store, product, warehouse);
    service.assign(store, product2, warehouse);
    service.assign(store, product3, warehouse);
    service.assign(store2, product4, warehouse);
    service.assign(store2, product5, warehouse);

    // Try to assign sixth product - should fail
    assertThrows(
        IllegalArgumentException.class,
        () -> service.assign(store, product6, warehouse),
        "A warehouse can store at most 5 different product types");
  }
}
