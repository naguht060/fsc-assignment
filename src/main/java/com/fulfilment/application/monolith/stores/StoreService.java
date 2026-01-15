package com.fulfilment.application.monolith.stores;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Status;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@ApplicationScoped
public class StoreService {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject TransactionSynchronizationRegistry transactionSynchronizationRegistry;

  @Transactional
  public List<Store> listAll() {
    return Store.listAll(Sort.by("name"));
  }

  @Transactional
  public Store findByIdOrThrow(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @Transactional
  public Store create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    store.persist();
    runAfterCommit(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
    return store;
  }

  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = findByIdOrThrow(id);

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    runAfterCommit(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore));
    return entity;
  }

  @Transactional
  public Store patch(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = findByIdOrThrow(id);

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    if (entity.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    runAfterCommit(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore));
    return entity;
  }

  @Transactional
  public void delete(Long id) {
    Store entity = findByIdOrThrow(id);
    entity.delete();
  }

  private void runAfterCommit(Runnable action) {
    transactionSynchronizationRegistry.registerInterposedSynchronization(
        new AfterCommitSynchronization(action));
  }

  private static class AfterCommitSynchronization implements Synchronization {
    private final Runnable action;

    AfterCommitSynchronization(Runnable action) {
      this.action = action;
    }

    @Override
    public void beforeCompletion() {}

    @Override
    public void afterCompletion(int status) {
      if (status == Status.STATUS_COMMITTED) {
        action.run();
      }
    }
  }
}

