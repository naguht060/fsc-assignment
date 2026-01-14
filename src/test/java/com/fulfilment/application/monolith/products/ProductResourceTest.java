package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import java.math.BigDecimal;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceTest {

  @Test
  @Order(1)
  public void testGetAllProducts() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body(containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  @Order(2)
  public void testGetProductById() {
    // Use ID 2 or 3 which should exist from import.sql
    given()
        .when()
        .get("/product/2")
        .then()
        .statusCode(200)
        .body(containsString("KALLAX"));
  }

  @Test
  @Order(3)
  public void testGetProductByIdNotFound() {
    given()
        .when()
        .get("/product/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testCreateProduct() {
    Product product = new Product();
    product.name = "TEST_PRODUCT";
    product.description = "Test Description";
    product.price = new BigDecimal("99.99");
    product.stock = 50;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body(containsString("TEST_PRODUCT"));
  }

  @Test
  @Order(5)
  public void testCreateProductWithIdShouldFail() {
    Product product = new Product();
    product.id = 999L;
    product.name = "INVALID_PRODUCT";

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(6)
  public void testUpdateProduct() {
    Product product = new Product();
    product.name = "UPDATED_KALLAX";
    product.description = "Updated Description";
    product.price = new BigDecimal("149.99");
    product.stock = 25;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .put("/product/2")
        .then()
        .statusCode(200)
        .body(containsString("UPDATED_KALLAX"));
  }

  @Test
  @Order(7)
  public void testUpdateProductWithoutNameShouldFail() {
    Product product = new Product();
    product.description = "No name";

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .put("/product/2")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(8)
  public void testUpdateProductNotFound() {
    Product product = new Product();
    product.name = "NOT_FOUND";

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .put("/product/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(9)
  public void testDeleteProduct() {
    given()
        .when()
        .delete("/product/3")
        .then()
        .statusCode(204);

    // Verify it's deleted
    given()
        .when()
        .get("/product/3")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(10)
  public void testDeleteProductNotFound() {
    given()
        .when()
        .delete("/product/999")
        .then()
        .statusCode(404);
  }
}
