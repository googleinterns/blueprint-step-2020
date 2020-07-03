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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/** Utility class for the Google Directions API. */
public final class DirectionsUtility {

  /** Prevents instances of this class. */
  private DirectionsUtility() {}

  /**
   * Sets multiple waypoints to the waypoints parameter of the Google Directions API.
   *
   * @param waypoints A list of string containing the multiple waypoints to set as parameter values.
   * @return A string of waypoints to set to the waypoints parameter.
   */
  private static String buildWaypoints(List<String> waypoints) {
    StringBuilder waypointsBuilder = new StringBuilder();
    boolean firstIteration = true;
    for (String waypoint : waypoints) {
      if (firstIteration) {
        waypointsBuilder.append("via:" + waypoint);
        firstIteration = false;
        continue;
      }
      waypointsBuilder.append("|via:" + waypoint);
    }
    return waypointsBuilder.toString();
  }

  /**
   * Builds custom URI to send a HTTP GET Request to the Google Directions API.
   *
   * @param destination A string representing the destination to get directions to.
   * @param origin A string representing the origin to get directions from.
   * @param waypoints A list of string consisting of waypoints to visit between the destination and
   *     the origin.
   * @param apiKey A string representing the API key to authenticate a Google Directions API call.
   * @return A string representing the URI.
   * @throws URISyntaxException Indicates that the built string could not be parsed as a URI.
   */
  public static String getDirectionsUri(
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

  /**
   * Gets the result of a call to the Directions API.
   *
   * @param destination A string representing the destination to get directions to.
   * @param origin A string representing the origin to get directions from.
   * @param waypoints A list of string consisting of waypoints to visit between the destination and
   *     the origin.
   * @param apiKey A string representing the API key to authenticate a Google Directions API call.
   * @return A JSONObject representing the result from a call to the Google Directions API.
   * @throws IOException Indicates failed or interrupted I/O operations.
   * @throws JSONException Indicates a problem with the JSON API.
   * @throws URISyntaxException Indicates that the built string could not be parsed as a URI.
   */
  public static JSONObject getDirections(
      String destination, String origin, List<String> waypoints, String apiKey)
      throws IOException, JSONException, URISyntaxException {
    String uri = getDirectionsUri(destination, origin, waypoints, apiKey);
    String jsonString = Request.Get(uri).execute().returnContent().toString();
    return new JSONObject(jsonString);
  }
}
