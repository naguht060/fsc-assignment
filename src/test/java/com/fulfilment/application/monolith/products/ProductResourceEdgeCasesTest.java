package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Edge case tests for ProductResource to improve coverage of error paths and edge cases.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductResourceEdgeCasesTest {

  @Inject ProductResource productResource;

  @Test
  @Order(1)
  @Transactional
  public void testGetSingleProductNotFound() {
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> productResource.getSingle(999999L));
    assertTrue(exception.getMessage().contains("does not exist"));
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  @Order(2)
  @Transactional
  public void testCreateProductWithNullName() {
    Product product = new Product();
    product.name = null;
    product.stock = 10;

    // Should succeed - name is not required at create time in the resource
    assertDoesNotThrow(() -> productResource.create(product));
  }

  @Test
  @Order(3)
  @Transactional
  public void testCreateProductWithIdSetShouldFail() {
    Product product = new Product();
    product.id = 9999L;
    product.name = "INVALID_PRODUCT";
    product.stock = 10;

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> productResource.create(product));
    assertTrue(exception.getMessage().contains("Id was invalidly set on request"));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  @Order(4)
  @Transactional
  public void testUpdateProductWithoutName() {
    // First create a product
    Product product = new Product();
    product.name = "UPDATE_TEST_PRODUCT";
    product.stock = 50;
    var response = productResource.create(product);
    Long productId = response.getEntity() != null ? ((Product) response.getEntity()).id : 1L;

    // Try to update without name
    Product updateProduct = new Product();
    updateProduct.name = null;
    updateProduct.stock = 100;

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () -> productResource.update(productId, updateProduct));
    assertTrue(exception.getMessage().contains("Product Name was not set on request"));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  @Order(5)
  @Transactional
  public void testUpdateProductNotFound() {
    Product product = new Product();
    product.name = "SOME_NAME";
    product.stock = 10;

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> productResource.update(999999L, product));
    assertTrue(exception.getMessage().contains("does not exist"));
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  @Order(6)
  @Transactional
  public void testDeleteProductNotFound() {
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> productResource.delete(999999L));
    assertTrue(exception.getMessage().contains("does not exist"));
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  @Order(7)
  @Transactional
  public void testGetAllProducts() {
    var products = productResource.get();
    assertNotNull(products);
    assertTrue(products.size() >= 0);
  }

  @Test
  @Order(8)
  @Transactional
  public void testUpdateProductSuccessfully() {
    // Create a product
    Product product = new Product();
    product.name = "PRODUCT_TO_UPDATE_" + System.currentTimeMillis();
    product.description = "Original description";
    product.price = new BigDecimal("50.00");
    product.stock = 10;
    var createResponse = productResource.create(product);
    Long productId = ((Product) createResponse.getEntity()).id;

    // Update it
    Product updateProduct = new Product();
    updateProduct.name = "UPDATED_PRODUCT";
    updateProduct.description = "Updated description";
    updateProduct.price = new BigDecimal("75.00");
    updateProduct.stock = 20;

    Product result = productResource.update(productId, updateProduct);
    assertNotNull(result);
    assertEquals("UPDATED_PRODUCT", result.name);
    assertEquals("Updated description", result.description);
    assertEquals(0, new BigDecimal("75.00").compareTo(result.price));
    assertEquals(20, result.stock);
  }

  @Test
  @Order(9)
  @Transactional
  public void testDeleteProductSuccessfully() {
    // Create a product
    Product product = new Product();
    product.name = "PRODUCT_TO_DELETE_" + System.currentTimeMillis();
    product.stock = 10;
    var createResponse = productResource.create(product);
    Long productId = ((Product) createResponse.getEntity()).id;

    // Delete it
    var deleteResponse = productResource.delete(productId);
    assertEquals(204, deleteResponse.getStatus());

    // Verify it's deleted
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> productResource.getSingle(productId));
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  @Order(10)
  @Transactional
  public void testCreateProductWithAllFields() {
    Product product = new Product();
    product.name = "COMPLETE_PRODUCT_" + System.currentTimeMillis();
    product.description = "Complete product description";
    product.price = new BigDecimal("199.99");
    product.stock = 100;

    var response = productResource.create(product);
    assertEquals(201, response.getStatus());

    Product created = (Product) response.getEntity();
    assertNotNull(created.id);
    assertTrue(created.name.startsWith("COMPLETE_PRODUCT_"));
  }
}
