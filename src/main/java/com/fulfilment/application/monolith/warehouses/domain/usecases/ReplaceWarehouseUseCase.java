package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new IllegalArgumentException("New warehouse data must be provided");
    }
    if (newWarehouse.businessUnitCode == null || newWarehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code must be provided");
    }

    Warehouse existing =
        warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse not found for business unit code: " + newWarehouse.businessUnitCode);
    }

    // Location validation for the new warehouse
    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new IllegalArgumentException("Location must be provided");
    }
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + newWarehouse.location);
    }

    if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be a positive integer");
    }
    if (newWarehouse.stock == null || newWarehouse.stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
    if (newWarehouse.stock > newWarehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }

    // Additional Validations for Replacing a Warehouse
    // Stock Matching
    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new IllegalArgumentException("New warehouse stock must match existing warehouse stock");
    }

    // Capacity Accommodation
    if (newWarehouse.capacity < existing.stock) {
      throw new IllegalArgumentException(
          "New warehouse capacity must accommodate the stock of the warehouse being replaced");
    }

    // Warehouse Creation Feasibility at the new location (considering the existing warehouse will
    // be archived)
    List<Warehouse> activeWarehouses =
        warehouseStore.getAll().stream().filter(w -> w.archivedAt == null).toList();

    List<Warehouse> warehousesAtLocation =
        activeWarehouses.stream()
            .filter(w -> !w.businessUnitCode.equals(existing.businessUnitCode))
            .filter(w -> newWarehouse.location.equals(w.location))
            .toList();

    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
          "Maximum number of warehouses reached for location " + newWarehouse.location);
    }

    int currentTotalCapacityAtLocation =
        warehousesAtLocation.stream()
            .mapToInt(w -> w.capacity != null ? w.capacity : 0)
            .sum();
    int newTotalCapacity = currentTotalCapacityAtLocation + newWarehouse.capacity;
    if (newTotalCapacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Total capacity for location "
              + newWarehouse.location
              + " would exceed the maximum allowed: "
              + location.maxCapacity);
    }

    // Archive the existing warehouse
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    // Create the new warehouse (with reset timestamps)
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;

    warehouseStore.create(newWarehouse);
  }
}
