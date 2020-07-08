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

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.utility.DirectionsUtility;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/directions")
public class DirectionsServlet extends HttpServlet {

  private String origin;
  private String destination;
  private List<String> waypoints;
  private String apiKey;

  /**
   * Converts object to JSON and sends it to a HTTP Servlet Response.
   * @param response Response to send object to.
   * @param object Object to convert to JSON and send.
   * @throws IOException
   */
  public static void sendJson(HttpServletResponse response, Object object) throws IOException {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    response.getWriter().println(json);
  }

  /**
   * Get parameters from HTML to store in this class.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    origin = request.getParameter("origin");
    destination = request.getParameter("destination");
    String waypointsString = request.getParameter("waypoints");
    apiKey = request.getParameter("api-key");
    waypoints = Arrays.asList(waypointsString.split(";"));
    response.sendRedirect("/index.html");
  }

  /**
   * Get directions using the parameters stored in this class.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      JSONObject directions =
          DirectionsUtility.getDirections(destination, origin, waypoints, apiKey);
      sendJson(response, directions);
    } catch (JSONException e) {
      System.out.println("JSONException Occurred.");
    } catch (URISyntaxException e) {
      System.out.println("URISyntaxException Occurred.");
    }
  }
}
