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
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;
import com.google.sps.model.DirectionsClient;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test DirectionsClientImpl to correctly parse DirectionsResult obtained from the Directions API
 * into a List of String
 */
@RunWith(JUnit4.class)
public class DirectionsClientTest {

  @Test
  public void parseDirectionsResult() throws Exception {
    Duration duration = new Duration();
    DirectionsLeg leg = new DirectionsLeg();
    DirectionsRoute route = new DirectionsRoute();
    DirectionsRoute[] routes = {route};
    DirectionsResult directionsResult = new DirectionsResult();

    duration.humanReadable = "6 hours";
    leg.duration = duration;
    leg.endAddress = "B";
    route.legs = new DirectionsLeg[] {leg};
    directionsResult.routes = routes;

    List<String> actual = DirectionsClient.parseDirectionsResult(directionsResult);

    Assert.assertEquals(ImmutableList.of("6 hours to travel to B"), actual);
  }

  @Test
  public void getTotalTravelTime() throws Exception {
    Duration durationOne = new Duration();
    Duration durationTwo = new Duration();
    DirectionsLeg legOne = new DirectionsLeg();
    DirectionsLeg legTwo = new DirectionsLeg();
    DirectionsRoute route = new DirectionsRoute();
    DirectionsRoute[] routes = {route};
    DirectionsResult directionsResult = new DirectionsResult();

    durationOne.humanReadable = "6 seconds";
    durationOne.inSeconds = 6;
    durationTwo.humanReadable = "12 seconds";
    durationTwo.inSeconds = 12;
    legOne.duration = durationOne;
    legTwo.duration = durationTwo;
    route.legs = new DirectionsLeg[] {legOne, legTwo};
    directionsResult.routes = routes;

    long actual = DirectionsClient.getTotalTravelTime(directionsResult);

    Assert.assertEquals(18, actual);
  }
}
