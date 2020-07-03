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
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DirectionsUtilityTest {

  private final String ORIGIN = "University of Waterloo";
  private final String DESTINATION = "Wilfrid Laurier University";
  private final List<String> WAYPOINTS = Arrays.asList("University of Waterloo Place");
  private final String API_KEY = "AIzaSyCBeGgjMWNe-d5qN34j_TitIllGMSvqWpY";

  @Test
  public void buildUri() throws IOException, URISyntaxException {
    // Checks that the correct URI is built.
    String actual = DirectionsUtility.getDirectionsUri(DESTINATION, ORIGIN, WAYPOINTS, API_KEY);
    String expected =
        "https://maps.googleapis.com/maps/api/directions/json?origin=University+of+Waterloo&destination=Wilfrid+Laurier+University&waypoints=via%3AUniversity+of+Waterloo+Place&departure_time=now&key="
            + API_KEY;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void apiCall() throws JSONException, IOException, URISyntaxException {
    // Ensures that Distance Matrix API call is successful.
    JSONObject actual = DirectionsUtility.getDirections(DESTINATION, ORIGIN, WAYPOINTS, API_KEY);
    Assert.assertEquals("OK", actual.get("status"));
  }
}
