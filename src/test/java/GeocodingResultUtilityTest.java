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
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.sps.utility.GeocodingResultUtility;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test that GeocodingResultUtility correctly extracts information from GeocodingResult objects
 * obtained from the Geocoding API.
 */
@RunWith(JUnit4.class)
public class GeocodingResultUtilityTest {

  @Test
  public void getCoordinates() {
    GeocodingResult result = new GeocodingResult();
    LatLng expectedCoordinates = new LatLng(0, 0);
    Geometry geometry = new Geometry();
    geometry.location = expectedCoordinates;
    result.types = new AddressType[] {AddressType.STREET_ADDRESS};
    result.geometry = geometry;
    Optional<LatLng> actualCoordinates =
        GeocodingResultUtility.getCoordinates(ImmutableList.of(result));
    Assert.assertEquals(expectedCoordinates, actualCoordinates.get());
  }

  @Test
  public void getPlaceType() {
    GeocodingResult result = new GeocodingResult();
    result.types = new AddressType[] {AddressType.ACCOUNTING};
    Optional<PlaceType> actualPlaceType = GeocodingResultUtility.getPlaceType(result);
    Assert.assertEquals(PlaceType.ACCOUNTING, actualPlaceType.get());
  }

  @Test
  public void convertRestaurantToPlaceType() {
    // PlaceType is a subset of AddressType. AddressType here is in the PlaceType enum class, hence
    // the same AddressType in the form of PlaceType is expected.
    Optional<PlaceType> actualPlaceType = GeocodingResultUtility.convertToPlaceType("restaurant");
    Assert.assertEquals(PlaceType.RESTAURANT, actualPlaceType.get());
  }

  @Test
  public void convertStreetAddressToPlaceType() {
    // PlaceType is a subset of AddressType. AddressType here is not present in the PlaceType enum
    // class, hence empty optional is expected.
    Optional<PlaceType> actualPlaceType =
        GeocodingResultUtility.convertToPlaceType("street address");
    Assert.assertEquals(Optional.empty(), actualPlaceType);
  }

  @Test
  public void hasStreetAddress() {
    GeocodingResult result = new GeocodingResult();
    result.types = new AddressType[] {AddressType.STREET_ADDRESS};
    Assert.assertTrue(GeocodingResultUtility.hasStreetAddress(ImmutableList.of(result)));
  }
}
