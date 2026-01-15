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
class FulfilmentAssignmentServiceTest {

    @Inject FulfilmentAssignmentService service;
    @Inject ProductRepository productRepository;
    @Inject WarehouseRepository warehouseRepository;

    private Store store;
    private Product product;
    private DbWarehouse warehouse;

    @BeforeEach
    @Transactional
    void setUp() {
        long ts = System.currentTimeMillis();

        store = new Store();
        store.name = "TEST_STORE_" + ts;
        store.quantityProductsInStock = 10;
        store.persist();

        product = new Product();
        product.name = "TEST_PRODUCT_" + ts;
        product.stock = 20;
        productRepository.persist(product);

        warehouse = new DbWarehouse();
        warehouse.businessUnitCode = "MWH.TEST_" + ts;
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        warehouseRepository.persist(warehouse);
    }

    // ---------- BASIC ASSIGNMENT ----------

    @Test
    @Transactional
    void assign_success() {
        StoreWarehouseProduct assignment = service.assign(store, product, warehouse);

        assertNotNull(assignment);
        assertNotNull(assignment.id);
        assertEquals(store.id, assignment.store.id);
        assertEquals(product.id, assignment.product.id);
        assertEquals(warehouse.id, assignment.warehouse.id);
    }

    @Test
    @Transactional
    void assign_duplicate_returnsSameAssignment() {
        StoreWarehouseProduct first = service.assign(store, product, warehouse);
        StoreWarehouseProduct second = service.assign(store, product, warehouse);

        assertEquals(first.id, second.id);
    }

    // ---------- NULL GUARDS ----------

    @Test
    @Transactional
    void assign_nullStore_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.assign(null, product, warehouse));
    }

    @Test
    @Transactional
    void assign_nullProduct_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.assign(store, null, warehouse));
    }

    @Test
    @Transactional
    void assign_nullWarehouse_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.assign(store, product, null));
    }

    // ---------- PRODUCT / STORE RULES ----------

    @Test
    @Transactional
    void maxTwoWarehousesPerProductPerStore_enforced() {
        DbWarehouse w2 = createWarehouse("MWH.TEST2");
        DbWarehouse w3 = createWarehouse("MWH.TEST3");

        service.assign(store, product, warehouse);
        service.assign(store, product, w2);

        assertThrows(IllegalArgumentException.class,
                () -> service.assign(store, product, w3));
    }

    @Test
    @Transactional
    void maxThreeWarehousesPerStore_enforced() {
        DbWarehouse w2 = createWarehouse("MWH.TEST2");
        DbWarehouse w3 = createWarehouse("MWH.TEST3");
        DbWarehouse w4 = createWarehouse("MWH.TEST4");

        Product product2 = createProduct("TEST_PRODUCT_2");

        service.assign(store, product, warehouse);
        service.assign(store, product2, w2);
        service.assign(store, product, w3);

        assertThrows(IllegalArgumentException.class,
                () -> service.assign(store, product2, w4));
    }

    @Test
    @Transactional
    void maxFiveProductTypesPerWarehouse_enforced() {
        Product p2 = createProduct("P2");
        Product p3 = createProduct("P3");
        Product p4 = createProduct("P4");
        Product p5 = createProduct("P5");
        Product p6 = createProduct("P6");

        Store store2 = createStore("STORE2");

        service.assign(store, product, warehouse);
        service.assign(store, p2, warehouse);
        service.assign(store, p3, warehouse);
        service.assign(store2, p4, warehouse);
        service.assign(store2, p5, warehouse);

        assertThrows(IllegalArgumentException.class,
                () -> service.assign(store, p6, warehouse));
    }

    // ---------- EXTRA COVERAGE (IMPORTANT) ----------

    @Test
    @Transactional
    void sameProductWarehouseDifferentStore_allowed() {
        Store store2 = createStore("STORE2");

        StoreWarehouseProduct a1 = service.assign(store, product, warehouse);
        StoreWarehouseProduct a2 = service.assign(store2, product, warehouse);

        assertNotEquals(a1.id, a2.id);
    }

    @Test
    @Transactional
    void sameStoreWarehouseDifferentProducts_allowed() {
        Product product2 = createProduct("PRODUCT_X");

        StoreWarehouseProduct a1 = service.assign(store, product, warehouse);
        StoreWarehouseProduct a2 = service.assign(store, product2, warehouse);

        assertNotEquals(a1.id, a2.id);
    }

    @Test
    @Transactional
    void duplicateAssignment_doesNotCreateMultipleRecords() {
        service.assign(store, product, warehouse);
        service.assign(store, product, warehouse);
        service.assign(store, product, warehouse);

        long count = StoreWarehouseProduct
                .find("store.id = ?1 and product.id = ?2 and warehouse.id = ?3",
                        store.id, product.id, warehouse.id)
                .count();

        assertEquals(1, count);
    }

    @Test
    @Transactional
    void assignmentAfterFailure_stillWorks() {
        DbWarehouse w2 = createWarehouse("MWH.TEST2");
        DbWarehouse w3 = createWarehouse("MWH.TEST3");

        service.assign(store, product, warehouse);
        service.assign(store, product, w2);

        assertThrows(IllegalArgumentException.class,
                () -> service.assign(store, product, w3));

        Product newProduct = createProduct("NEW_PRODUCT");

        StoreWarehouseProduct valid =
                service.assign(store, newProduct, warehouse);

        assertNotNull(valid);
    }

    @Test
    @Transactional
    void warehouseWithZeroStock_assignmentHandled() {
        warehouse.stock = 0;
        warehouseRepository.persist(warehouse);

        StoreWarehouseProduct assignment =
                service.assign(store, product, warehouse);

        assertNotNull(assignment);
    }

    // ---------- HELPERS ----------

    private DbWarehouse createWarehouse(String prefix) {
        DbWarehouse w = new DbWarehouse();
        w.businessUnitCode = prefix + "_" + System.currentTimeMillis();
        w.location = "AMSTERDAM-001";
        w.capacity = 100;
        w.stock = 50;
        warehouseRepository.persist(w);
        return w;
    }

    private Product createProduct(String name) {
        Product p = new Product();
        p.name = name + "_" + System.currentTimeMillis();
        p.stock = 20;
        productRepository.persist(p);
        return p;
    }

    private Store createStore(String name) {
        Store s = new Store();
        s.name = name + "_" + System.currentTimeMillis();
        s.quantityProductsInStock = 10;
        s.persist();
        return s;
    }
}
