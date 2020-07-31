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
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.RankBy;
import com.google.sps.exceptions.DirectionsException;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.DirectionsClient;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.model.DirectionsClientImpl;
import com.google.sps.model.PlacesClient;
import com.google.sps.model.PlacesClientFactory;
import com.google.sps.model.PlacesClientImpl;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.model.TasksClientImpl;
import com.google.sps.utility.JsonUtility;
import com.google.sps.utility.KeyProvider;
import com.google.sps.utility.LocationsUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves key information from optimizing between addresses. */
@WebServlet("/go")
public class GoServlet extends AuthenticatedHttpServlet {

  private final DirectionsClientFactory directionsClientFactory;
  private final PlacesClientFactory placesClientFactory;
  private final TasksClientFactory tasksClientFactory;
  private final String apiKey;

  /**
   * Construct servlet with default DirectionsClient.
   *
   * @throws IOException
   */
  public GoServlet() throws IOException {
    directionsClientFactory = new DirectionsClientImpl.Factory();
    placesClientFactory = new PlacesClientImpl.Factory();
    tasksClientFactory = new TasksClientImpl.Factory();
    apiKey = (new KeyProvider()).getKey("apiKey");
  }

  /**
   * Construct servlet with explicit implementation of DirectionsClient.
   *
   * @param factory A DirectionsClientFactory containing the implementation of
   *     DirectionsClientFactory.
   */
  public GoServlet(
      DirectionsClientFactory directionsClientFactory,
      PlacesClientFactory placesClientFactory,
      TasksClientFactory tasksClientFactory,
      String fakeApiKey,
      String fakeOrigin,
      String fakeDestination,
      List<String> fakeWaypoints) {
    this.directionsClientFactory = directionsClientFactory;
    this.placesClientFactory = placesClientFactory;
    this.tasksClientFactory = tasksClientFactory;
    apiKey = fakeApiKey;
  }

  private List<Task> getTasks(TasksClient tasksClient) throws IOException {
    List<TaskList> taskLists = tasksClient.listTaskLists();
    List<Task> tasks = new ArrayList<>();
    for (TaskList taskList : taskLists) {
      tasks.addAll(tasksClient.listTasks(taskList));
    }
    return tasks;
  }

  /**
   * Returns the most optimal order of travel between addresses.
   *
   * @param request HTTP request from the client.
   * @param response HTTP response to the client.
   * @throws ServletException
   * @throws IOException
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws ServletException, IOException {
    assert googleCredential != null
        : "Null credentials (i.e. unauthenticated requests) should already be handled";
    // Get all tasks from user's tasks account TODO: Get relevant tasks using task titles
    TasksClient tasksClient = tasksClientFactory.getTasksClient(googleCredential);
    List<Task> tasks = getTasks(tasksClient);

    // Get descriptions of relevant tasks
    // Parse for locations from descriptions
    List<String> originList = LocationsUtility.getLocations("Origin", tasks);
    List<String> destinationList = LocationsUtility.getLocations("Destination", tasks);
    List<String> waypoints = LocationsUtility.getLocations("Waypoint", tasks);

    // Split waypoints into exact addresses and generic locations by looking for the presence of ","
    // For every exact address, generic location is sent to Places to obtain the closest match


    try {
      String origin = originList.get(0);
      String destination = destinationList.get(0);
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      List<String> directions = directionsClient.getDirections(origin, destination, waypoints);
      JsonUtility.sendJson(response, directions);
    } catch (IndexOutOfBoundsException e) {
      throw new ServletException("Either origin or destination not found.");
    } catch (DirectionsException | IOException e) {
      throw new ServletException(e);
    }
  }

  public void generatePermutations(List<List<String>> searchResults, List<List<String>> result, int depth, List<String> current) {
    if (depth == searchResults.size()) {
        result.add(current);
        return;
    }

    for (int i = 0; i < searchResults.get(depth).size(); ++i) {
      current.add(searchResults.get(depth).get(i));
      generatePermutations(searchResults, result, depth + 1, current);
    }
  }

  private List<String> optimizeSearchNearbyWaypoints(String origin, String destination, List<String> exactAddressWaypoints, List<String> searchNearbyWaypoints)
      throws ServletException {
    try {
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      PlacesClient placesClient = placesClientFactory.getPlacesClient(apiKey);
      List<String> allExactAddress = exactAddressWaypoints;
      allExactAddress.add(origin);
      allExactAddress.add(destination);

      List<List<String>> allSearchNearbyResults = new ArrayList<>();

      // Geocode API
      List<LatLng> allExactAddressCoordinates = ImmutableList.of();
      for (LatLng coordinate : allExactAddressCoordinates) {
        for (String query : searchNearbyWaypoints) {
          PlaceType placeType = PlaceType.RESTAURANT; // Parse query for PlaceType
          RankBy rankBy = RankBy.DISTANCE;
          List<String> searchNearbyResults = placesClient.searchNearby(coordinate, placeType, rankBy);
          allSearchNearbyResults.add(searchNearbyResults);
        }
      }

      List<List<String>> allWaypointCombinations = new ArrayList<List<String>>();
      for (String waypoint : exactAddressWaypoints) {
        allSearchNearbyResults.add(ImmutableList.of(waypoint));
      }
      LocationsUtility.generateCombinations(allSearchNearbyResults, allWaypointCombinations, 0, new ArrayList<String>());

      int minTravelTime = 0;
      List<String> mostOptimalWaypointCombination;

      for (List<String> waypointCombination : allWaypointCombinations) {
        int travelTime = directionsClient.getTotalTravelTime(origin, destination, waypointCombination);
        if (minTravelTime == 0 || travelTime < minTravelTime) {
          minTravelTime = travelTime;
          mostOptimalWaypointCombination = waypointCombination;
        }
      }
      return mostOptimalWaypointCombination;
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
