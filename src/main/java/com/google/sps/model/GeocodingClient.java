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

/**
 * Contract for sending GET requests to the Google Geocoding API. Implement getGeocodingResult to
 * obtain the corresponding GeocodingResult of an address, getCoordinates to convert a Geocoding
 * result to its coordinates, getPlaceType to convert a Geocodiing result to its place type.
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
  GeocodingResult getGeocodingResult(String address) throws GeocodingException;

  /**
   * Parses for coordinates in a resulting call to the Geocoding API.
   *
   * @param result A GeocodingResult returned from the Geocoding API.
   * @return A LatLng representing coordinates.
   */
  public static LatLng getCoordinates(GeocodingResult result) {
    return result.geometry.location;
  }

  /**
   * Converts an AddressType to a PlaceType for the purpose of calling the Places API. PlaceType is
   * a subset of AddressType and hence, not all AddressTypes are supported by the Places API.
   *
   * @param addressType An AddressType to convert to a PlaceType
   * @return A PlaceType corresponding to an AddressType if available, null if not available.
   */
  public static PlaceType convertAddressTypeToPlaceType(AddressType addressType) {
    for (PlaceType placeType : PlaceType.values()) {
      if (placeType.toString().equals(addressType.toString())) {
        return placeType;
      }
    }
    return null;
  }

  /**
   * Determines if the address in which the Geocoding API is executed against is geocoded as an
   * exact match or a partial match. An exact match includes a valid street address or a mistyped
   * street address which is converted to the correct spelling by the Geocoding API. A partial match
   * is everything else.
   *
   * @param result A GeocodingResult returned from the Geocoding API.
   * @return True if result is a partial match, false otherwise.
   */
  public static boolean isPartialMatch(GeocodingResult result) {
    return result.partialMatch;
  }

  /**
   * Parses for a address type and converts it to a place type if available. This place type is used
   * to further determine a location which results in a route with the shortest travel time.
   *
   * @param result A GeocodingResult returned from the Geocoding API.
   * @return A PlaceType representing the type or null if no corresponding PlaceType is found.
   */
  public static PlaceType getPlaceType(GeocodingResult result) {
    return convertAddressTypeToPlaceType(result.types[0]);
  }
}
