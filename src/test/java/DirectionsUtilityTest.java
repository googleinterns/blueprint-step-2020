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

import com.google.sps.utility.DirectionsUtility;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DirectionsUtilityTest {

  private final String ORIGIN = "University of Waterloo";
  private final String DESTINATION = "Wilfrid Laurier University";
  private final List<String> NO_WAYPOINTS = Arrays.asList();
  private final List<String> ONE_WAYPOINT = Arrays.asList("University of Waterloo Place");
  private final List<String> MANY_WAYPOINTS =
      Arrays.asList("University of Waterloo Place", "Google Kitchener");
  private final String API_KEY = "SAMPLE_API_KEY";

  @Test
  public void oneWaypointUri() throws IOException, URISyntaxException {
    // Checks that the correct URI is built for a single waypoint.
    String actual = DirectionsUtility.getDirectionsUri(DESTINATION, ORIGIN, ONE_WAYPOINT, API_KEY);
    String expected =
        "https://maps.googleapis.com/maps/api/directions/json?origin=University+of+Waterloo&destination=Wilfrid+Laurier+University&waypoints=optimize%3Atrue%7CUniversity+of+Waterloo+Place&departure_time=now&key=SAMPLE_API_KEY";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noWaypointsUri() throws IOException, URISyntaxException {
    // Checks that the correct URI is built for no waypoints.
    String actual = DirectionsUtility.getDirectionsUri(DESTINATION, ORIGIN, NO_WAYPOINTS, API_KEY);
    String expected =
        "https://maps.googleapis.com/maps/api/directions/json?origin=University+of+Waterloo&destination=Wilfrid+Laurier+University&waypoints=optimize%3Atrue&departure_time=now&key=SAMPLE_API_KEY";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void manyWaypointsUri() throws IOException, URISyntaxException {
    // Checks that the correct URI is built for multiple waypoints.
    String actual =
        DirectionsUtility.getDirectionsUri(DESTINATION, ORIGIN, MANY_WAYPOINTS, API_KEY);
    String expected =
        "https://maps.googleapis.com/maps/api/directions/json?origin=University+of+Waterloo&destination=Wilfrid+Laurier+University&waypoints=optimize%3Atrue%7CUniversity+of+Waterloo+Place%7CGoogle+Kitchener&departure_time=now&key=SAMPLE_API_KEY";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void returnToOriginUri() throws IOException, URISyntaxException {
    // Checks that the correct URI is built for multiple waypoints and destination is the same as
    // origin.
    String actual = DirectionsUtility.getDirectionsUri(ORIGIN, ORIGIN, MANY_WAYPOINTS, API_KEY);
    String expected =
        "https://maps.googleapis.com/maps/api/directions/json?origin=University+of+Waterloo&destination=University+of+Waterloo&waypoints=optimize%3Atrue%7CUniversity+of+Waterloo+Place%7CGoogle+Kitchener&departure_time=now&key=SAMPLE_API_KEY";
    Assert.assertEquals(expected, actual);
  }
}
