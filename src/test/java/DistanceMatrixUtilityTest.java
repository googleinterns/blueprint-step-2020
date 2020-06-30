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

import com.google.sps.utility.AuthenticationUtility;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(JUnit4.class)
public class DistanceMatrixUtilityTest {
  /*
   * // No locations private final List<String> noLocations = Arrays.asList();
   * 
   * // Single location private final List<String> singleLocation =
   * Arrays.asList("New York, NY, USA");
   * 
   * // Realistic example, multiple locations within the same city, lined in a
   * straight line, coordinates private final List<String> singleCityLocations =
   * Arrays.asList("Ohio", "Miami");
   * 
   * // Realistic example, multiple locations within the same country, scattered
   * around, addresses private final List<String> singleCountryLocations =
   * Arrays.asList( "Waterloo, Ontario, Canada", "Brampton, Ontario, Canada",
   * "Markham, Ontario, Canada", "Montreal, Quebec, Canada",
   * "Quebec City, Quebec, Canada");
   * 
   * // Unrealistic example, multiple locations across the world, not attainable
   * by car, place_ID private final List<String> multipleCountryLocations =
   * Arrays.asList( "Ohio", "Miami");
   * 
   * // only addresses private final List<String> onlyAddresses =
   * Arrays.asList(a);
   * 
   * // only coordinates private final List<String> onlyCoordinates =
   * Arrays.asList(a);
   * 
   * // only placeIDs private final List<String> onlyPlaceIds = Arrays.asList(a);
   * 
   * 
   * // Mix of address, coordinates and place_ID private final List<String>
   * mixedLocationTypes = Arrays.asList(a);
   * 
   * @Test public void noLocations() { // A cookie is requested and is present in
   * the list. Should return cookie object
   * 
   * Cookie retrievedCookie = DistanceMatrixUtility.getCookie(request, "idToken");
   * Assert.assertNotNull(retrievedCookie);
   * Assert.assertEquals(retrievedCookie.getName(), "idToken");
   * Assert.assertEquals(retrievedCookie.getValue(), "sample_id_token"); }
   */

  @Test
  public void test() throws IOException {
    String actual = DistanceMatrixUtility.getDistanceMatrix();
    Assert.assertEquals("", actual);
  }
  
}
