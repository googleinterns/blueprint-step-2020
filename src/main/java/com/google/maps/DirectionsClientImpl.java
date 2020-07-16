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

package com.google.maps;

import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import java.io.IOException;
import java.util.Arrays;
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

  @Override
  public List<String> getDirections(String origin, String destination, List<String> waypoints)
      throws DirectionsException {
    String[] waypointsArray = waypoints.toArray(new String[0]);
    try {
      DirectionsResult result =
          directionsService
              .origin(origin)
              .destination(destination)
              .waypoints(waypointsArray)
              .optimizeWaypoints(true)
              .await();

      return Arrays.asList(result.routes).stream()
          .map(
              route -> {
                return Arrays.asList(route.legs).stream()
                    .map(
                        leg -> {
                          return leg.toString();
                        })
                    .collect(Collectors.toList());
              })
          .flatMap(leg -> leg.stream())
          .collect(Collectors.toList());

    } catch (ApiException | InterruptedException | IOException e) {
      throw new DirectionsException("Failed to get directions " + e);
    }
  }
}
