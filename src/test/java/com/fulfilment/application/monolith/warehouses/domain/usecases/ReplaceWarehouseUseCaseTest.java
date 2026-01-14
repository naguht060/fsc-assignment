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

public class ReplaceWarehouseUseCaseTest {

  private InMemoryWarehouseStore warehouseStore;
  private FakeLocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new InMemoryWarehouseStore();
    locationResolver = new FakeLocationResolver();
    locationResolver.addLocation(new Location("AMSTERDAM-001", 3, 200));

    // existing active warehouse to be replaced
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 50;
    existing.stock = 20;
    existing.createdAt = LocalDateTime.now().minusDays(1);
    warehouseStore.create(existing);

    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void replaceWarehouseHappyPathArchivesOldAndCreatesNew() {
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 60;
    replacement.stock = 20; // must match existing stock

    useCase.replace(replacement);

    // there should be exactly one active warehouse for the BU, with the replacement data
    Warehouse active =
        warehouseStore.findByBusinessUnitCode("MWH.001"); // ignores archived warehouses
    assertNotNull(active);
    assertEquals("AMSTERDAM-001", active.location);
    assertEquals(60, active.capacity);
    assertEquals(20, active.stock);

    // and one archived warehouse
    long archivedCount =
        warehouseStore.getAll().stream().filter(w -> w.archivedAt != null).count();
    assertEquals(1, archivedCount);
  }

  @Test
  void replaceWithDifferentStockThrows() {
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 60;
    replacement.stock = 10; // mismatch on purpose

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceWithInsufficientCapacityThrows() {
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 10; // cannot accommodate existing stock (20)
    replacement.stock = 20;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
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
        if (warehouses.get(i).businessUnitCode.equals(warehouse.businessUnitCode)
            && warehouses.get(i).createdAt.equals(warehouse.createdAt)) {
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

