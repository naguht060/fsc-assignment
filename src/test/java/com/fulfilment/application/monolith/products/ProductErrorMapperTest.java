package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class ProductErrorMapperTest {

  @Test
  void errorMapper_buildsJsonResponse() {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    WebApplicationException ex = new WebApplicationException("Not found", 404);
    Response resp = mapper.toResponse(ex);

    assertEquals(404, resp.getStatus());
    Object entity = resp.getEntity();
    assertNotNull(entity);
    String json = entity.toString();
    assertTrue(json.contains("exceptionType"));
    assertTrue(json.contains("code"));
    assertTrue(json.contains("Not found"));
  }
}
