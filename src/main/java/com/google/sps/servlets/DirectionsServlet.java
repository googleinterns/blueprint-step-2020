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

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.maps.DirectionsClient;
import com.google.maps.DirectionsClientFactory;
import com.google.maps.DirectionsClientImpl;
import com.google.maps.DirectionsException;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves key information from optimizing between addresses. */
@WebServlet("/directions")
public class DirectionsServlet extends HttpServlet {

  private final DirectionsClientFactory directionsClientFactory;

  /** Construct servlet with default DirectionsClient. */
  public DirectionsServlet() {
    directionsClientFactory = new DirectionsClientImpl.Factory();
  }

  /**
   * Construct servlet with explicit implementation of DirectionsClient.
   *
   * @param factory A DirectionsClientFactory containing the implementation of
   *     DirectionsClientFactory.
   */
  public DirectionsServlet(DirectionsClientFactory factory) {
    directionsClientFactory = factory;
  }

  /**
   * Returns the most optimal order of travel between addresses.
   *
   * @param request HTTP request from the client.
   * @param response HTTP response to the client.
   * @throws ServletException
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    String apiKey = ""; // TODO: Merge ApiKeyProvider to make a request here (Issue #77)
    String origin =
        "Montreal, QC"; // TODO: Replace with getOrigin from Tasks upon implementation (Issue #78)
    String destination =
        "Montreal, QC"; // TODO: Replace with getDestination from Tasks implementation (Issue #78)
    List<String> waypoints =
        ImmutableList.of(
            "Windsor, ON", "Waterloo, ON", "Kitchener, ON"); // TODO: Replace with getWaypoints from
    // Tasks implementation (Issue #78)
    try {
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      List<String> directions = directionsClient.getDirections(origin, destination, waypoints);
      Gson gson = new Gson();
      String directionsJson = gson.toJson(directions);
      response.setContentType("application/json");
      response.getWriter().println(directionsJson);
    } catch (DirectionsException | IOException e) {
      throw new ServletException(e);
    }
  }
}