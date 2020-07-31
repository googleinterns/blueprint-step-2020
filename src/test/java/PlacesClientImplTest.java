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
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.sps.model.PlacesClient;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test that PlacesClientImpl correctly converts a PlacesSearchResponse object obtained from the
 * Places API into a list of formatted addresses
 */
@RunWith(JUnit4.class)
public class PlacesClientImplTest {

  private static PlacesSearchResult googleKitchener;
  private static PlacesSearchResult googleMontreal;
  private static final String GOOGLE_KITCHENER_NAME = "Google Kitchener";
  private static final String GOOGLE_MONTREAL_NAME = "Google Montreal";
  private static final String GOOGLE_KITCHENER_FORMATTED_ADDRESS =
      "51 Breithaupt St, Kitchener, ON N2H 5G5";
  private static final String GOOGLE_MONTREAL_FORMATTED_ADDRESS =
      "1253 McGill College Ave, Montreal, Quebec H3B 2Y5";

  @Before
  public void setUp() {
    googleKitchener = new PlacesSearchResult();
    googleMontreal = new PlacesSearchResult();
    googleKitchener.name = GOOGLE_KITCHENER_NAME;
    googleMontreal.name = GOOGLE_MONTREAL_NAME;
    googleKitchener.formattedAddress = GOOGLE_KITCHENER_FORMATTED_ADDRESS;
    googleMontreal.formattedAddress = GOOGLE_MONTREAL_FORMATTED_ADDRESS;
  }

  @Test
  public void noPlaceResult() throws Exception {
    PlacesSearchResponse response = new PlacesSearchResponse();
    response.results = new PlacesSearchResult[] {};
    List<String> actual = PlacesClient.getFormattedAddresses(response);
    Assert.assertEquals(ImmutableList.of(), actual);
  }

  @Test
  public void onePlaceResult() throws Exception {
    PlacesSearchResponse response = new PlacesSearchResponse();
    response.results = new PlacesSearchResult[] {googleKitchener};
    List<String> actual = PlacesClient.getFormattedAddresses(response);
    Assert.assertEquals(ImmutableList.of(GOOGLE_KITCHENER_FORMATTED_ADDRESS), actual);
  }

  @Test
  public void multiplePlacesResult() throws Exception {
    PlacesSearchResponse response = new PlacesSearchResponse();
    response.results = new PlacesSearchResult[] {googleKitchener, googleMontreal};
    List<String> actual = PlacesClient.getFormattedAddresses(response);
    Assert.assertEquals(
        ImmutableList.of(GOOGLE_KITCHENER_FORMATTED_ADDRESS, GOOGLE_MONTREAL_FORMATTED_ADDRESS),
        actual);
  }
}
