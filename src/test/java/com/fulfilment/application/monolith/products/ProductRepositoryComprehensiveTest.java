package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for ProductRepository covering all database operations.
 * Tests: list, find by ID, persistence, queries.
 */
@QuarkusTest
public class ProductRepositoryComprehensiveTest {

  @Inject ProductRepository productRepository;

  @Test
  public void testListAllProducts() {
    List<Product> products = productRepository.listAll();

    assertNotNull(products);
    assertTrue(products.size() > 0);
  }

  @Test
  public void testFindByIdReturnsNullForNonExistentId() {
    Product product = productRepository.findById(999999L);

    assertNull(product);
  }

  @Test
  public void testFindByName() {
    var product = productRepository.find("name = ?1", "KALLAX").firstResult();

    assertNotNull(product);
    assertEquals("KALLAX", product.name);
  }

  @Test
  public void testFindByNameNotFound() {
    var product = productRepository.find("name = ?1", "NONEXISTENT_PRODUCT").firstResult();

    assertNull(product);
  }

  @Test
  public void testCountAllProducts() {
    long count = productRepository.count();

    assertTrue(count > 0);
  }

  @Test
  @Transactional
  public void testPersistAndRetrieveProduct() {
    Product product = new Product();
    product.name = "NEW_TEST_PRODUCT";
    product.stock = 150;

    productRepository.persist(product);

    Product retrieved = productRepository.find("name = ?1", "NEW_TEST_PRODUCT").firstResult();

    assertNotNull(retrieved);
    assertEquals("NEW_TEST_PRODUCT", retrieved.name);
    assertEquals(150, retrieved.stock);
  }

  @Test
  public void testProductHasCorrectFieldValues() {
    Product product = productRepository.findById(2L);

    assertNotNull(product);
    assertEquals(2L, product.id);
    assertEquals("KALLAX", product.name);
    assertEquals(5, product.stock);
  }

  @Test
  public void testFindFirstResult() {
    var product = productRepository.find("name like ?1", "%AX").firstResult();

    assertNotNull(product);
    assertTrue(product.name.endsWith("AX"));
  }

  @Test
  public void testListAllReturnsMultipleProducts() {
    List<Product> products = productRepository.listAll();

    assertTrue(products.size() >= 3);
  }

  @Test
  @Transactional
  public void testDeleteProduct() {
    Product product = new Product();
    product.name = "DELETE_TEST_PRODUCT";
    product.stock = 100;

    productRepository.persist(product);
    Long id = product.id;

    Product toDelete = productRepository.findById(id);
    assertNotNull(toDelete);

    productRepository.delete(toDelete);

    Product deleted = productRepository.findById(id);
    assertNull(deleted);
  }

  @Test
  public void testCountByQuery() {
    long count = productRepository.count("stock > ?1", 5);

    assertTrue(count >= 0);
  }

  @Test
  public void testFindWithMultipleConditions() {
    List<Product> products = productRepository.list("stock >= ?1 and name like ?2", 3, "%TAD%");

    assertNotNull(products);
    assertTrue(products.stream().allMatch(p -> p.stock >= 3 && p.name.contains("TAD")));
  }
}
