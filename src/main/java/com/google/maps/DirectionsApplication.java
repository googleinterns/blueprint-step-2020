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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.client.utils.URIBuilder;

/** Application class which fulfills the declared Directions consumer contract. */
public class DirectionsApplication implements DirectionsConsumer {
  private DirectionsService directionsService;

  /** Changes constructor to require an instantiated service. */
  public DirectionsApplication(DirectionsService service) {
    this.directionsService = service;
  }

  private String buildWaypoints(List<String> waypoints) {
    StringBuilder waypointsBuilder = new StringBuilder("optimize:true");
    if (waypoints.isEmpty()) {
      return waypointsBuilder.toString();
    }
    for (String waypoint : waypoints) {
      waypointsBuilder.append("|" + waypoint);
    }
    return waypointsBuilder.toString();
  }

  private String getDirectionsUri(
      String destination, String origin, List<String> waypoints, String apiKey)
      throws URISyntaxException {
    URI uri =
        new URIBuilder()
            .setScheme("https")
            .setHost("maps.googleapis.com")
            .setPath("/maps/api/directions/json")
            .setParameter("origin", origin)
            .setParameter("destination", destination)
            .setParameter("waypoints", buildWaypoints(waypoints))
            .setParameter("departure_time", "now")
            .setParameter("key", apiKey)
            .build();
    return uri.toString();
  }

  @Override
  public String processDirections(
      String destination, String origin, List<String> waypoints, String apiKey) {
    String uri;
    try {
      uri = getDirectionsUri(destination, origin, waypoints, apiKey);
      return this.directionsService.getDirections(uri);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }
}
