package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    // when
    var location = locationGateway.resolveByIdentifier("NON-EXISTING");

    // then
    assertNull(location);
  }
}
