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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.google.sps.model.DirectionsClient;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.model.DirectionsClientImpl;
import com.google.sps.servlets.DirectionsServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/**
 * Test Directions Servlet to ensure response contains correctly parsed legs. Assumes all parameters
 * origin, destination and waypoints are valid addresses.
 */
@RunWith(JUnit4.class)
public final class DirectionsServletTest {
  private DirectionsClientFactory directionsClientFactory;
  private DirectionsClient directionsClient;
  private DirectionsServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;
  private Gson gson;

  private static final String API_KEY = "sampleApiKey";
  private static final List<String> A_TO_B_WITH_WAYPOINTS =
      ImmutableList.of(
          "[DirectionsLeg: \"A\" -> \"C\" (42,-83 -> 43,-80), duration=3 hours, distance=2 km: 1 steps]",
          "[DirectionsLeg: \"C\" -> \"D\" (43,-80 -> 43,-79), duration=1 hour, distance=1 km: 1 steps]",
          "[DirectionsLeg: \"D\" -> \"B\" (43,-79 -> 46,-71), duration=8 hours, distance=7 km: 3 steps]");
  private static final List<String> A_TO_A_WITH_WAYPOINTS =
      ImmutableList.of(
          "[DirectionsLeg: \"A\" -> \"B\" (45,-73 -> 43,-80), duration=6 hours, distance=6 km: 1 steps]",
          "[DirectionsLeg: \"B\" -> \"C\" (43,-80 -> 43,-80), duration=8 mins, distance=3 km: 5 steps]",
          "[DirectionsLeg: \"C\" -> \"D\" (43,-80 -> 42,-83), duration=3 hours, distance=2 km: 1 steps]",
          "[DirectionsLeg: \"D\" -> \"A\" (42,-83 -> 45,-73), duration=9 hours, distance=8 km: 2 steps]");
  private static final List<String> A_TO_B_NO_WAYPOINTS =
      ImmutableList.of(
          "[DirectionsLeg: \"A\" -> \"B\" (45,-73 -> 43,-80), duration=6 hours, distance=6 km: 2 steps]");

  @Before
  public void setUp() throws IOException {
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    directionsClientFactory = Mockito.mock(DirectionsClientFactory.class);
    directionsClient = Mockito.mock(DirectionsClient.class);
    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    gson = new Gson();
    Mockito.when(directionsClientFactory.getDirectionsClient(API_KEY)).thenReturn(directionsClient);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void aToBWithWaypoints() throws DirectionsException, ServletException {
    servlet =
        new DirectionsServlet(
            directionsClientFactory, API_KEY, "A", "B", ImmutableList.of("C", "D"));

    Mockito.when(directionsClient.getDirections("A", "B", ImmutableList.of("C", "D")))
        .thenReturn(A_TO_B_WITH_WAYPOINTS);

    servlet.doGet(request, response);
    printWriter.flush();

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(A_TO_B_WITH_WAYPOINTS, actual);
  }

  @Test
  public void aToAWithWaypoints() throws DirectionsException, ServletException {
    servlet =
        new DirectionsServlet(
            directionsClientFactory, API_KEY, "A", "A", ImmutableList.of("B", "C", "D"));

    Mockito.when(directionsClient.getDirections("A", "A", ImmutableList.of("B", "C", "D")))
        .thenReturn(A_TO_A_WITH_WAYPOINTS);

    servlet.doGet(request, response);
    printWriter.flush();

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(A_TO_A_WITH_WAYPOINTS, actual);
  }

  @Test
  public void aToBNoWaypoints() throws DirectionsException, ServletException {
    servlet = new DirectionsServlet(directionsClientFactory, API_KEY, "A", "B", ImmutableList.of());

    Mockito.when(directionsClient.getDirections("A", "B", ImmutableList.of()))
        .thenReturn(A_TO_B_NO_WAYPOINTS);

    servlet.doGet(request, response);
    printWriter.flush();

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(A_TO_B_NO_WAYPOINTS, actual);
  }

  @Test
  public void aToBNoWaypointsParseResult()
      throws DirectionsException, ServletException, ApiException, InterruptedException,
          IOException {

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
