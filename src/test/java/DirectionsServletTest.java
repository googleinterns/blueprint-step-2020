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
import com.google.sps.model.DirectionsClient;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.servlets.DirectionsServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.AdditionalAnswers;
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
  public void setUp() throws Exception {
    directionsClientFactory = Mockito.mock(DirectionsClientFactory.class);
    directionsClient = Mockito.mock(DirectionsClient.class);
    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    request = Mockito.mock(HttpServletRequest.class);
    response =
        Mockito.mock(
            HttpServletResponse.class,
            AdditionalAnswers.delegatesTo(new HttpServletResponseFake(stringWriter)));
    gson = new Gson();
    Mockito.when(directionsClientFactory.getDirectionsClient(API_KEY)).thenReturn(directionsClient);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void aToBWithWaypoints() throws Exception {
    servlet =
        new DirectionsServlet(
            directionsClientFactory, API_KEY, "A", "B", ImmutableList.of("C", "D"));

    Mockito.when(directionsClient.getDirections("A", "B", ImmutableList.of("C", "D")))
        .thenReturn(A_TO_B_WITH_WAYPOINTS);

    servlet.doGet(request, response);

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(A_TO_B_WITH_WAYPOINTS, actual);
  }

  @Test
  public void aToAWithWaypoints() throws Exception {
    servlet =
        new DirectionsServlet(
            directionsClientFactory, API_KEY, "A", "A", ImmutableList.of("B", "C", "D"));

    Mockito.when(directionsClient.getDirections("A", "A", ImmutableList.of("B", "C", "D")))
        .thenReturn(A_TO_A_WITH_WAYPOINTS);

    servlet.doGet(request, response);

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(A_TO_A_WITH_WAYPOINTS, actual);
  }

  @Test
  public void aToBNoWaypoints() throws Exception {
    servlet = new DirectionsServlet(directionsClientFactory, API_KEY, "A", "B", ImmutableList.of());

    Mockito.when(directionsClient.getDirections("A", "B", ImmutableList.of()))
        .thenReturn(A_TO_B_NO_WAYPOINTS);

    servlet.doGet(request, response);

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(A_TO_B_NO_WAYPOINTS, actual);
  }
}
