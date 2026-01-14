package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseResourceImplTest {

  @Inject WarehouseRepository warehouseRepository;

  @BeforeEach
  @Transactional
  void setUp() {
    // Ensure test data exists
    var existing = warehouseRepository.findByBusinessUnitCode("MWH.001");
    if (existing == null) {
      Warehouse warehouse = new Warehouse();
      warehouse.businessUnitCode = "MWH.001";
      warehouse.location = "ZWOLLE-001";
      warehouse.capacity = 100;
      warehouse.stock = 10;
      warehouseRepository.create(warehouse);
    }
  }

  @Test
  @Order(1)
  public void testListAllWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  @Order(2)
  public void testGetWarehouseById() {
    given()
        .when()
        .get("/warehouse/1")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"));
  }

  @Test
  @Order(3)
  public void testGetWarehouseByIdNotFound() {
    given()
        .when()
        .get("/warehouse/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testGetWarehouseByIdInvalidFormat() {
    given()
        .when()
        .get("/warehouse/invalid")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(5)
  public void testCreateWarehouse() {
    // Use AMSTERDAM-002: maxNumberOfWarehouses=3, maxCapacity=75, no existing warehouses in import.sql
    // This allows multiple warehouses, so tests won't conflict
    long timestamp = System.currentTimeMillis();
    com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode("MWH.100_" + timestamp); // Unique BU code
    warehouse.setLocation("AMSTERDAM-002"); // Allows up to 3 warehouses, maxCapacity=75
    warehouse.setCapacity(30); // Well within the 75 capacity limit
    warehouse.setStock(10);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(warehouse)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201) // OpenAPI spec says 201 for POST
        .body(containsString("MWH.100"));
  }

  @Test
  @Order(6)
  public void testCreateWarehouseWithInvalidLocation() {
    com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode("MWH.101_" + System.currentTimeMillis());
    warehouse.setLocation("INVALID_LOCATION");
    warehouse.setCapacity(50);
    warehouse.setStock(10);

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(warehouse)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(7)
  public void testArchiveWarehouse() {
    // First create a warehouse to archive using EINDHOVEN-001: maxNumberOfWarehouses=2, maxCapacity=70
    long timestamp = System.currentTimeMillis();
    com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode("MWH.200_" + timestamp);
    warehouse.setLocation("EINDHOVEN-001"); // maxNumberOfWarehouses=2, maxCapacity=70, no existing warehouses
    warehouse.setCapacity(30);
    warehouse.setStock(5);

    var response = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(warehouse)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201)
        .extract()
        .body()
        .jsonPath();
    
    String warehouseId = response.getString("id");

    // Now archive it
    given()
        .when()
        .delete("/warehouse/" + warehouseId)
        .then()
        .statusCode(204);

    // Verify it's archived (should not appear in list)
    given()
        .when()
        .get("/warehouse/" + warehouseId)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(8)
  public void testArchiveWarehouseNotFound() {
    given()
        .when()
        .delete("/warehouse/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(9)
  public void testReplaceWarehouse() {
    // First ensure MWH.001 exists and get its stock
    var existingResponse = given()
        .when()
        .get("/warehouse/1")
        .then()
        .statusCode(200)
        .extract()
        .body()
        .jsonPath();
    
    Integer existingStock = existingResponse.getInt("stock");
    
    // For replace: MWH.001 is at ZWOLLE-001
    // Replacement archives the old warehouse first, then creates new one
    // ZWOLLE-001: maxNumberOfWarehouses=1, maxCapacity=40
    // Current MWH.001 has capacity=100 (exceeds maxCapacity, but that's test data issue)
    // For replacement at same location, new capacity must be <= maxCapacity (40)
    // Or we can move to a different location
    // Let's use AMSTERDAM-002: maxNumberOfWarehouses=3, maxCapacity=75
    // Note: testCreateWarehouse (Order 5) already created a warehouse here with capacity 30
    // So we have 75 - 30 = 45 available capacity
    com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode("MWH.001");
    warehouse.setLocation("AMSTERDAM-002"); // Use a location with available capacity
    warehouse.setCapacity(45); // 30 (existing) + 45 (new) = 75, which is exactly the max
    warehouse.setStock(existingStock); // Must match existing stock

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(warehouse)
        .when()
        .post("/warehouse/MWH.001/replacement") // Correct endpoint from OpenAPI spec
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"));
  }
}
