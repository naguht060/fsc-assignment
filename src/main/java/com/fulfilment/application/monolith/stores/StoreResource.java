package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject StoreService storeService;

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    return storeService.listAll();
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    return storeService.findByIdOrThrow(id);
  }

  @POST
  public Response create(Store store) {
    Store created = storeService.create(store);
    legacyStoreManagerGateway.createStoreOnLegacySystem(store);

    return Response.ok(created).status(201).build();
  }

  @PUT
  @Path("{id}")
  public Store update(Long id, Store updatedStore) {
    Store entity = storeService.update(id, updatedStore);
    legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

    return entity;
  }

  @PATCH
  @Path("{id}")
  public Store patch(Long id, Store updatedStore) {
    Store entity = storeService.patch(id, updatedStore);
    legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

    return entity;
  }

  @DELETE
  @Path("{id}")
  public Response delete(Long id) {
    storeService.delete(id);
    return Response.status(204).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
