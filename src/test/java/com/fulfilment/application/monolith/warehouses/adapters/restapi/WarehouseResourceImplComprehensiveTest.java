package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Comprehensive integration tests for WarehouseResourceImpl to cover all REST endpoints and error paths.
 * Tests: list warehouses, create, get by ID, archive, replace, error handling for invalid IDs and states.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseResourceImplComprehensiveTest {

  @Test
  @Order(1)
  public void testListAllWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("ZWOLLE-001"), containsString("AMSTERDAM-001"), containsString("TILBURG-001"));
  }

  @Test
  @Order(2)
  public void testGetWarehouseByValidId() {
    given()
        .when()
        .get("/warehouse/1")
        .then()
        .statusCode(200)
        .body(containsString("ZWOLLE-001"), containsString("MWH.001"));
  }

  @Test
  @Order(3)
  public void testGetWarehouseByInvalidStringId() {
    given()
        .when()
        .get("/warehouse/invalid-id")
        .then()
        .statusCode(400)
        .body(containsString("Invalid warehouse id"));
  }

  @Test
  @Order(4)
  public void testGetWarehouseByNonExistentId() {
    given()
        .when()
        .get("/warehouse/999999")
        .then()
        .statusCode(404)
        .body(containsString("Warehouse not found"));
  }

  @Test
  @Order(5)
  public void testGetArchivedWarehouseReturns404() {
    // First archive a warehouse
    given()
        .when()
        .delete("/warehouse/3")
        .then()
        .statusCode(204);

    // Now try to get it - should return 404
    given()
        .when()
        .get("/warehouse/3")
        .then()
        .statusCode(404)
        .body(containsString("Warehouse not found"));
  }

  @Test
  @Order(6)
  public void testCreateWarehouseSuccessfully() {
    var createRequest = """
        {
          "businessUnitCode": "MWH.NEW.001",
          "location": "ROTTERDAM-001",
          "capacity": 150,
          "stock": 45
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201)
        .body(containsString("MWH.NEW.001"), containsString("ROTTERDAM-001"));
  }

  @Test
  @Order(7)
  public void testCreateWarehouseWithNullBusinessUnitCode() {
    var createRequest = """
        {
          "location": "ROTTERDAM-001",
          "capacity": 150,
          "stock": 45
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(8)
  public void testCreateWarehouseWithNullLocation() {
    var createRequest = """
        {
          "businessUnitCode": "MWH.NEW.002",
          "capacity": 150,
          "stock": 45
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(9)
  public void testCreateWarehouseWithNullCapacity() {
    var createRequest = """
        {
          "businessUnitCode": "MWH.NEW.003",
          "location": "ROTTERDAM-002",
          "stock": 45
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(10)
  public void testCreateWarehouseWithNullStock() {
    var createRequest = """
        {
          "businessUnitCode": "MWH.NEW.004",
          "location": "ROTTERDAM-003",
          "capacity": 150
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(11)
  public void testArchiveWarehouseSuccessfully() {
    given()
        .when()
        .delete("/warehouse/2")
        .then()
        .statusCode(204);
  }

  @Test
  @Order(12)
  public void testArchiveWarehouseWithInvalidId() {
    given()
        .when()
        .delete("/warehouse/not-a-number")
        .then()
        .statusCode(400)
        .body(containsString("Invalid warehouse id"));
  }

  @Test
  @Order(13)
  public void testArchiveNonExistentWarehouse() {
    given()
        .when()
        .delete("/warehouse/888888")
        .then()
        .statusCode(404)
        .body(containsString("Warehouse not found"));
  }

  @Test
  @Order(14)
  public void testReplaceWarehouseSuccessfully() {
    // First create one to replace
    var createRequest = """
        {
          "businessUnitCode": "MWH.REPLACE.001",
          "location": "HAGUE-001",
          "capacity": 150,
          "stock": 50
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201);

    // Now replace it - stock must match the existing warehouse
    var replaceRequest = """
        {
          "location": "HAGUE-UPDATED",
          "capacity": 200,
          "stock": 50
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(replaceRequest)
        .when()
        .post("/warehouse/MWH.REPLACE.001/replacement")
        .then()
        .statusCode(200)
        .body(containsString("HAGUE-UPDATED"), containsString("200"));
  }

  @Test
  @Order(15)
  public void testReplaceWarehouseWithNullLocation() {
    var replaceRequest = """
        {
          "capacity": 250,
          "stock": 75
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(replaceRequest)
        .when()
        .post("/warehouse/MWH.001/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(16)
  public void testReplaceWarehouseWithNullCapacity() {
    var replaceRequest = """
        {
          "location": "HAGUE-UPDATED",
          "stock": 75
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(replaceRequest)
        .when()
        .post("/warehouse/MWH.001/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(17)
  public void testReplaceWarehouseWithNullStock() {
    var replaceRequest = """
        {
          "location": "HAGUE-UPDATED",
          "capacity": 250
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(replaceRequest)
        .when()
        .post("/warehouse/MWH.001/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(18)
  public void testReplaceNonExistentWarehouse() {
    var replaceRequest = """
        {
          "location": "HAGUE-UPDATED",
          "capacity": 250,
          "stock": 75
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(replaceRequest)
        .when()
        .post("/warehouse/MWH.DOES.NOT.EXIST/replacement")
        .then()
        .statusCode(400)
        .body(containsString("Warehouse not found"));
  }

  @Test
  @Order(19)
  public void testReplaceArchivedWarehouse() {
    // Archive a warehouse first
    given()
        .when()
        .delete("/warehouse/1")
        .then()
        .statusCode(204);

    // Now try to replace it
    var replaceRequest = """
        {
          "location": "HAGUE-UPDATED",
          "capacity": 250,
          "stock": 75
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(replaceRequest)
        .when()
        .post("/warehouse/MWH.001/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(20)
  public void testCreateWarehouseWithInvalidCapacity() {
    var createRequest = """
        {
          "businessUnitCode": "MWH.BAD.001",
          "location": "INVALID-LOC",
          "capacity": 0,
          "stock": 45
        }
        """;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }
}
