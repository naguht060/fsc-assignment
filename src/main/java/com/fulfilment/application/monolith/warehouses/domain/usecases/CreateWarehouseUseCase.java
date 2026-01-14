package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse data must be provided");
    }

    // Business Unit Code Verification
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code must be provided");
    }
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code already exists: " + warehouse.businessUnitCode);
    }

    // Location Validation
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new IllegalArgumentException("Location must be provided");
    }
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + warehouse.location);
    }

    // Capacity and Stock Validation
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be a positive integer");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }

    // Warehouse Creation Feasibility: max number of warehouses and total capacity for the location
    List<Warehouse> warehousesAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> w.archivedAt == null)
            .filter(w -> warehouse.location.equals(w.location))
            .toList();

    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
          "Maximum number of warehouses reached for location " + warehouse.location);
    }

    int currentTotalCapacity =
        warehousesAtLocation.stream()
            .mapToInt(w -> w.capacity != null ? w.capacity : 0)
            .sum();
    int newTotalCapacity = currentTotalCapacity + warehouse.capacity;
    if (newTotalCapacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Total capacity for location "
              + warehouse.location
              + " would exceed the maximum allowed: "
              + location.maxCapacity);
    }

    // Set timestamps for the new warehouse
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
