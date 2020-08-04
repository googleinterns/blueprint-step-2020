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
import com.google.sps.model.GeocodingClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test that GeocodingClientImpl correctly converts a GeocodingResult object obtained from the
 * Geocoding API into their respective coordinates or place types.
 */
@RunWith(JUnit4.class)
public class GeocodingClientImplTest {

  @Test
  public void getCoordinates() {
    GeocodingResult result = new GeocodingResult();
    LatLng expectedCoordinates = new LatLng(0, 0);
    Geometry geometry = new Geometry();
    geometry.location = expectedCoordinates;
    result.types = new AddressType[] {AddressType.STREET_ADDRESS};
    result.geometry = geometry;
    LatLng actualCoordinates = GeocodingClient.getCoordinates(ImmutableList.of(result));
    Assert.assertEquals(expectedCoordinates, actualCoordinates);
  }

  @Test
  public void getPlaceType() {
    GeocodingResult result = new GeocodingResult();
    result.types = new AddressType[] {AddressType.ACCOUNTING};
    PlaceType actualPlaceType = GeocodingClient.getPlaceType(result);
    Assert.assertEquals(PlaceType.ACCOUNTING, actualPlaceType);
  }

  @Test
  public void convertValidAddressTypeToPlaceType() {
    // PlaceType is a subset of AddressType. AddressType here is in the PlaceType enum class, hence
    // the same AddressType in the form of PlaceType is expected.
    PlaceType actualPlaceType =
        GeocodingClient.convertToPlaceType("restaurant");
    Assert.assertEquals(PlaceType.RESTAURANT, actualPlaceType);
  }

  @Test
  public void convertInvalidAddressTypeToPlaceType() {
    // PlaceType is a subset of AddressType. AddressType here is not present in the PlaceType enum
    // class, hence null is expected.
    PlaceType actualPlaceType =
        GeocodingClient.convertToPlaceType("street address");
    Assert.assertNull(actualPlaceType);
  }

  @Test
  public void test() {
    GeocodingResult result = new GeocodingResult();
    result.types = new AddressType[] {AddressType.STREET_ADDRESS};
    Assert.assertTrue(GeocodingClient.isStreetAddress(ImmutableList.of(result)));
  }
}
