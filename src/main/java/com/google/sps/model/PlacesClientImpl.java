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

import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.RankBy;
import com.google.sps.exceptions.PlacesException;
import java.io.IOException;

/** Handles GET requests to the Google Places API */
public class PlacesClientImpl implements PlacesClient {
  private NearbySearchRequest placesService;

  private PlacesClientImpl(NearbySearchRequest service) {
    placesService = service;
  }

  /** Factory to create a PlacesClientImpl instance with given API key */
  public static class Factory implements PlacesClientFactory {
    @Override
    public PlacesClient getPlacesClient(String apiKey) {
      GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
      NearbySearchRequest service = new NearbySearchRequest(context);
      return new PlacesClientImpl(service);
    }
  }

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

  @Override
  public String searchNearby(LatLng location, PlaceType placeType, RankBy rankBy)
      throws PlacesException {
    try {
      PlacesSearchResponse response =
          placesService.location(location).type(placeType).rankby(rankBy).await();
      return getPlaceId(response);
    } catch (ApiException | InterruptedException | IOException e) {
      throw new PlacesException("Failed to get directions", e);
    }
  }
}
