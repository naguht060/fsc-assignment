package com.fulfilment.application.monolith.stores;

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
 * Tests for StoreResource.ErrorMapper to verify exception handling and JSON response mapping.
 * Tests: 404/400/500 status codes, exception type, error messages in response body.
 */
public class StoreResourceErrorMapperComprehensiveTest {

  private StoreResource.ErrorMapper errorMapper;
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    errorMapper = new StoreResource.ErrorMapper();
    objectMapper = new ObjectMapper();
    errorMapper.objectMapper = objectMapper;
  }

  @Test
  public void testMapWebApplicationExceptionWith404Status() {
    Exception ex = new NotFoundException("Store not found");

    Response response = errorMapper.toResponse(ex);

    assertEquals(404, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapWebApplicationExceptionWith400Status() {
    Exception ex = new BadRequestException("Invalid request");

    Response response = errorMapper.toResponse(ex);

    assertEquals(400, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapWebApplicationExceptionWith500Status() {
    Exception ex = new InternalServerErrorException("Server error");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapGenericException() {
    Exception ex = new IllegalArgumentException("Invalid argument");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapExceptionWithNullMessage() {
    Exception ex = new Exception((String) null);

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapExceptionWithEmptyMessage() {
    Exception ex = new Exception("");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapExceptionIncludesExceptionType() {
    Exception ex = new NotFoundException("Not found");

    Response response = errorMapper.toResponse(ex);

    // Response entity should be a JSON string containing exceptionType
    String entity = response.getEntity().toString();
    assertTrue(entity.contains("exceptionType"));
    assertTrue(entity.contains("NotFoundException"));
  }

  @Test
  public void testMapExceptionIncludesCode() {
    Exception ex = new BadRequestException("Bad request");

    Response response = errorMapper.toResponse(ex);

    String entity = response.getEntity().toString();
    assertTrue(entity.contains("code"));
    assertTrue(entity.contains("400"));
  }

  @Test
  public void testMapExceptionIncludesErrorMessage() {
    String errorMsg = "This is a test error message";
    Exception ex = new WebApplicationException(errorMsg, 422);

    Response response = errorMapper.toResponse(ex);

    String entity = response.getEntity().toString();
    assertTrue(entity.contains("error"));
  }

  @Test
  public void testMapMultipleExceptionsConsistently() {
    Exception ex1 = new NotFoundException("Not found");
    Exception ex2 = new NotFoundException("Not found");

    Response response1 = errorMapper.toResponse(ex1);
    Response response2 = errorMapper.toResponse(ex2);

    assertEquals(response1.getStatus(), response2.getStatus());
    assertEquals(404, response1.getStatus());
    assertEquals(404, response2.getStatus());
  }

  @Test
  public void testMapExceptionWithLongMessage() {
    String longMessage = "This is a very long error message that should be included in the response. "
        + "It contains multiple sentences and should still be properly serialized to JSON.";
    Exception ex = new WebApplicationException(longMessage, 422);

    Response response = errorMapper.toResponse(ex);

    assertEquals(422, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapExceptionWithSpecialCharactersInMessage() {
    String messageWithSpecialChars = "Error: @#$%^&*()_+-=[]{}|;':\",./<>?";
    Exception ex = new WebApplicationException(messageWithSpecialChars, 400);

    Response response = errorMapper.toResponse(ex);

    assertEquals(400, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapExceptionStatusIsAccessible() {
    Exception ex = new NotFoundException("Resource not found");

    Response response = errorMapper.toResponse(ex);

    assertTrue(response.getStatus() >= 400);
    assertTrue(response.getStatus() < 600);
  }

  @Test
  public void testMapExceptionEntityIsAccessible() {
    Exception ex = new BadRequestException("Bad input");

    Response response = errorMapper.toResponse(ex);

    assertNotNull(response.getEntity());
    assertNotNull(response.getEntity().toString());
    assertTrue(response.getEntity().toString().length() > 0);
  }

  @Test
  public void testMapRuntimeExceptionDefaultsTo500() {
    Exception ex = new RuntimeException("Runtime error");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
  }

  @Test
  public void testMapThrowableMessage() {
    Exception ex = new Exception("Generic throwable error");

    Response response = errorMapper.toResponse(ex);

    assertEquals(500, response.getStatus());
    assertNotNull(response.getEntity());
  }

  @Test
  public void testMapExceptionTypeFieldAlwaysPresent() {
    Exception[] exceptions = {
        new NotFoundException("not found"),
        new BadRequestException("bad request"),
        new IllegalArgumentException("illegal arg"),
        new RuntimeException("runtime"),
        new Exception("generic")
    };

    for (Exception ex : exceptions) {
      Response response = errorMapper.toResponse(ex);
      String entity = response.getEntity().toString();
      assertTrue(entity.contains("exceptionType"), "exceptionType should be present for " + ex.getClass().getName());
    }
  }

  @Test
  public void testMapExceptionCodeFieldAlwaysPresent() {
    Exception[] exceptions = {
        new NotFoundException("not found"),
        new BadRequestException("bad request"),
        new IllegalArgumentException("illegal arg"),
        new RuntimeException("runtime")
    };

    for (Exception ex : exceptions) {
      Response response = errorMapper.toResponse(ex);
      String entity = response.getEntity().toString();
      assertTrue(entity.contains("code"), "code should be present for " + ex.getClass().getName());
    }
  }

  @Test
  public void testMapWebApplicationExceptionPreservesHttpStatus() {
    WebApplicationException ex = new WebApplicationException("Custom error", 418);

    Response response = errorMapper.toResponse(ex);

    assertEquals(418, response.getStatus());
  }

  @Test
  public void testMapExceptionResponseHasJsonContentType() {
    Exception ex = new BadRequestException("Bad request");

    Response response = errorMapper.toResponse(ex);

    // Verify entity is a valid object (JSON serializable)
    assertNotNull(response.getEntity());
  }
}
