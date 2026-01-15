package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

    private static final String PATH = "/warehouse";

    // ---------- LIST ----------

    @Test
    void listWarehouses_returnsInitialData() {
        given()
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body(
                        containsString("MWH.001"),
                        containsString("MWH.012"),
                        containsString("MWH.023"));
    }

    // ---------- GET ----------

    @Test
    void getWarehouse_notFound_returns404() {
        given()
                .when()
                .get(PATH + "/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void getWarehouse_invalidId_returns400() {
        given()
                .when()
                .get(PATH + "/invalid")
                .then()
                .statusCode(400);
    }

    // ---------- CREATE ----------

    @Test
    void createWarehouse_success() {
        long ts = System.currentTimeMillis();
        String body =
                """
                {
                  "businessUnitCode": "MWH.IT.%d",
                  "location": "AMSTERDAM-002",
                  "capacity": 25,
                  "stock": 5
                }
                """
                        .formatted(ts);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when()
                .post(PATH)
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body(containsString("MWH.IT."));
    }

    @Test
    void createWarehouse_invalidLocation_returns400() {
        String body =
                """
                {
                  "businessUnitCode": "MWH.BAD",
                  "location": "INVALID",
                  "capacity": 20,
                  "stock": 5
                }
                """;

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when()
                .post(PATH)
                .then()
                .statusCode(400);
    }

    // ---------- ARCHIVE ----------

    @Test
    void archiveWarehouse_success() {
        long ts = System.currentTimeMillis();
        String body =
                """
                {
                  "businessUnitCode": "MWH.ARCH.%d",
                  "location": "EINDHOVEN-001",
                  "capacity": 30,
                  "stock": 5
                }
                """
                        .formatted(ts);

        String id =
                given()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .post(PATH)
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .extract()
                        .jsonPath()
                        .getString("id");

        given()
                .when()
                .delete(PATH + "/" + id)
                .then()
                .statusCode(204);

        given()
                .when()
                .get(PATH + "/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    void archiveWarehouse_notFound_returns404() {
        given()
                .when()
                .delete(PATH + "/999999")
                .then()
                .statusCode(404);
    }

    // ---------- IDENTITY / IDEMPOTENCY ----------

    @Test
    void archiveWarehouse_twice_isIdempotent() {
        given()
                .when()
                .delete(PATH + "/999999")
                .then()
                .statusCode(404);

        given()
                .when()
                .delete(PATH + "/999999")
                .then()
                .statusCode(404);
    }

    // ---------- REPLACEMENT ----------

    @Test
    void replaceWarehouse_notFound_returns404() {
        String body =
                """
                {
                  "businessUnitCode": "UNKNOWN",
                  "location": "AMSTERDAM-002",
                  "capacity": 10,
                  "stock": 5
                }
                """;

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .when()
                .post(PATH + "/UNKNOWN/replacement")
                .then()
                .statusCode(404);
    }

    // ---------- METHOD SAFETY ----------

    @Test
    void unsupportedMethod_returns405() {
        given()
                .when()
                .patch(PATH)
                .then()
                .statusCode(405);
    }
}