package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreResourceTest {

  @Test
  @Order(1)
  public void testGetAllStores() {
    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  @Order(2)
  public void testGetStoreById() {
    given()
        .when()
        .get("/store/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"));
  }

  @Test
  @Order(3)
  public void testGetStoreByIdNotFound() {
    given()
        .when()
        .get("/store/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testCreateStore() {
    Store store = new Store();
    store.name = "TEST_STORE_NEW";
    store.quantityProductsInStock = 100;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(store)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body(containsString("TEST_STORE_NEW"));
  }

  @Test
  @Order(5)
  public void testUpdateStore() {
    // First create a store to update
    Store newStore = new Store();
    newStore.name = "STORE_TO_UPDATE";
    newStore.quantityProductsInStock = 50;
    
    var response = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(newStore)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .body()
        .jsonPath();
    
    Long storeId = response.getLong("id");
    
    // Now update it
    Store updateStore = new Store();
    updateStore.name = "UPDATED_STORE_NAME";
    updateStore.quantityProductsInStock = 75;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updateStore)
        .when()
        .put("/store/" + storeId)
        .then()
        .statusCode(200)
        .body(containsString("UPDATED_STORE_NAME"));
  }

  @Test
  @Order(6)
  public void testUpdateStoreWithoutNameShouldFail() {
    Store store = new Store();
    store.quantityProductsInStock = 50;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(store)
        .when()
        .put("/store/1")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(7)
  public void testPatchStore() {
    // First create a store to patch
    Store newStore = new Store();
    newStore.name = "STORE_TO_PATCH";
    newStore.quantityProductsInStock = 100;
    
    var response = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(newStore)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .body()
        .jsonPath();
    
    Long storeId = response.getLong("id");
    
    // Now patch it
    Store patchStore = new Store();
    patchStore.name = "PATCHED_STORE_NAME";
    patchStore.quantityProductsInStock = 200;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(patchStore)
        .when()
        .patch("/store/" + storeId)
        .then()
        .statusCode(200)
        .body(containsString("PATCHED_STORE_NAME"));
  }

  @Test
  @Order(8)
  public void testDeleteStore() {
    // First create a store to delete
    Store newStore = new Store();
    newStore.name = "STORE_TO_DELETE";
    newStore.quantityProductsInStock = 10;
    
    var response = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(newStore)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .body()
        .jsonPath();
    
    Long storeId = response.getLong("id");
    
    // Now delete it
    given()
        .when()
        .delete("/store/" + storeId)
        .then()
        .statusCode(204);

    // Verify it's deleted
    given()
        .when()
        .get("/store/" + storeId)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(9)
  public void testCreateStoreWithNameNull() {
    Store store = new Store();
    store.quantityProductsInStock = 100;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(store)
        .when()
        .post("/store")
        .then()
        .statusCode(201);  // Created - no validation for null name in API
  }

  @Test
  @Order(10)
  public void testUpdateStoreNotFound() {
    Store store = new Store();
    store.name = "NOT_FOUND";
    store.quantityProductsInStock = 50;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(store)
        .when()
        .put("/store/999999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(11)
  public void testDeleteStoreNotFound() {
    given()
        .when()
        .delete("/store/999999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(12)
  public void testPatchStoreNotFound() {
    Store store = new Store();
    store.name = "PATCH_NOT_FOUND";
    store.quantityProductsInStock = 100;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(store)
        .when()
        .patch("/store/999999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(13)
  public void testPatchStoreWithoutNameShouldFail() {
    Store store = new Store();
    store.quantityProductsInStock = 50;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(store)
        .when()
        .patch("/store/1")
        .then()
        .statusCode(422);
  }
}
