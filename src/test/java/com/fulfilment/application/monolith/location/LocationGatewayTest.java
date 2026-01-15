package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LocationGatewayTest {

  @Inject LocationGateway locationGateway;

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // when
    var location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    // when
    var location = locationGateway.resolveByIdentifier("NON-EXISTING");

    // then
    assertNull(location);
  }

  @Test
  public void testResolveAmsterdamLocation() {
    var location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
  }

  @Test
  public void testResolveEindhovenLocation() {
    var location = locationGateway.resolveByIdentifier("EINDHOVEN-001");
    assertNotNull(location);
    assertEquals("EINDHOVEN-001", location.identification);
  }

  @Test
  public void testResolveAllLocationsAvailable() {
    assertNotNull(locationGateway.resolveByIdentifier("ZWOLLE-001"));
    assertNotNull(locationGateway.resolveByIdentifier("AMSTERDAM-001"));
    assertNotNull(locationGateway.resolveByIdentifier("EINDHOVEN-001"));
  }

  @Test
  public void testLocationConstraints() {
    var location = locationGateway.resolveByIdentifier("ZWOLLE-001");
    assertTrue(location.maxNumberOfWarehouses > 0);
    assertTrue(location.maxCapacity > 0);
  }
}
