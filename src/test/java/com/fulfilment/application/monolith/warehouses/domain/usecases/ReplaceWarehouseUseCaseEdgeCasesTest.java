package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive edge case tests for ReplaceWarehouseUseCase to improve branch coverage.
 */
public class ReplaceWarehouseUseCaseEdgeCasesTest {

  private InMemoryWarehouseStore warehouseStore;
  private FakeLocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new InMemoryWarehouseStore();
    locationResolver = new FakeLocationResolver();
    locationResolver.addLocation(new Location("ZWOLLE-001", 2, 300));
    locationResolver.addLocation(new Location("AMSTERDAM-001", 3, 500));
    locationResolver.addLocation(new Location("ROTTERDAM-001", 2, 200));
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void replaceWithNullWarehouseThrows() {
    assertThrows(IllegalArgumentException.class, () -> useCase.replace(null));
  }

  @Test
  void replaceWithNullBusinessUnitCodeThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = null;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(warehouse));
  }

  @Test
  void replaceWithBlankBusinessUnitCodeThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "   ";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(warehouse));
  }

  @Test
  void replaceNonExistentWarehouseThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.999";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(warehouse));
  }

  @Test
  void replaceWithNullLocationThrows() {
    // Create existing warehouse
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    // Try to replace with null location
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = null;
    replacement.capacity = 50;
    replacement.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithBlankLocationThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "   ";
    replacement.capacity = 50;
    replacement.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithInvalidLocationThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "INVALID-LOC";
    replacement.capacity = 50;
    replacement.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithNullCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = null;
    replacement.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithZeroCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 0;
    replacement.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithNegativeCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = -50;
    replacement.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithNullStockThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 50;
    replacement.stock = null;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithNegativeStockThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 50;
    replacement.stock = -5;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithStockExceedingCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 50;
    replacement.stock = 100; // Exceeds capacity

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithDifferentStockThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 50;
    replacement.stock = 20; // Different from existing

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.replace(replacement),
        "Stock must match existing warehouse stock");
  }

  @Test
  void replaceWithCapacityLessThanStockThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 40;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 30; // Less than stock of 40
    replacement.stock = 40;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.replace(replacement),
        "New capacity must accommodate existing stock");
  }

  @Test
  void replaceExceedsMaxWarehousesAtNewLocation() {
    // Create 2 warehouses at ROTTERDAM (max 2)
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.100";
    w1.location = "ROTTERDAM-001";
    w1.capacity = 100;
    w1.stock = 50;
    w1.createdAt = LocalDateTime.now();
    warehouseStore.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.101";
    w2.location = "ROTTERDAM-001";
    w2.capacity = 100;
    w2.stock = 50;
    w2.createdAt = LocalDateTime.now();
    warehouseStore.create(w2);

    // Create warehouse at ZWOLLE
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    // Try to replace to ROTTERDAM - would exceed max
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "ROTTERDAM-001";
    replacement.capacity = 50;
    replacement.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.replace(replacement),
        "Should fail when exceeding max warehouses at new location");
  }

  @Test
  void replaceExceedsTotalCapacityAtNewLocation() {
    // Create warehouse at ROTTERDAM with high capacity
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.100";
    w1.location = "ROTTERDAM-001";
    w1.capacity = 150;
    w1.stock = 50;
    w1.createdAt = LocalDateTime.now();
    warehouseStore.create(w1);

    // Create warehouse at ZWOLLE
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 100;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    // Try to replace to ROTTERDAM with capacity 100 - would exceed max 200
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "ROTTERDAM-001";
    replacement.capacity = 100;
    replacement.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.replace(replacement),
        "Should fail when exceeding max capacity at new location");
  }

  @Test
  void replaceSuccessfully() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 75;
    replacement.stock = 10;

    // Just verify it doesn't throw - validation logic is tested elsewhere
    assertDoesNotThrow(() -> useCase.replace(replacement));
  }

  @Test
  void replaceSameLocation() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "ZWOLLE-001";
    replacement.capacity = 100;
    replacement.stock = 10;

    // Just verify it doesn't throw - validation logic is tested elsewhere
    assertDoesNotThrow(() -> useCase.replace(replacement));
  }

  // In-memory implementations
  private static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return new ArrayList<>(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      for (int i = 0; i < warehouses.size(); i++) {
        if (warehouses.get(i).businessUnitCode.equals(warehouse.businessUnitCode)) {
          warehouses.set(i, warehouse);
          return;
        }
      }
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.removeIf(w -> w.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String code) {
      return warehouses.stream()
          .filter(w -> w.businessUnitCode.equals(code))
          .findFirst()
          .orElse(null);
    }
  }

  private static class FakeLocationResolver implements LocationResolver {
    private final List<Location> locations = new ArrayList<>();

    void addLocation(Location location) {
      locations.add(location);
    }

    @Override
    public Location resolveByIdentifier(String identifier) {
      return locations.stream()
          .filter(l -> l.identification.equals(identifier))
          .findFirst()
          .orElse(null);
    }
  }
}
