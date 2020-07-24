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

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.sps.exceptions.DirectionsException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/** Handles GET requests to the Google Directions API */
public class DirectionsClientImpl implements DirectionsClient {
  private DirectionsApiRequest directionsService;

  private DirectionsClientImpl(DirectionsApiRequest service) {
    directionsService = service;
  }

  /** Factory to create a DirectionsClientImpl instance with given API key */
  public static class Factory implements DirectionsClientFactory {
    @Override
    public DirectionsClient getDirectionsClient(String apiKey) {
      GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
      DirectionsApiRequest service = DirectionsApi.newRequest(context);
      return new DirectionsClientImpl(service);
    }
  }

  /**
   * Converts DirectionsResult into a List<String> Scope of method is public for testing purposes
   *
   * @param result The DirectionsResult object to convert into a List<String>
   */
  public static List<String> parseDirectionsResult(DirectionsResult result) {
    return Arrays.asList(result.routes).stream()
        .map(
            route ->
                Arrays.asList(route.legs).stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getDirections(String origin, String destination, List<String> waypoints)
      throws DirectionsException {
    try {
      DirectionsResult result =
          directionsService
              .origin(origin)
              .destination(destination)
              .waypoints(waypoints.toArray(new String[0]))
              .optimizeWaypoints(true)
              .await();
      return parseDirectionsResult(result);
    } catch (ApiException | InterruptedException | IOException e) {
      throw new DirectionsException("Failed to get directions", e);
    }
  }
}
