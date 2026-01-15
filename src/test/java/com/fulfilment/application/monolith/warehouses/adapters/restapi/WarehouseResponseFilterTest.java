package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    // ---------- POSITIVE PATH ----------

    @Test
    void testFilterChanges200To201ForPostWarehouse() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void testFilterWorksWithSlashPath() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
    }

    // ---------- NEGATIVE / NO-OP PATHS ----------

    @Test
    void testFilterDoesNotChangeStatusForGetRequest() {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext, never()).setStatus(anyInt());
    }

    @Test
    void testFilterDoesNotChangeStatusForDifferentPath() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("store");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext, never()).setStatus(anyInt());
    }

    @Test
    void testFilterDoesNotChangeStatusWhenAlready201() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext, never()).setStatus(anyInt());
    }

    // ---------- DEFENSIVE / NULL SAFETY ----------

    @Test
    void testFilterHandlesNullRequestContext() {
        assertDoesNotThrow(() -> filter.filter(null, responseContext));
    }

    @Test
    void testFilterHandlesNullResponseContext() {
        assertDoesNotThrow(() -> filter.filter(requestContext, null));
    }

    @Test
    void testFilterHandlesNullUriInfo() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(null);
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        assertDoesNotThrow(() -> filter.filter(requestContext, responseContext));
    }

    @Test
    void testFilterHandlesNullMethod() {
        when(requestContext.getMethod()).thenReturn(null);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        assertDoesNotThrow(() -> filter.filter(requestContext, responseContext));
    }
}