package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfilmentAssServiceCompTest {

  @Inject FulfilmentAssignmentService fulfilmentAssignmentService;
  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  private Store testStore;
  private Product product1, product2, product3, product4, product5, product6;
  private DbWarehouse warehouse1, warehouse2, warehouse3, warehouse4;

  @BeforeEach
  @Transactional
  public void setUp() {
    long ts = System.currentTimeMillis();

    // Create test store
    testStore = new Store();
    testStore.name = "TEST_STORE_" + ts;
    testStore.quantityProductsInStock = 100;
    testStore.persist();

    // Create test products
    product1 = new Product();
    product1.name = "PROD_1_" + ts;
    product1.stock = 10;
    productRepository.persist(product1);

    product2 = new Product();
    product2.name = "PROD_2_" + ts;
    product2.stock = 10;
    productRepository.persist(product2);

    product3 = new Product();
    product3.name = "PROD_3_" + ts;
    product3.stock = 10;
    productRepository.persist(product3);

    product4 = new Product();
    product4.name = "PROD_4_" + ts;
    product4.stock = 10;
    productRepository.persist(product4);

    product5 = new Product();
    product5.name = "PROD_5_" + ts;
    product5.stock = 10;
    productRepository.persist(product5);

    product6 = new Product();
    product6.name = "PROD_6_" + ts;
    product6.stock = 10;
    productRepository.persist(product6);

    // Create test warehouses
    warehouse1 = new DbWarehouse();
    warehouse1.businessUnitCode = "WH.TEST1_" + ts;
    warehouse1.location = "LOCATION_1";
    warehouse1.capacity = 100;
    warehouse1.stock = 50;
    warehouseRepository.persist(warehouse1);

    warehouse2 = new DbWarehouse();
    warehouse2.businessUnitCode = "WH.TEST2_" + ts;
    warehouse2.location = "LOCATION_2";
    warehouse2.capacity = 100;
    warehouse2.stock = 50;
    warehouseRepository.persist(warehouse2);

    warehouse3 = new DbWarehouse();
    warehouse3.businessUnitCode = "WH.TEST3_" + ts;
    warehouse3.location = "LOCATION_3";
    warehouse3.capacity = 100;
    warehouse3.stock = 50;
    warehouseRepository.persist(warehouse3);

    warehouse4 = new DbWarehouse();
    warehouse4.businessUnitCode = "WH.TEST4_" + ts;
    warehouse4.location = "LOCATION_4";
    warehouse4.capacity = 100;
    warehouse4.stock = 50;
    warehouseRepository.persist(warehouse4);
  }

  @Test
  @Transactional
  public void testAssignNullStore_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> fulfilmentAssignmentService.assign(null, product1, warehouse1));
  }

  @Test
  @Transactional
  public void testAssignNullProduct_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> fulfilmentAssignmentService.assign(testStore, null, warehouse1));
  }

  @Test
  @Transactional
  public void testAssignNullWarehouse_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> fulfilmentAssignmentService.assign(testStore, product1, null));
  }

  @Test
  @Transactional
  public void testAssignSuccessful() {
    StoreWarehouseProduct result =
        fulfilmentAssignmentService.assign(testStore, product1, warehouse1);

    assertNotNull(result);
    assertEquals(testStore.id, result.store.id);
    assertEquals(product1.id, result.product.id);
    assertEquals(warehouse1.id, result.warehouse.id);
  }

  @Test
  @Transactional
  public void testAssignDuplicate_ReturnExisting() {
    StoreWarehouseProduct first =
        fulfilmentAssignmentService.assign(testStore, product1, warehouse1);
    StoreWarehouseProduct second =
        fulfilmentAssignmentService.assign(testStore, product1, warehouse1);

    assertEquals(first.id, second.id);
  }

  @Test
  @Transactional
  public void testAssignProductRule_MaxTwoWarehousesPerStore_Success() {
    // Assign product1 to 2 warehouses
    StoreWarehouseProduct first =
        fulfilmentAssignmentService.assign(testStore, product1, warehouse1);
    StoreWarehouseProduct second =
        fulfilmentAssignmentService.assign(testStore, product1, warehouse2);

    assertNotNull(first);
    assertNotNull(second);
  }

  @Test
  @Transactional
  public void testAssignProductRule_ExceedsMaxTwoWarehouses_Fails() {
    // Assign product1 to 2 warehouses (at limit)
    fulfilmentAssignmentService.assign(testStore, product1, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product1, warehouse2);

    // Try to assign to 3rd warehouse
    assertThrows(
        IllegalArgumentException.class,
        () -> fulfilmentAssignmentService.assign(testStore, product1, warehouse3));
  }

  @Test
  @Transactional
  public void testAssignStoreRule_MaxThreeWarehouses_Success() {
    // Assign 3 different products to 3 warehouses
    fulfilmentAssignmentService.assign(testStore, product1, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product2, warehouse2);
    StoreWarehouseProduct third =
        fulfilmentAssignmentService.assign(testStore, product3, warehouse3);

    assertNotNull(third);
  }

  @Test
  @Transactional
  public void testAssignStoreRule_ExceedsMaxThreeWarehouses_Fails() {
    // Assign 3 different products to 3 warehouses (at limit)
    fulfilmentAssignmentService.assign(testStore, product1, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product2, warehouse2);
    fulfilmentAssignmentService.assign(testStore, product3, warehouse3);

    // Try to assign to 4th warehouse
    assertThrows(
        IllegalArgumentException.class,
        () -> fulfilmentAssignmentService.assign(testStore, product4, warehouse4));
  }

  @Test
  @Transactional
  public void testAssignWarehouseRule_MaxFiveProductTypes_Success() {
    // Assign 5 different products to same warehouse
    fulfilmentAssignmentService.assign(testStore, product1, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product2, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product3, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product4, warehouse1);
    StoreWarehouseProduct fifth =
        fulfilmentAssignmentService.assign(testStore, product5, warehouse1);

    assertNotNull(fifth);
  }

  @Test
  @Transactional
  public void testAssignWarehouseRule_ExceedsMaxFiveProductTypes_Fails() {
    // Assign 5 different products to same warehouse (at limit)
    fulfilmentAssignmentService.assign(testStore, product1, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product2, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product3, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product4, warehouse1);
    fulfilmentAssignmentService.assign(testStore, product5, warehouse1);

    // Try to assign 6th product type
    assertThrows(
        IllegalArgumentException.class,
        () -> fulfilmentAssignmentService.assign(testStore, product6, warehouse1));
  }

}
