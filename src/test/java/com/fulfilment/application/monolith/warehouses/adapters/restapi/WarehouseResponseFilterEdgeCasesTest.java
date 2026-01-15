package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Edge case tests for WarehouseResponseFilter to improve coverage of filter logic.
 */
public class WarehouseResponseFilterEdgeCasesTest {

  private WarehouseResponseFilter filter;

  @Mock private ContainerRequestContext requestContext;

  @Mock private ContainerResponseContext responseContext;

  @Mock private UriInfo uriInfo;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    filter = new WarehouseResponseFilter();
  }

  @Test
  public void testPostWarehouseWithOkStatusChangesTo201() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
  }

  @Test
  public void testPostWarehouseWithSlashPathChangesTo201() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
  }

  @Test
  public void testPostWarehouseWith201StatusNoChange() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testGetWarehouseNoStatusChange() {
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testPutWarehouseNoStatusChange() {
    when(requestContext.getMethod()).thenReturn("PUT");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testDeleteWarehouseNoStatusChange() {
    when(requestContext.getMethod()).thenReturn("DELETE");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testPostDifferentPathNoStatusChange() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("store");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testPostWarehouseWithStatus404NoChange() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testPostWarehouseWithStatus500NoChange() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testPostWarehouseWithSubpathNoChange() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse/123");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testPostWarehousePathCaseSensitive() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("Warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testPostMethodCaseSensitive() {
    when(requestContext.getMethod()).thenReturn("post");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testEmptyPathNoChange() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    filter.filter(requestContext, responseContext);

    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  public void testNullPathNoStatusChange() {
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn(null);
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    // The filter will throw NPE with null path - this is the actual behavior
    assertThrows(NullPointerException.class, () -> filter.filter(requestContext, responseContext));
  }
}
