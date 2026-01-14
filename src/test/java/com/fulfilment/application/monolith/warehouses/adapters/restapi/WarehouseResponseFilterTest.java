package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

import static org.mockito.Mockito.*;

class WarehouseResponseFilterTest {

  private WarehouseResponseFilter filter;

  @Mock private ContainerRequestContext requestContext;

  @Mock private ContainerResponseContext responseContext;

  @Mock private UriInfo uriInfo;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    filter = new WarehouseResponseFilter();
  }

  @Test
  void testFilterChanges200To201ForPostWarehouse() {
    // Given
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    // When
    filter.filter(requestContext, responseContext);

    // Then
    verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
  }

  @Test
  void testFilterDoesNotChangeStatusForGetRequest() {
    // Given
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    // When
    filter.filter(requestContext, responseContext);

    // Then
    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  void testFilterDoesNotChangeStatusForDifferentPath() {
    // Given
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("store");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    // When
    filter.filter(requestContext, responseContext);

    // Then
    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  void testFilterDoesNotChangeStatusWhenAlready201() {
    // Given
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());

    // When
    filter.filter(requestContext, responseContext);

    // Then
    verify(responseContext, never()).setStatus(anyInt());
  }

  @Test
  void testFilterWorksWithSlashPath() {
    // Given
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/warehouse");
    when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

    // When
    filter.filter(requestContext, responseContext);

    // Then
    verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
  }
}
