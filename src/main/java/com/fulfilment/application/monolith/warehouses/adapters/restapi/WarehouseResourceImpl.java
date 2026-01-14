package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;

  @Inject CreateWarehouseOperation createWarehouseOperation;

  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.list("archivedAt is null").stream()
        .map(this::toWarehouseResponse)
        .toList();
  }

  @Override
  @Transactional
  public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(
      @NotNull com.warehouse.api.beans.Warehouse data) {
    Warehouse warehouse = toDomainWarehouse(data);
    try {
      createWarehouseOperation.create(warehouse);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException(e.getMessage(), e);
    }
    // Flush to ensure the entity is persisted before querying
    Panache.getEntityManager().flush();
    // Retrieve the created entity to get the id
    var entity = warehouseRepository.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
    if (entity == null) {
      throw new jakarta.ws.rs.InternalServerErrorException("Failed to retrieve created warehouse");
    }
    return toWarehouseResponse(entity);
  }

  @Override
  public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
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

    return toWarehouseResponse(entity);
  }

  @Override
  @Transactional
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

    Warehouse warehouse = entity.toWarehouse();

    try {
      archiveWarehouseOperation.archive(warehouse);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException(e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull com.warehouse.api.beans.Warehouse data) {
    Warehouse warehouse = toDomainWarehouse(data);
    // Ensure the business unit code from the path is used as the identifier
    warehouse.businessUnitCode = businessUnitCode;

    try {
      replaceWarehouseOperation.replace(warehouse);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException(e.getMessage(), e);
    }

    // Flush to ensure the entity is persisted before querying
    Panache.getEntityManager().flush();
    // Retrieve the created entity to get the id
    var entity = warehouseRepository.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
    if (entity == null) {
      throw new jakarta.ws.rs.InternalServerErrorException("Failed to retrieve created warehouse");
    }
    return toWarehouseResponse(entity);
  }

  private com.warehouse.api.beans.Warehouse toWarehouseResponse(DbWarehouse entity) {
    var response = new com.warehouse.api.beans.Warehouse();
    response.setId(entity.id != null ? entity.id.toString() : null);
    response.setBusinessUnitCode(entity.businessUnitCode);
    response.setLocation(entity.location);
    response.setCapacity(entity.capacity);
    response.setStock(entity.stock);

    return response;
  }

  private Warehouse toDomainWarehouse(com.warehouse.api.beans.Warehouse warehouse) {
    var domain = new Warehouse();
    domain.businessUnitCode = warehouse.getBusinessUnitCode();
    domain.location = warehouse.getLocation();
    domain.capacity = warehouse.getCapacity();
    domain.stock = warehouse.getStock();
    return domain;
  }
}
