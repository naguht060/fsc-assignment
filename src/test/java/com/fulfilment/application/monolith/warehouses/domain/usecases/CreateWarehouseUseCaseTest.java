package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private InMemoryWarehouseStore warehouseStore;
  private FakeLocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new InMemoryWarehouseStore();
    locationResolver = new FakeLocationResolver();
    locationResolver.addLocation(new Location("AMSTERDAM-001", 3, 200));
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void createValidWarehousePersistsAndSetsTimestamps() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.100";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    useCase.create(warehouse);

    List<Warehouse> all = warehouseStore.getAll();
    assertEquals(1, all.size());
    Warehouse persisted = all.get(0);
    assertEquals("MWH.100", persisted.businessUnitCode);
    assertEquals("AMSTERDAM-001", persisted.location);
    assertNotNull(persisted.createdAt);
    // newly created warehouses must not be archived
    assertEquals(null, persisted.archivedAt);
  }

  @Test
  void createWithExistingBusinessUnitThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.100";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 50;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now();
    warehouseStore.create(existing);

    Warehouse duplicate = new Warehouse();
    duplicate.businessUnitCode = "MWH.100";
    duplicate.location = "AMSTERDAM-001";
    duplicate.capacity = 50;
    duplicate.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.create(duplicate));
  }

  @Test
  void createWithInvalidLocationThrows() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.101";
    warehouse.location = "INVALID";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
  }

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
      warehouses.remove(warehouse);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(w -> buCode.equals(w.businessUnitCode) && w.archivedAt == null)
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
          .filter(l -> identifier.equals(l.identification))
          .findFirst()
          .orElse(null);
    }
  }
}

