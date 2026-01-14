package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    var location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    var location = locationGateway.resolveByIdentifier("NON-EXISTING");

    // then
    assertNull(location);
  }
}
