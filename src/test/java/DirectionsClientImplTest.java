// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.google.common.collect.ImmutableList;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.Distance;
import com.google.maps.model.Duration;
import com.google.maps.model.GeocodedWaypoint;
import com.google.maps.model.LatLng;
import com.google.sps.exceptions.DirectionsException;
import com.google.sps.model.DirectionsClientImpl;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test DirectionsClientImpl to correctly parse DirectionsResult obtained from the Directions API
 * into a List of String
 */
@RunWith(JUnit4.class)
public class DirectionsClientImplTest {

  private static final List<String> A_TO_B_NO_WAYPOINTS =
      ImmutableList.of(
          "[DirectionsLeg: \"A\" -> \"B\" (45,-73 -> 43,-80), duration=6 hours, distance=6 km: 2 steps]");

  @Test
  public void aToBNoWaypoints()
      throws DirectionsException, ServletException, ApiException, InterruptedException,
          IOException {

    // Create fake DirectionsResult to parse
    Distance distance = new Distance();
    distance.humanReadable = "6 km";
    Duration duration = new Duration();
    duration.humanReadable = "6 hours";

    DirectionsLeg leg = new DirectionsLeg();
    leg.steps = new DirectionsStep[] {new DirectionsStep(), new DirectionsStep()};
    leg.distance = distance;
    leg.duration = duration;
    leg.startLocation = new LatLng(45, -73);
    leg.endLocation = new LatLng(43, -80);
    leg.startAddress = "A";
    leg.endAddress = "B";

    DirectionsRoute route = new DirectionsRoute();
    route.legs = new DirectionsLeg[] {leg};

    GeocodedWaypoint[] geocodedWaypoints = {new GeocodedWaypoint(), new GeocodedWaypoint()};
    DirectionsRoute[] routes = {route};
    DirectionsResult directionsResult = new DirectionsResult();
    directionsResult.geocodedWaypoints = geocodedWaypoints;
    directionsResult.routes = routes;

    List<String> actual = DirectionsClientImpl.parseDirectionsResult(directionsResult);

    // Trailing zeroes in tests omitted by default to reduce clutter
    List<String> expected =
        ImmutableList.of(
            A_TO_B_NO_WAYPOINTS
                .get(0)
                .replace(
                    "(45,-73 -> 43,-80)",
                    "(45.00000000,-73.00000000 -> 43.00000000,-80.00000000)"));

    Assert.assertEquals(expected, actual);
  }
}
