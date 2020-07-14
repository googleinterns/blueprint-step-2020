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
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Handles GET requests to the Google Directions API */
public class DirectionsClientImpl implements DirectionsClient {
  private DirectionsApiRequest directionsService;

  DirectionsClientImpl(DirectionsApiRequest service) {
    directionsService = service;
  }

  /** Factory to create a DirectionsClientImpl instance with given API key */
  public static class Factory implements DirectionsClientFactory {
    /**
     * Gets a DirectionsClient which executes against the given API key.
     *
     * @param apiKey A string representing the API key to authenticate a Google Directions API call.
     * @return DirectionsClientImpl instance which executes against the given API key.
     */
    @Override
    public DirectionsClient getDirectionsClient(String apiKey) {
      GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
      DirectionsApiRequest service = DirectionsApi.newRequest(context);
      return new DirectionsClientImpl(service);
    }
  }

  /**
   * Gets the result of a GET request to the Google Directions API.
   *
   * @param destination A string representing the destination to get directions
   *                    to.
   * @param origin      A string representing the origin to get directions from.
   * @param waypoints   A list of string consisting of waypoints to visit between
   *                    the destination and the origin.
   * @return A string representing the result from a GET request to the Google
   *         Directions API.
   * @throws Exception
   */
  public List<String> getDirections(String origin, String destination, String[] waypoints) throws Exception {
    try {
      DirectionsResult result =
          directionsService
              .origin(origin)
              .destination(destination)
              .waypoints(waypoints)
              .optimizeWaypoints(true)
              .await();
      List<String> listLegs = new ArrayList<>();
      DirectionsRoute routes[] = result.routes;
      for (DirectionsRoute route : routes) {
        DirectionsLeg legs[] = route.legs;
        for (DirectionsLeg leg : legs) {
          listLegs.add(leg.toString());
        }
      }
      return listLegs;
    } catch (ApiException | InterruptedException | IOException e) {
      e.printStackTrace();
      throw e;
    }
  }
}
