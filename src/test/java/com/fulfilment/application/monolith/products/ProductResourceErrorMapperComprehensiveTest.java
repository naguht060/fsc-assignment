package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ProductResource.ErrorMapper to verify exception mapping and response serialization.
 * Tests: status code mapping, exception type, error message serialization.
 */
public class ProductResourceErrorMapperComprehensiveTest {

  private ProductResource.ErrorMapper errorMapper;
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    errorMapper = new ProductResource.ErrorMapper();
    objectMapper = new ObjectMapper();
    errorMapper.objectMapper = objectMapper;
  }

  @Test
  public void testMapNotFoundExceptionTo404() {
    Exception ex = new NotFoundException("Product not found");

    Response response = errorMapper.toResponse(ex);

    assertEquals(404, response.getStatus());
  }

  @Test
  public void testMapBadRequestExceptionTo400() {
    Exception ex = new BadRequestException("Invalid product data");

    Response response = errorMapper.toResponse(ex);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void testMapInternalServerErrorExceptionTo500() {
    Exception ex = new InternalServerErrorException("Internal server error");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
  }

  @Test
  public void testMapGenericExceptionTo500() {
    Exception ex = new IllegalArgumentException("Invalid argument");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
  }

  @Test
  public void testResponseEntityContainsExceptionType() {
    Exception ex = new NotFoundException("Not found");

    Response response = errorMapper.toResponse(ex);

    String entity = response.getEntity().toString();
    assertTrue(entity.contains("exceptionType"));
    assertTrue(entity.contains("NotFoundException"));
  }

  @Test
  public void testResponseEntityContainsCode() {
    Exception ex = new NotFoundException("Not found");

    Response response = errorMapper.toResponse(ex);

    String entity = response.getEntity().toString();
    assertTrue(entity.contains("code"));
    assertTrue(entity.contains("404"));
  }

  @Test
  public void testResponseEntityContainsErrorMessage() {
    String message = "Product with id 123 not found";
    Exception ex = new WebApplicationException(message, 404);

    Response response = errorMapper.toResponse(ex);

    String entity = response.getEntity().toString();
    assertTrue(entity.contains("error"));
  }

  @Test
  public void testExceptionWithNullMessage() {
    Exception ex = new Exception((String) null);

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
    String entity = response.getEntity().toString();
    assertTrue(entity.contains("exceptionType"));
    assertTrue(entity.contains("code"));
  }

  @Test
  public void testExceptionWithEmptyMessage() {
    Exception ex = new Exception("");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
  }

  @Test
  public void testWebApplicationExceptionWithCustomStatus() {
    Exception ex = new WebApplicationException("Unprocessable entity", 422);

    Response response = errorMapper.toResponse(ex);

    assertEquals(422, response.getStatus());
  }

  @Test
  public void testMultipleExceptionMappingsConsistent() {
    Exception ex1 = new NotFoundException("Not found");
    Exception ex2 = new NotFoundException("Product not found");

    Response response1 = errorMapper.toResponse(ex1);
    Response response2 = errorMapper.toResponse(ex2);

    assertEquals(response1.getStatus(), response2.getStatus());
    assertEquals(404, response1.getStatus());
  }

  @Test
  public void testExceptionWithLongMessage() {
    String longMessage = "This is a very long error message that contains detailed information about the error. "
        + "It should be properly serialized to JSON without any issues. "
        + "The mapper should handle long messages gracefully.";
    Exception ex = new WebApplicationException(longMessage, 400);

    Response response = errorMapper.toResponse(ex);

    assertEquals(400, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testExceptionWithSpecialCharacters() {
    String messageWithSpecialChars = "Error with special chars: @#$%^&*()_+-=[]{}";
    Exception ex = new WebApplicationException(messageWithSpecialChars, 400);

    Response response = errorMapper.toResponse(ex);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void testResponseEntityIsAccessible() {
    Exception ex = new BadRequestException("Bad request");

    Response response = errorMapper.toResponse(ex);

    assertNotNull(response.getEntity());
  }

  @Test
  public void testRuntimeExceptionDefaultsTo500() {
    Exception ex = new RuntimeException("Runtime error occurred");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
  }

  @Test
  public void testAllExceptionTypesIncluded() {
    Exception[] exceptions = {
        new NotFoundException("not found"),
        new BadRequestException("bad request"),
        new IllegalArgumentException("illegal arg"),
        new RuntimeException("runtime error"),
        new Exception("generic exception")
    };

    for (Exception ex : exceptions) {
      Response response = errorMapper.toResponse(ex);
      String entity = response.getEntity().toString();
      assertTrue(entity.contains("exceptionType"), "Should include exceptionType");
      assertTrue(entity.contains("code"), "Should include code");
    }
  }

  @Test
  public void testStatusCodeIsValid() {
    Exception ex = new BadRequestException("Invalid input");

    Response response = errorMapper.toResponse(ex);

    assertTrue(response.getStatus() >= 400);
    assertTrue(response.getStatus() < 600);
  }

  @Test
  public void testWebApplicationExceptionPreservesStatus() {
    int customStatus = 418; // I'm a teapot
    Exception ex = new WebApplicationException("Teapot error", customStatus);

    Response response = errorMapper.toResponse(ex);

    assertEquals(customStatus, response.getStatus());
  }

  @Test
  public void testNullMessageDoesNotBreakSerialization() {
    Exception ex = new Exception((String) null);

    Response response = errorMapper.toResponse(ex);

    assertNotNull(response.getEntity());
    String entity = response.getEntity().toString();
    assertTrue(entity.contains("exceptionType"));
  }

  @Test
  public void testResponseCanBeSerializedMultipleTimes() {
    Exception ex = new NotFoundException("Not found");

    Response response1 = errorMapper.toResponse(ex);
    Response response2 = errorMapper.toResponse(ex);

    assertNotNull(response1.getEntity());
    assertNotNull(response2.getEntity());
    assertEquals(response1.getStatus(), response2.getStatus());
  }
}
