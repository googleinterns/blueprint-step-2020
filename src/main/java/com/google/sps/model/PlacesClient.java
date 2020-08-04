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

import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.RankBy;
import com.google.sps.exceptions.PlacesException;

/**
 * Contract for sending GET requests to the Google Places API. Implement searchNearby to obtain
 * formatted addresses of nearby locations satisfying criteria set in the parameters from the API.
 */
public interface PlacesClient {
  /**
   * Sends a GET request to the Google Places API for nearby locations.
   *
   * @param location A LatLng with coordinates of the location to search nearby.
   * @param placeType A PlaceType representing the type of place to look for.
   * @param rankBy A RankBy representing how to sort results. e.g. distance to location.
   * @return A list of formatted addresses from the results of the GET request.
   * @throws PlacesException A custom exception is thrown to signal an error pertaining to the
   *     Places API.
   */
  String searchNearby(LatLng location, PlaceType placeType, RankBy rankBy) throws PlacesException;

  /**
   * Gets place ID of first result from the response from the Google Places API. Scope of method is
   * public for testing purposes.
   *
   * @param response The PlacesSearchResponse object to get formatted addresses from
   */
  public static String getPlaceId(PlacesSearchResponse response) {
    if (response.results.length != 0) {
      return response.results[0].placeId;
    }
    return "";
  }
}
