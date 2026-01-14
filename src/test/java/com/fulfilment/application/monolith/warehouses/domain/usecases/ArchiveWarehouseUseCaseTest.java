package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private InMemoryWarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = new InMemoryWarehouseStore();

    Warehouse active = new Warehouse();
    active.businessUnitCode = "MWH.001";
    active.location = "AMSTERDAM-001";
    active.capacity = 50;
    active.stock = 10;
    active.createdAt = LocalDateTime.now().minusDays(1);
    warehouseStore.create(active);

    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void archiveExistingWarehouseMarksAsArchived() {
    Warehouse toArchive = new Warehouse();
    toArchive.businessUnitCode = "MWH.001";

    useCase.archive(toArchive);

    Warehouse stored = warehouseStore.findByBusinessUnitCodeIncludingArchived("MWH.001");
    assertNotNull(stored.archivedAt);
    assertTrue(warehouseStore.updatedCalled);
  }

  @Test
  void archiveNonExistingWarehouseThrows() {
    Warehouse toArchive = new Warehouse();
    toArchive.businessUnitCode = "UNKNOWN";

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(toArchive));
  }

  private static class InMemoryWarehouseStore implements WarehouseStore {

    private final List<Warehouse> warehouses = new ArrayList<>();
    boolean updatedCalled = false;

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
      updatedCalled = true;
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

    Warehouse findByBusinessUnitCodeIncludingArchived(String buCode) {
      return warehouses.stream()
          .filter(w -> buCode.equals(w.businessUnitCode))
          .findFirst()
          .orElse(null);
    }
  }
}

