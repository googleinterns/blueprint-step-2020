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
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@RunWith(JUnit4.class)
public class DistanceMatrixUtilityTest {

  String ORIGIN = "University of Waterloo";
  List<String> DESTINATIONS = Arrays.asList("Wilfrid Laurier University", "Conestoga College");

  @Test
  public void buildUri() throws IOException, URISyntaxException {
    // Checks that the correct URI is built.
    String actual = DistanceMatrixUtility.getDistanceMatrixUri(DESTINATIONS, ORIGIN);
    String expected =
        "https://maps.googleapis.com/maps/api/distancematrix/json?destinations=Wilfrid+Laurier+University&destinations=Conestoga+College&origins=University+of+Waterloo&units=imperial&key=AIzaSyBsHP0Wo698KQk2lkNlroMzSWHKyH9-05Y";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void apiCall() throws JSONException, IOException, URISyntaxException {
    // Ensures that Distance Matrix API call is successful.
    JSONObject actual = DistanceMatrixUtility.getDistanceMatrix(DESTINATIONS, ORIGIN);
    Assert.assertEquals("OK", actual.get("status"));
    System.out.println(actual);
  }

  @Test
  public void compareJson() throws IOException, JSONException, URISyntaxException {
    // Confirms that the JSON obtained from the API is correct after building URI and calling API.
    String expected =
        "{\"destination_addresses\":[\"75 University Ave W, Waterloo, ON N2L 3C5, Canada\"],\"origin_addresses\":[\"200 University Ave W, Waterloo, ON N2L 3G1, Canada\"],\"rows\":[{\"elements\":[{\"duration\":{\"text\":\"5 mins\",\"value\":315},\"distance\":{\"text\":\"0.9 mi\",\"value\":1464},\"status\":\"OK\"}]}],\"status\":\"OK\"}";
    JSONObject actual = DistanceMatrixUtility.getDistanceMatrix(DESTINATIONS, ORIGIN);
    JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
  }
}
