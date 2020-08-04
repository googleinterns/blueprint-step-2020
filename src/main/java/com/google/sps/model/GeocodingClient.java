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

package com.google.sps.model;

import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.sps.exceptions.GeocodingException;
import java.util.Arrays;
import java.util.List;

/**
 * Contract for sending GET requests to the Google Geocoding API. Implement getGeocodingResult to
 * obtain the corresponding GeocodingResult of an address.
 */
public interface GeocodingClient {
  /**
   * Sends a GET request to the Google Geocoding API to convert from address to GeocodingResult.
   *
   * @param address A String representing the address to geocode.
   * @return A GeocodingResult returned from the Geocoding API.
   * @throws GeocodingException A custom exception is thrown to signal an error pertaining to the
   *     Geocoding API.
   */
  List<GeocodingResult> getGeocodingResult(String address) throws GeocodingException;

  /**
   * Parses for coordinates in a resulting call to the Geocoding API.
   *
   * @param result A GeocodingResult returned from the Geocoding API.
   * @return A LatLng representing coordinates.
   */
  public static LatLng getCoordinates(List<GeocodingResult> results) {
    for (GeocodingResult result : results) {
      List<AddressType> types = Arrays.asList(result.types);
      for (AddressType type : types) {
        if (type == AddressType.STREET_ADDRESS) {
          return result.geometry.location;
        }
      }
    }
    return results.get(0).geometry.location;
  }

  /**
   * Parses for a address type and converts it to a place type if available. This place type is used
   * to further determine a location which results in a route with the shortest travel time.
   *
   * @param result A GeocodingResult returned from the Geocoding API.
   * @return A PlaceType representing the type or null if no corresponding PlaceType is found.
   */
  public static PlaceType getPlaceType(GeocodingResult result) {
    AddressType addressType = result.types[0];
    for (PlaceType placeType : PlaceType.values()) {
      if (placeType.toString().equals(addressType.toString())) {
        return placeType;
      }
    }
    return null;
  }

  public static PlaceType convertToPlaceType(String location) {
    for (PlaceType placeType : PlaceType.values()) {
      if (placeType.toString().equals(location.toLowerCase().replace(" ", "_"))) {
        return placeType;
      }
    }
    return null;
  }

  public static boolean isStreetAddress(List<GeocodingResult> results) {
    for (GeocodingResult result : results) {
      List<AddressType> types = Arrays.asList(result.types);
      for (AddressType type : types) {
        if (type == AddressType.STREET_ADDRESS) {
          return true;
        }
      }
    }
    return false;
  }
}
