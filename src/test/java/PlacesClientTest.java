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

import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.sps.model.PlacesClientImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test that PlacesClientImpl correctly converts a PlacesSearchResponse object obtained from the
 * Places API into a list of Place IDs
 */
@RunWith(JUnit4.class)
public class PlacesClientTest {

  private static PlacesSearchResult googleKitchener;
  private static PlacesSearchResult googleMontreal;
  private static final String GOOGLE_KITCHENER_NAME = "Google Kitchener";
  private static final String GOOGLE_MONTREAL_NAME = "Google Montreal";
  private static final String GOOGLE_KITCHENER_PLACE_ID = "Google Kitchener Place ID";
  private static final String GOOGLE_MONTREAL_PLACE_ID = "Google Montreal Place ID";

  @Before
  public void setUp() {
    googleKitchener = new PlacesSearchResult();
    googleMontreal = new PlacesSearchResult();
    googleKitchener.name = GOOGLE_KITCHENER_NAME;
    googleMontreal.name = GOOGLE_MONTREAL_NAME;
    googleKitchener.placeId = GOOGLE_KITCHENER_PLACE_ID;
    googleMontreal.placeId = GOOGLE_MONTREAL_PLACE_ID;
  }

  @Test
  public void noPlaceResult() throws Exception {
    PlacesSearchResponse response = new PlacesSearchResponse();
    response.results = new PlacesSearchResult[] {};
    String actual = PlacesClientImpl.getPlaceId(response);
    Assert.assertEquals("", actual);
  }

  @Test
  public void onePlaceResult() throws Exception {
    PlacesSearchResponse response = new PlacesSearchResponse();
    response.results = new PlacesSearchResult[] {googleKitchener};
    String actual = PlacesClientImpl.getPlaceId(response);
    Assert.assertEquals(GOOGLE_KITCHENER_PLACE_ID, actual);
  }

  @Test
  public void multiplePlacesResult() throws Exception {
    // Only formatted address of first result is returned, second result is ignored
    PlacesSearchResponse response = new PlacesSearchResponse();
    response.results = new PlacesSearchResult[] {googleKitchener, googleMontreal};
    String actual = PlacesClientImpl.getPlaceId(response);
    Assert.assertEquals(GOOGLE_KITCHENER_PLACE_ID, actual);
  }
}
