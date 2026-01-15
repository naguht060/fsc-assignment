package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testCrudProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void testGetProductByIdNotFound() {
    given().when().get("product/999999").then().statusCode(404);
  }

  @Test
  public void testCreateProductMissingName() {
    Product p = new Product();
    p.description = "No name";
    p.price = new BigDecimal("10.00");
    p.stock = 5;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(p)
        .when()
        .post("product")
        .then()
        .statusCode(201);
  }

  @Test
  public void testCreateProductSuccess() {
    Product p = new Product();
    p.name = "NEW_PRODUCT_" + System.currentTimeMillis();
    p.description = "Test product";
    p.price = new BigDecimal("25.99");
    p.stock = 10;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(p)
        .when()
        .post("product")
        .then()
        .statusCode(201)
        .body(containsString(p.name));
  }

  @Test
  public void testUpdateProductNotFound() {
    Product p = new Product();
    p.name = "UPDATE_TEST";
    p.stock = 5;

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(p)
        .when()
        .put("product/999999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testDeleteProductNotFound() {
    given().when().delete("product/999999").then().statusCode(404);
  }
}
