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
import com.google.maps.DirectionsClient;
import com.google.maps.DirectionsClientFactory;
import com.google.maps.DirectionsException;
import com.google.sps.servlets.DirectionsServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.After;
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
  private static DirectionsClientFactory directionsClientFactory;
  private static DirectionsClient directionsClient;
  private DirectionsServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  private static final List<String> WINDSOR_TO_MONTREAL_WITH_WAYPOINTS =
      ImmutableList.of(
          "[DirectionsLeg: \"Windsor, ON, Canada\" -> \"Waterloo, ON, Canada\" (42.31486680,-83.03656800 -> 43.46430180,-80.52042120), duration=2 hours 58 mins, distance=293 km: 16 steps]",
          "[DirectionsLeg: \"Waterloo, ON, Canada\" -> \"Markham, ON, Canada\" (43.46430180,-80.52042120 -> 43.85644940,-79.33771690), duration=1 hour 14 mins, distance=123 km: 15 steps]",
          "[DirectionsLeg: \"Markham, ON, Canada\" -> \"Quebec City, QC, Canada\" (43.85644940,-79.33771690 -> 46.81380600,-71.20822600), duration=7 hours 42 mins, distance=790 km: 30 steps]",
          "[DirectionsLeg: \"Quebec City, QC, Canada\" -> \"Montreal, QC, Canada\" (46.81380600,-71.20822600 -> 45.50171230,-73.56721840), duration=2 hours 46 mins, distance=253 km: 29 steps]");
  private static final List<String> MONTREAL_TO_MONTREAL_WITH_WAYPOINTS =
      ImmutableList.of(
          "[DirectionsLeg: \"Montreal, QC, Canada\" -> \"Kitchener, ON, Canada\" (45.50171230,-73.56721840 -> 43.45184990,-80.49313410), duration=6 hours 10 mins, distance=632 km: 16 steps]",
          "[DirectionsLeg: \"Kitchener, ON, Canada\" -> \"Waterloo, ON, Canada\" (43.45184990,-80.49313410 -> 43.46430180,-80.52042120), duration=8 mins, distance=3.0 km: 5 steps]",
          "[DirectionsLeg: \"Waterloo, ON, Canada\" -> \"Windsor, ON, Canada\" (43.46430180,-80.52042120 -> 42.31486680,-83.03656800), duration=2 hours 55 mins, distance=291 km: 18 steps]",
          "[DirectionsLeg: \"Windsor, ON, Canada\" -> \"Montreal, QC, Canada\" (42.31486680,-83.03656800 -> 45.50171230,-73.56721840), duration=8 hours 42 mins, distance=897 km: 21 steps]");
  private static final List<String> MONTREAL_TO_WATERLOO_NO_WAYPOINTS =
      ImmutableList.of(
          "[DirectionsLeg: \"Montreal, QC, Canada\" -> \"Waterloo, ON, Canada\" (45.50171230,-73.56721840 -> 43.46430180,-80.52042120), duration=6 hours 11 mins, distance=638 km: 22 steps]");
  private static final String WINDSOR_TO_MONTREAL_WITH_WAYPOINTS_JSON =
      "[\"[DirectionsLeg: \"Windsor, ON, Canada\" -\u003e \"Waterloo, ON, Canada\" (42.31486680,-83.03656800 -\u003e 43.46430180,-80.52042120), duration\u003d2 hours 58 mins, distance\u003d293 km: 16 steps]\",\"[DirectionsLeg: \"Waterloo, ON, Canada\" -\u003e \"Markham, ON, Canada\" (43.46430180,-80.52042120 -\u003e 43.85644940,-79.33771690), duration\u003d1 hour 14 mins, distance\u003d123 km: 15 steps]\",\"[DirectionsLeg: \"Markham, ON, Canada\" -\u003e \"Quebec City, QC, Canada\" (43.85644940,-79.33771690 -\u003e 46.81380600,-71.20822600), duration\u003d7 hours 42 mins, distance\u003d790 km: 30 steps]\",\"[DirectionsLeg: \"Quebec City, QC, Canada\" -\u003e \"Montreal, QC, Canada\" (46.81380600,-71.20822600 -\u003e 45.50171230,-73.56721840), duration\u003d2 hours 46 mins, distance\u003d253 km: 29 steps]\"]";
  private static final String MONTREAL_TO_MONTREAL_WITH_WAYPOINTS_JSON =
      "[\"[DirectionsLeg: \"Montreal, QC, Canada\" -\u003e \"Kitchener, ON, Canada\" (45.50171230,-73.56721840 -\u003e 43.45184990,-80.49313410), duration\u003d6 hours 10 mins, distance\u003d632 km: 16 steps]\",\"[DirectionsLeg: \"Kitchener, ON, Canada\" -\u003e \"Waterloo, ON, Canada\" (43.45184990,-80.49313410 -\u003e 43.46430180,-80.52042120), duration\u003d8 mins, distance\u003d3.0 km: 5 steps]\",\"[DirectionsLeg: \"Waterloo, ON, Canada\" -\u003e \"Windsor, ON, Canada\" (43.46430180,-80.52042120 -\u003e 42.31486680,-83.03656800), duration\u003d2 hours 55 mins, distance\u003d291 km: 18 steps]\",\"[DirectionsLeg: \"Windsor, ON, Canada\" -\u003e \"Montreal, QC, Canada\" (42.31486680,-83.03656800 -\u003e 45.50171230,-73.56721840), duration\u003d8 hours 42 mins, distance\u003d897 km: 21 steps]\"]";
  private static final String MONTREAL_TO_WATERLOO_NO_WAYPOINTS_JSON =
      "[\"[DirectionsLeg: \"Montreal, QC, Canada\" -\u003e \"Waterloo, ON, Canada\" (45.50171230,-73.56721840 -\u003e 43.46430180,-80.52042120), duration\u003d6 hours 11 mins, distance\u003d638 km: 22 steps]\"]";

  @Before
  public void setUp() throws IOException {
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    directionsClientFactory = Mockito.mock(DirectionsClientFactory.class);
    directionsClient = Mockito.mock(DirectionsClient.class);
    servlet = new DirectionsServlet(directionsClientFactory);
    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(directionsClientFactory.getDirectionsClient(Mockito.any()))
        .thenReturn(directionsClient);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void tearDown() throws IOException {
    // Dump contents after each test
    stringWriter.getBuffer().setLength(0);
    stringWriter.close();
    printWriter.close();
  }

  @Test
  public void windsorToMontrealWithWaypoints() throws DirectionsException, ServletException {
    // Get optimized route of travel from Windsor, ON to Montreal, QC with waypoints
    // Markham, ON,
    // Waterloo, ON and Quebec City, QC in between.
    // Should return four legs: Windsor, ON -> Waterloo, ON -> Markham, ON -> Quebec
    // City, QC ->
    // Montreal, QC.
    Mockito.when(
            directionsClient.getDirections(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(String.class)))
        .thenReturn(WINDSOR_TO_MONTREAL_WITH_WAYPOINTS);
    servlet.doGet(request, response);
    printWriter.flush();
    String actual = StringEscapeUtils.unescapeJson(stringWriter.toString());
    Assert.assertTrue(actual.contains(WINDSOR_TO_MONTREAL_WITH_WAYPOINTS_JSON));
  }

  @Test
  public void montrealToMontrealWithWaypoints() throws DirectionsException, ServletException {
    // Get optimized route of travel from Montreal, QC to Montreal, QC with
    // waypoints Windsor, ON,
    // Waterloo, ON and Kitchener, ON in between.
    // Should return four legs: Montreal, QC -> Kitchener, ON -> Waterloo, ON ->
    // Windsor, ON ->
    // Montreal, QC.
    Mockito.when(
            directionsClient.getDirections(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(String.class)))
        .thenReturn(MONTREAL_TO_MONTREAL_WITH_WAYPOINTS);
    servlet.doGet(request, response);
    printWriter.flush();
    String actual = StringEscapeUtils.unescapeJson(stringWriter.toString());
    Assert.assertTrue(actual.contains(MONTREAL_TO_MONTREAL_WITH_WAYPOINTS_JSON));
  }

  @Test
  public void montrealToWaterlooNoWaypoints() throws DirectionsException, ServletException {
    // Get optimized route of travel from Montreal, QC to Waterloo, ON with no
    // waypoints in between.
    // Should return only one leg from Montreal, QC to Waterloo, ON.
    Mockito.when(
            directionsClient.getDirections(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(String.class)))
        .thenReturn(MONTREAL_TO_WATERLOO_NO_WAYPOINTS);
    servlet.doGet(request, response);
    printWriter.flush();
    String actual = StringEscapeUtils.unescapeJson(stringWriter.toString());
    Assert.assertTrue(actual.contains(MONTREAL_TO_WATERLOO_NO_WAYPOINTS_JSON));
  }
}
