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
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseResourceImplTest {

    @Inject WarehouseRepository warehouseRepository;

    private static final String BASE_PATH = "/warehouse";

    @BeforeEach
    @Transactional
    void setUp() {
        // Ensure at least one warehouse exists for GET tests
        if (warehouseRepository.findByBusinessUnitCode("MWH.001") == null) {
            Warehouse warehouse = new Warehouse();
            warehouse.businessUnitCode = "MWH.001";
            warehouse.location = "ZWOLLE-001";
            warehouse.capacity = 30;
            warehouse.stock = 10;
            warehouseRepository.create(warehouse);
        }
    }

    // ---------- LIST ----------

    @Test
    void listAllWarehouses_returns200() {
        given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body(containsString("MWH.001"));
    }

    // ---------- GET BY ID ----------

    @Test
    void getWarehouseById_success() {
        String id =
                given()
                        .when()
                        .get(BASE_PATH)
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getString("[0].id");

        given()
                .when()
                .get(BASE_PATH + "/" + id)
                .then()
                .statusCode(200)
                .body(containsString("MWH.001"));
    }

    @Test
    void getWarehouseById_notFound() {
        given()
                .when()
                .get(BASE_PATH + "/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void getWarehouseById_invalidFormat() {
        given()
                .when()
                .get(BASE_PATH + "/invalid")
                .then()
                .statusCode(400);
    }

    // ---------- CREATE ----------

    @Test
    void createWarehouse_success() {
        long ts = System.currentTimeMillis();
        com.warehouse.api.beans.Warehouse warehouse =
                new com.warehouse.api.beans.Warehouse();

        warehouse.setBusinessUnitCode("MWH.NEW_" + ts);
        warehouse.setLocation("AMSTERDAM-002");
        warehouse.setCapacity(25);
        warehouse.setStock(5);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(warehouse)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body(containsString("MWH.NEW"));
    }

    @Test
    void createWarehouse_invalidLocation_returns400() {
        com.warehouse.api.beans.Warehouse warehouse =
                new com.warehouse.api.beans.Warehouse();

        warehouse.setBusinessUnitCode("MWH.BAD_" + System.currentTimeMillis());
        warehouse.setLocation("INVALID_LOCATION");
        warehouse.setCapacity(20);
        warehouse.setStock(5);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(warehouse)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void createWarehouse_missingPayload_returns400() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    // ---------- ARCHIVE ----------

    @Test
    void archiveWarehouse_success() {
        long ts = System.currentTimeMillis();
        com.warehouse.api.beans.Warehouse warehouse =
                new com.warehouse.api.beans.Warehouse();

        warehouse.setBusinessUnitCode("MWH.ARCH_" + ts);
        warehouse.setLocation("EINDHOVEN-001");
        warehouse.setCapacity(30);
        warehouse.setStock(5);

        String id =
                given()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(warehouse)
                        .post(BASE_PATH)
                        .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getString("id");

        given()
                .when()
                .delete(BASE_PATH + "/" + id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get(BASE_PATH + "/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    void archiveWarehouse_notFound() {
        given()
                .when()
                .delete(BASE_PATH + "/999999")
                .then()
                .statusCode(404);
    }

    // ---------- REPLACEMENT ----------

    @Test
    void replaceWarehouse_success() {
        String id =
                given()
                        .when()
                        .get(BASE_PATH)
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getString("[0].businessUnitCode");

        com.warehouse.api.beans.Warehouse replacement =
                new com.warehouse.api.beans.Warehouse();

        replacement.setBusinessUnitCode(id);
        replacement.setLocation("AMSTERDAM-002");
        replacement.setCapacity(30);
        replacement.setStock(10);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(replacement)
                .when()
                .post(BASE_PATH + "/" + id + "/replacement")
                .then()
                .statusCode(200)
                .body(containsString(id));
    }

    // ---------- NEGATIVE / EDGE ----------

    @Test
    void replaceWarehouse_notFound() {
        com.warehouse.api.beans.Warehouse replacement =
                new com.warehouse.api.beans.Warehouse();

        replacement.setBusinessUnitCode("UNKNOWN");
        replacement.setLocation("AMSTERDAM-002");
        replacement.setCapacity(10);
        replacement.setStock(5);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(replacement)
                .when()
                .post(BASE_PATH + "/UNKNOWN/replacement")
                .then()
                .statusCode(404);
    }

    @Test
    void unsupportedMethod_returns405() {
        given()
                .when()
                .patch(BASE_PATH)
                .then()
                .statusCode(405);
    }
}