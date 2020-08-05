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
    result.geometry = geometry;
    LatLng actualCoordinates = GeocodingResultUtility.getCoordinates(result);
    Assert.assertEquals(expectedCoordinates, actualCoordinates);
  }

  @Test
  public void isPartialMatch() {
    GeocodingResult result = new GeocodingResult();
    result.partialMatch = true;
    boolean actual = GeocodingResultUtility.isPartialMatch(result);
    Assert.assertTrue(actual);
  }

  @Test
  public void isNotPartialMatch() {
    GeocodingResult result = new GeocodingResult();
    result.partialMatch = false;
    boolean actual = GeocodingResultUtility.isPartialMatch(result);
    Assert.assertFalse(actual);
  }

  @Test
  public void getPlaceType() {
    GeocodingResult result = new GeocodingResult();
    result.types = new AddressType[] {AddressType.ACCOUNTING};
    Optional<PlaceType> actualPlaceType = GeocodingResultUtility.getPlaceType(result);
    Assert.assertEquals(PlaceType.ACCOUNTING, actualPlaceType.get());
  }

  @Test
  public void convertValidAddressTypeToPlaceType() {
    // PlaceType is a subset of AddressType. AddressType here is in the PlaceType enum class, hence
    // the same AddressType in the form of PlaceType is expected.
    Optional<PlaceType> actualPlaceType =
        GeocodingResultUtility.convertAddressTypeToPlaceType(AddressType.RESTAURANT);
    Assert.assertEquals(PlaceType.RESTAURANT, actualPlaceType.get());
  }

  @Test
  public void convertInvalidAddressTypeToPlaceType() {
    // PlaceType is a subset of AddressType. AddressType here is not present in the PlaceType enum
    // class, hence null is expected.
    Optional<PlaceType> actualPlaceType =
        GeocodingResultUtility.convertAddressTypeToPlaceType(AddressType.STREET_ADDRESS);
    Assert.assertEquals(Optional.empty(), actualPlaceType);
  }
}
