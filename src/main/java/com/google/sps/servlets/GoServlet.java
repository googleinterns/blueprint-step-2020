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
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.RankBy;
import com.google.sps.exceptions.DirectionsException;
import com.google.sps.exceptions.GeocodingException;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.DirectionsClient;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.model.DirectionsClientImpl;
import com.google.sps.model.GeocodingClient;
import com.google.sps.model.GeocodingClientFactory;
import com.google.sps.model.GeocodingClientImpl;
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
import java.util.List;
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
  private final GeocodingClientFactory geocodingClientFactory;
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
    geocodingClientFactory = new GeocodingClientImpl.Factory();
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
      GeocodingClientFactory geocodingClientFactory,
      String fakeApiKey,
      String fakeOrigin,
      String fakeDestination,
      List<String> fakeWaypoints) {
    this.directionsClientFactory = directionsClientFactory;
    this.placesClientFactory = placesClientFactory;
    this.tasksClientFactory = tasksClientFactory;
    this.geocodingClientFactory = geocodingClientFactory;
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
    // Get all tasks from user's tasks account TODO: Get relevant tasks using task
    // titles
    TasksClient tasksClient = tasksClientFactory.getTasksClient(googleCredential);
    DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
    GeocodingClient geocodingClient = geocodingClientFactory.getGeocodingClient(apiKey);
    List<Task> tasks = getTasks(tasksClient);

    // Get descriptions of relevant tasks
    // Parse for locations from descriptions
    List<String> originList = LocationsUtility.getLocations("Origin", tasks);
    List<String> destinationList = LocationsUtility.getLocations("Destination", tasks);
    List<String> waypoints = LocationsUtility.getLocations("Waypoint", tasks);

    // Split waypoints into exact addresses and generic locations by looking for the
    // presence of ","
    // For every exact address, generic location is sent to Places to obtain the
    // closest match

    List<String> mostOptimalWaypointCombination =
        optimizeSearchNearbyWaypoints(originList, destinationList, waypoints);

    try {
      String origin = originList.get(0);
      String destination = destinationList.get(0);
      DirectionsResult directionsResult =
          directionsClient.getDirections(origin, destination, mostOptimalWaypointCombination);
      List<String> directions = DirectionsClient.parseDirectionsResult(directionsResult);
      JsonUtility.sendJson(response, directions);
    } catch (IndexOutOfBoundsException e) {
      throw new ServletException("Either origin or destination not found.");
    } catch (DirectionsException | IOException e) {
      throw new ServletException(e);
    }
  }

  public void generatePermutations(
      List<List<String>> searchResults,
      List<List<String>> result,
      int depth,
      List<String> current) {
    if (depth == searchResults.size()) {
      result.add(current);
      return;
    }

    for (int i = 0; i < searchResults.get(depth).size(); ++i) {
      current.add(searchResults.get(depth).get(i));
      generatePermutations(searchResults, result, depth + 1, current);
    }
  }

  private List<String> optimizeSearchNearbyWaypoints(
      List<String> originList, List<String> destinationList, List<String> waypoints)
      throws ServletException {
    try {
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      PlacesClient placesClient = placesClientFactory.getPlacesClient(apiKey);
      GeocodingClient geocodingClient;

      LatLng originAsCoordinates;
      LatLng destinationAsCoordinates;
      List<LatLng> waypointsAsCoordinates = new ArrayList<>();
      List<PlaceType> waypointsAsPlaceTypes = new ArrayList<>();

      try {
        geocodingClient = geocodingClientFactory.getGeocodingClient(apiKey);
        originAsCoordinates =
            GeocodingClient.getCoordinates(geocodingClient.getGeocodingResult(originList.get(0)));
        geocodingClient = geocodingClientFactory.getGeocodingClient(apiKey);
        destinationAsCoordinates =
            GeocodingClient.getCoordinates(
                geocodingClient.getGeocodingResult(destinationList.get(0)));
        for (String waypoint : waypoints) {
          geocodingClient = geocodingClientFactory.getGeocodingClient(apiKey);
          GeocodingResult geocodingResult = geocodingClient.getGeocodingResult(waypoint);
          if (GeocodingClient.isPartialMatch(geocodingResult)) {
            waypointsAsPlaceTypes.add(GeocodingClient.getPlaceType(geocodingResult));
          } else {
            waypointsAsCoordinates.add(GeocodingClient.getCoordinates(geocodingResult));
          }
        }
      } catch (GeocodingException e) {
        throw new ServletException(e);
      }

      List<LatLng> originDestinationAndWaypointsAsCoordinates = waypointsAsCoordinates;
      originDestinationAndWaypointsAsCoordinates.add(originAsCoordinates);
      originDestinationAndWaypointsAsCoordinates.add(destinationAsCoordinates);

      List<List<String>> allSearchNearbyResults = new ArrayList<>();

      List<LatLng> allExactAddressCoordinates = ImmutableList.of();
      for (LatLng coordinate : originDestinationAndWaypointsAsCoordinates) {
        for (PlaceType query : waypointsAsPlaceTypes) {
          PlaceType placeType = query;
          RankBy rankBy = RankBy.DISTANCE;
          List<String> searchNearbyResults =
              placesClient.searchNearby(coordinate, placeType, rankBy);
          allSearchNearbyResults.add(searchNearbyResults);
        }
      }

      for (String waypoint : waypoints) {
        allSearchNearbyResults.add(ImmutableList.of(waypoint));
      }

      List<List<String>> allWaypointCombinations = new ArrayList<List<String>>();
      LocationsUtility.generateCombinations(
          allSearchNearbyResults, allWaypointCombinations, 0, new ArrayList<String>());

      long minTravelTime = 0;
      List<String> mostOptimalWaypointCombination = new ArrayList<String>();

      for (List<String> waypointCombination : allWaypointCombinations) {
        DirectionsResult directionsResult =
            directionsClient.getDirections(
                originList.get(0), destinationList.get(0), waypointCombination);
        long travelTime = DirectionsClient.getTotalTravelTime(directionsResult);
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
