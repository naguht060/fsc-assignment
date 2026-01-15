package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbWarehouseTest {

    private final DbWarehouse dbWarehouse = new DbWarehouse();

    @Test
    void testToWarehouse() {
        Warehouse result = dbWarehouse.toWarehouse();
        assertEquals(new Warehouse(), result);
    }
}
