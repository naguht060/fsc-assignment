package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Warehouse {

    // unique identifier
    public String businessUnitCode;
    public String location;
    public Integer capacity;
    public Integer stock;
    public LocalDateTime createdAt;
    public LocalDateTime archivedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warehouse that = (Warehouse) o;
        return Objects.equals(businessUnitCode, that.businessUnitCode)
                && Objects.equals(location, that.location)
                && Objects.equals(capacity, that.capacity)
                && Objects.equals(stock, that.stock)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(archivedAt, that.archivedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                businessUnitCode,
                location,
                capacity,
                stock,
                createdAt,
                archivedAt
        );
    }
}