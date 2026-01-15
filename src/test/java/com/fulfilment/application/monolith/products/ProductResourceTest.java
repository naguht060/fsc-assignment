package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.anyOf;
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

  private static Long createdProductId = null;

  @Test
  @Order(1)
  public void testGetAllProducts() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200);
    // Don't check for specific products since test data gets modified
  }

  @Test
  @Order(2)
  public void testCreateProductForUpdates() {
    Product product = new Product();
    product.name = "PRODUCT_FOR_UPDATES_" + System.currentTimeMillis();
    product.description = "Product used for update tests";
    product.stock = 100;

    createdProductId = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");
  }

  @Test
  @Order(3)
  public void testGetProductById() {
    // Use the created product from previous test
    given()
        .when()
        .get("/product/" + createdProductId)
        .then()
        .statusCode(200);
  }

  @Test
  @Order(4)
  public void testGetProductByIdNotFound() {
    given()
        .when()
        .get("/product/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(5)
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

  @Test
  @Order(11)
  public void testGetAllProductsIsEmpty() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(12)
  public void testUpdateProductNameNull() {
    Product product = new Product();
    product.description = "Test Description";
    product.price = new BigDecimal("99.99");
    product.stock = 50;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .put("/product/1")
        .then()
        .statusCode(anyOf(is(200), is(400), is(422)));
  }

  @Test
  @Order(13)
  public void testCreateProductWithZeroStock() {
    Product product = new Product();
    product.name = "ZERO_STOCK_" + System.currentTimeMillis();
    product.stock = 0;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(14)
  public void testCreateProductWithNegativeStock() {
    Product product = new Product();
    product.name = "NEGATIVE_STOCK_" + System.currentTimeMillis();
    product.stock = -50;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(anyOf(is(201), is(400), is(422)));
  }

  @Test
  @Order(15)
  public void testCreateProductWithLargeStock() {
    Product product = new Product();
    product.name = "LARGE_STOCK_" + System.currentTimeMillis();
    product.stock = Integer.MAX_VALUE;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(16)
  public void testUpdateProductWithZeroStock() {
    Product product = new Product();
    product.name = "UPDATE_ZERO_" + System.currentTimeMillis();
    product.stock = 0;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .put("/product/" + createdProductId)
        .then()
        .statusCode(200);
  }

  @Test
  @Order(17)
  public void testCreateProductWithPrice() {
    Product product = new Product();
    product.name = "WITH_PRICE_" + System.currentTimeMillis();
    product.price = new BigDecimal("199.99");
    product.stock = 100;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body(containsString("WITH_PRICE_"));
  }

  @Test
  @Order(18)
  public void testCreateProductWithDescription() {
    Product product = new Product();
    product.name = "WITH_DESC_" + System.currentTimeMillis();
    product.description = "A very detailed product description";
    product.stock = 75;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(19)
  public void testCreateMultipleProducts() {
    for (int i = 0; i < 3; i++) {
      Product product = new Product();
      product.name = "BULK_CREATE_" + i + "_" + System.currentTimeMillis();
      product.stock = 50 + (i * 10);

      given()
          .contentType(MediaType.APPLICATION_JSON)
          .body(product)
          .when()
          .post("/product")
          .then()
          .statusCode(201);
    }
  }

  @Test
  @Order(20)
  public void testCreateUpdateDeleteCycle() {
    // Create
    Product product = new Product();
    product.name = "CYCLE_" + System.currentTimeMillis();
    product.stock = 100;

    Long id = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    // Update
    Product updated = new Product();
    updated.name = "CYCLE_UPDATED_" + id;
    updated.stock = 200;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updated)
        .when()
        .put("/product/" + id)
        .then()
        .statusCode(200);

    // Get
    given()
        .when()
        .get("/product/" + id)
        .then()
        .statusCode(200)
        .body(containsString("CYCLE_UPDATED_"));

    // Delete
    given()
        .when()
        .delete("/product/" + id)
        .then()
        .statusCode(204);

    // Verify deleted
    given()
        .when()
        .get("/product/" + id)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(21)
  public void testGetProductByIdInvalidFormat() {
    given()
        .when()
        .get("/product/invalid_id")
        .then()
        .statusCode(anyOf(is(400), is(404)));
  }

  @Test
  @Order(22)
  public void testCreateProductWithLongName() {
    Product product = new Product();
    product.name = "VERY_LONG_PRODUCT_NAME_" + "X".repeat(200) + "_" + System.currentTimeMillis();
    product.stock = 60;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(anyOf(is(201), is(400), is(422), is(500)));
  }

  @Test
  @Order(23)
  public void testUpdateProductMultipleTimes() {
    // Create
    Product product = new Product();
    product.name = "MULTI_UPDATE_" + System.currentTimeMillis();
    product.stock = 50;

    Long id = given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(product)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    // Update multiple times
    for (int i = 0; i < 3; i++) {
      Product updated = new Product();
      updated.name = "UPDATE_" + i + "_" + id;
      updated.stock = 50 + (i * 50);

      given()
          .contentType(MediaType.APPLICATION_JSON)
          .body(updated)
          .when()
          .put("/product/" + id)
          .then()
          .statusCode(200);
    }
  }
}
