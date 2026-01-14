package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class FulfilmentAssignmentService {

  @Transactional
  public StoreWarehouseProduct assign(Store store, Product product, DbWarehouse warehouse) {
    if (store == null || product == null || warehouse == null) {
      throw new IllegalArgumentException("Store, Product and Warehouse must be provided");
    }

    // Avoid duplicate assignment
    StoreWarehouseProduct existing =
        StoreWarehouseProduct.find(
                "store = ?1 and product = ?2 and warehouse = ?3", store, product, warehouse)
            .firstResult();
    if (existing != null) {
      return existing;
    }

    // 1. Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
    List<StoreWarehouseProduct> forStoreAndProduct =
        StoreWarehouseProduct.list("store = ?1 and product = ?2", store, product);
    Set<Long> warehouseIdsForStoreAndProduct = new HashSet<>();
    for (StoreWarehouseProduct rel : forStoreAndProduct) {
      if (rel.warehouse != null && rel.warehouse.id != null) {
        warehouseIdsForStoreAndProduct.add(rel.warehouse.id);
      }
    }
    if (!warehouseIdsForStoreAndProduct.contains(warehouse.id)
        && warehouseIdsForStoreAndProduct.size() >= 2) {
      throw new IllegalArgumentException(
          "A product can be fulfilled by at most 2 warehouses per store");
    }

    // 2. Each Store can be fulfilled by a maximum of 3 different Warehouses
    List<StoreWarehouseProduct> forStore =
        StoreWarehouseProduct.list("store = ?1", store);
    Set<Long> warehouseIdsForStore = new HashSet<>();
    for (StoreWarehouseProduct rel : forStore) {
      if (rel.warehouse != null && rel.warehouse.id != null) {
        warehouseIdsForStore.add(rel.warehouse.id);
      }
    }
    if (!warehouseIdsForStore.contains(warehouse.id) && warehouseIdsForStore.size() >= 3) {
      throw new IllegalArgumentException(
          "A store can be fulfilled by at most 3 different warehouses");
    }

    // 3. Each Warehouse can store maximally 5 types of Products
    List<StoreWarehouseProduct> forWarehouse =
        StoreWarehouseProduct.list("warehouse = ?1", warehouse);
    Set<Long> productIdsForWarehouse = new HashSet<>();
    for (StoreWarehouseProduct rel : forWarehouse) {
      if (rel.product != null && rel.product.id != null) {
        productIdsForWarehouse.add(rel.product.id);
      }
    }
    if (!productIdsForWarehouse.contains(product.id) && productIdsForWarehouse.size() >= 5) {
      throw new IllegalArgumentException(
          "A warehouse can store at most 5 different product types");
    }

    StoreWarehouseProduct relation = new StoreWarehouseProduct();
    relation.store = store;
    relation.product = product;
    relation.warehouse = warehouse;
    relation.persist();

    return relation;
  }
}

