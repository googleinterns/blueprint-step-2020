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

package com.google.sps.utility;

import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.sps.exceptions.GeocodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Utility class to extract data from GeocodingResult objects. */
public class GeocodingResultUtility {
  /**
   * Parses for the first coordinate which is of a street address type in a result from the
   * Geocoding API. If no street address type results are found, the coordinates of the first result
   * is returned.
   *
   * @param results A List of GeocodingResult returned from the Geocoding API.
   * @return A LatLng representing coordinates.
   */
  public static Optional<LatLng> getCoordinates(List<GeocodingResult> results) {
    for (GeocodingResult result : results) {
      for (AddressType type : result.types) {
        if (type == AddressType.STREET_ADDRESS) {
          return Optional.ofNullable(result.geometry.location);
        }
      }
    }
    if (results.size() == 0) {
      return Optional.empty();
    }
    return Optional.ofNullable(results.get(0).geometry.location);
  }

  /**
   * Converts a location to a PlaceType if it exists in the PlaceType enum class.
   *
   * @param location A string representing a location.
   * @return An optional containing a PlaceType if an equivalent is found for the location
   *     specified, an empty optional otherwise.
   */
  public static Optional<PlaceType> convertToPlaceType(String location) {
    return Arrays.asList(PlaceType.values()).stream()
        .filter(placeType -> placeType.name().equalsIgnoreCase(location.replace(" ", "_")))
        .findFirst();
  }

  /**
   * Determines whether any of the results are street addresses.
   *
   * @param results A List of GeocodingResult returned from the Geocoding API.
   * @return True if any of the results are street addresses, false otherwise.
   */
  public static boolean hasStreetAddress(List<GeocodingResult> results) {
    for (GeocodingResult result : results) {
      if (Arrays.asList(result.types).contains(AddressType.STREET_ADDRESS)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Parses for an address type and converts it to a place type if an equivalent is available. This
   * place type is used to further determine a location which results in a route with the shortest
   * travel time.
   *
   * @param result A GeocodingResult returned from the Geocoding API.
   * @return An optional containing a PlaceType representing the type or an empty optional if no
   *     corresponding PlaceType is found.
   * @throws GeocodingException An exception thrown when an error occurs with the Geocoding API.
   */
  public static Optional<PlaceType> getPlaceType(GeocodingResult result) throws GeocodingException {
    if (result.types.length == 0) {
      throw new GeocodingException("No place types in geocoding result");
    }
    AddressType addressType = result.types[0];
    return Arrays.asList(PlaceType.values()).stream()
        .filter(placeType -> placeType.name().equals(addressType.name()))
        .findFirst();
  }
}
