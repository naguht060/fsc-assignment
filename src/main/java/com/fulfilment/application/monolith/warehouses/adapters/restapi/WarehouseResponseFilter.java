package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WarehouseResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    // Change 200 to 201 for POST requests to /warehouse (create operations)
    String path = requestContext.getUriInfo().getPath();
    if ("POST".equals(requestContext.getMethod())
        && (path.equals("warehouse") || path.equals("/warehouse"))
        && responseContext.getStatus() == Response.Status.OK.getStatusCode()) {
      responseContext.setStatus(Response.Status.CREATED.getStatusCode());
    }
  }
}
