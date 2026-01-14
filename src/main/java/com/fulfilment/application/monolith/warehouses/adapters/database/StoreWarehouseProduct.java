package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
@Cacheable
public class StoreWarehouseProduct extends PanacheEntity {

  @ManyToOne public Store store;

  @ManyToOne public Product product;

  @ManyToOne public DbWarehouse warehouse;
}

