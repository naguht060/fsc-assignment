package com.fulfilment.application.monolith.warehouses.adapters.restapi;

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
    void filter_changes200To201_forPostWarehouse() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void filter_worksWithLeadingSlash() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext).setStatus(Response.Status.CREATED.getStatusCode());
    }

    // ---------- NO-OP PATHS ----------

    @Test
    void filter_doesNothing_forGetRequest() {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext, never()).setStatus(anyInt());
    }

    @Test
    void filter_doesNothing_forDifferentPath() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("store");
        when(responseContext.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext, never()).setStatus(anyInt());
    }

    @Test
    void filter_doesNothing_whenAlready201() {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("warehouse");
        when(responseContext.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());

        filter.filter(requestContext, responseContext);

        verify(responseContext, never()).setStatus(anyInt());
    }
}