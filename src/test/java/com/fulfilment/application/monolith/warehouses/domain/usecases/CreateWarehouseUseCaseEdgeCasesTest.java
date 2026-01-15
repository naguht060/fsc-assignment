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
 * Comprehensive edge case tests for CreateWarehouseUseCase to improve branch coverage.
 */
public class CreateWarehouseUseCaseEdgeCasesTest {

  private InMemoryWarehouseStore warehouseStore;
  private FakeLocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new InMemoryWarehouseStore();
    locationResolver = new FakeLocationResolver();
    locationResolver.addLocation(new Location("ZWOLLE-001", 2, 300));
    locationResolver.addLocation(new Location("AMSTERDAM-001", 3, 500));
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void createWithNullWarehouseThrows() {
    assertThrows(IllegalArgumentException.class, () -> useCase.create(null));
  }

  @Test
  void createWithNullBusinessUnitCodeThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = null;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for null business unit code");
  }

  @Test
  void createWithBlankBusinessUnitCodeThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "   ";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for blank business unit code");
  }

  @Test
  void createWithNullLocationThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = null;
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for null location");
  }

  @Test
  void createWithBlankLocationThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "   ";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for blank location");
  }

  @Test
  void createWithNullCapacityThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = null;
    warehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for null capacity");
  }

  @Test
  void createWithZeroCapacityThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 0;
    warehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for zero capacity");
  }

  @Test
  void createWithNegativeCapacityThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = -50;
    warehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for negative capacity");
  }

  @Test
  void createWithNullStockThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = null;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for null stock");
  }

  @Test
  void createWithNegativeStockThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = -10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw for negative stock");
  }

  @Test
  void createWithStockExceedingCapacityThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 100;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(warehouse),
        "Should throw when stock exceeds capacity");
  }

  @Test
  void createWithMaxWarehouses() {
    // Location allows max 2 warehouses
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 100;
    w1.stock = 50;
    useCase.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.002";
    w2.location = "ZWOLLE-001";
    w2.capacity = 100;
    w2.stock = 50;
    useCase.create(w2);

    // Third warehouse should fail
    Warehouse w3 = new Warehouse();
    w3.businessUnitCode = "MWH.003";
    w3.location = "ZWOLLE-001";
    w3.capacity = 100;
    w3.stock = 50;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(w3),
        "Should throw when exceeding max warehouses for location");
  }

  @Test
  void createWithExceedingTotalCapacity() {
    // Location allows max 300 total capacity
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 200;
    w1.stock = 100;
    useCase.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "MWH.002";
    w2.location = "ZWOLLE-001";
    w2.capacity = 150; // Total would be 350 > 300
    w2.stock = 50;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(w2),
        "Should throw when exceeding max total capacity for location");
  }

  @Test
  void createSuccessfullySetsCreatedAtTimestamp() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;
    warehouse.createdAt = LocalDateTime.now().minusDays(10); // Pre-set createdAt should be overwritten

    useCase.create(warehouse);

    Warehouse created = warehouseStore.getAll().get(0);
    assertNotNull(created.createdAt);
    // Should be recent (not the old one)
    assertTrue(created.createdAt.isAfter(LocalDateTime.now().minusMinutes(1)));
  }

  @Test
  void createSuccessfullyClearsArchivedAt() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;
    warehouse.archivedAt = LocalDateTime.now(); // Pre-set archivedAt should be cleared

    useCase.create(warehouse);

    Warehouse created = warehouseStore.getAll().get(0);
    assertNull(created.archivedAt);
  }

  @Test
  void createWithArchivedWarehouse() {
    // Create an archived warehouse
    Warehouse archived = new Warehouse();
    archived.businessUnitCode = "MWH.001";
    archived.location = "ZWOLLE-001";
    archived.capacity = 50;
    archived.stock = 10;
    archived.createdAt = LocalDateTime.now();
    archived.archivedAt = LocalDateTime.now();
    warehouseStore.create(archived);

    // Try to create another with same business unit - should fail even if archived
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 50;
    newWarehouse.stock = 10;

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(newWarehouse),
        "Should throw for duplicate business unit code regardless of archived status");
  }

  @Test
  void createAtCapacityLimit() {
    // Create warehouse at exactly max capacity for location
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 300; // Max is 300
    warehouse.stock = 150;

    useCase.create(warehouse);

    List<Warehouse> all = warehouseStore.getAll();
    assertEquals(1, all.size());
    assertEquals(300, all.get(0).capacity);
  }

  @Test
  void createWithStockEqualsCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 50; // Stock equals capacity

    useCase.create(warehouse);

    Warehouse created = warehouseStore.getAll().get(0);
    assertEquals(50, created.stock);
    assertEquals(50, created.capacity);
  }

  // In-memory implementations for testing
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
