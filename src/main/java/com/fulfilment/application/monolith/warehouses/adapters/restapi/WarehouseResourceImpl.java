package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse as DomainWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;

  @Inject CreateWarehouseOperation createWarehouseOperation;

  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    DomainWarehouse warehouse = toDomainWarehouse(data);
    try {
      createWarehouseOperation.create(warehouse);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException(e.getMessage(), e);
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    Long numericId;
    try {
      numericId = Long.valueOf(id);
    } catch (NumberFormatException e) {
      throw new jakarta.ws.rs.BadRequestException("Invalid warehouse id: " + id, e);
    }

    var entity = warehouseRepository.findById(numericId);
    if (entity == null || entity.archivedAt != null) {
      throw new jakarta.ws.rs.NotFoundException("Warehouse not found with id " + id);
    }

    return toWarehouseResponse(entity.toWarehouse());
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    Long numericId;
    try {
      numericId = Long.valueOf(id);
    } catch (NumberFormatException e) {
      throw new jakarta.ws.rs.BadRequestException("Invalid warehouse id: " + id, e);
    }

    var entity = warehouseRepository.findById(numericId);
    if (entity == null || entity.archivedAt != null) {
      throw new jakarta.ws.rs.NotFoundException("Warehouse not found with id " + id);
    }

    DomainWarehouse warehouse = entity.toWarehouse();

    try {
      archiveWarehouseOperation.archive(warehouse);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException(e.getMessage(), e);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    DomainWarehouse warehouse = toDomainWarehouse(data);
    // Ensure the business unit code from the path is used as the identifier
    warehouse.businessUnitCode = businessUnitCode;

    try {
      replaceWarehouseOperation.replace(warehouse);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException(e.getMessage(), e);
    }

    return toWarehouseResponse(warehouse);
  }

  private Warehouse toWarehouseResponse(DomainWarehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private DomainWarehouse toDomainWarehouse(Warehouse warehouse) {
    var domain = new DomainWarehouse();
    domain.businessUnitCode = warehouse.getBusinessUnitCode();
    domain.location = warehouse.getLocation();
    domain.capacity = warehouse.getCapacity();
    domain.stock = warehouse.getStock();
    return domain;
  }
}
