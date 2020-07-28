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

package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.common.collect.ImmutableList;
import com.google.sps.exceptions.DirectionsException;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.DirectionsClient;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.model.DirectionsClientImpl;
import com.google.sps.utility.JsonUtility;
import com.google.sps.utility.KeyProvider;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves key information from optimizing between addresses. */
@WebServlet("/go")
public class GoServlet extends AuthenticatedHttpServlet {

  private final DirectionsClientFactory directionsClientFactory;
  private final String apiKey;
  private final String origin;
  private final String destination;
  private final List<String> waypoints;

  /**
   * Construct servlet with default DirectionsClient.
   *
   * @throws IOException
   */
  public GoServlet() throws IOException {
    directionsClientFactory = new DirectionsClientImpl.Factory();
    apiKey = (new KeyProvider()).getKey("apiKey");
    origin = "Waterloo, ON";
    destination = "Waterloo, ON";
    waypoints = ImmutableList.of("Montreal, QC", "Windsor, ON", "Kitchener, ON");
  }

  /**
   * Construct servlet with explicit implementation of DirectionsClient.
   *
   * @param factory A DirectionsClientFactory containing the implementation of
   *     DirectionsClientFactory.
   */
  public GoServlet(
      DirectionsClientFactory directionsClientFactory,
      String fakeApiKey,
      String fakeOrigin,
      String fakeDestination,
      List<String> fakeWaypoints) {
    this.directionsClientFactory = directionsClientFactory;
    apiKey = fakeApiKey;
    origin = fakeOrigin;
    destination = fakeDestination;
    waypoints = fakeWaypoints;
  }

  /**
   * Returns the most optimal order of travel between addresses.
   *
   * @param request HTTP request from the client.
   * @param response HTTP response to the client.
   * @throws ServletException
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws ServletException {
    assert googleCredential != null
        : "Null credentials (i.e. unauthenticated requests) should already be handled";
    // Get relevant tasks using task titles
    // Get descriptions of relevant tasks
    // Parse for locations from descriptions

    // Split waypoints into exact addresses and generic locations by looking for the presence of ","
    // For every exact address, generic location is sent to Places to obtain the closest match

    // Create 'route' for every permutation send to Directions
    // Route with shortest travel time is kept at every iteration
    try {
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      List<String> directions = directionsClient.getDirections(origin, destination, waypoints);
      JsonUtility.sendJson(response, directions);
    } catch (DirectionsException | IOException e) {
      throw new ServletException(e);
    }
  }
}
