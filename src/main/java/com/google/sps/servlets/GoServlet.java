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
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.RankBy;
import com.google.sps.exceptions.DirectionsException;
import com.google.sps.exceptions.GeocodingException;
import com.google.sps.exceptions.PlacesException;
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
import com.google.sps.utility.GeocodingResultUtility;
import com.google.sps.utility.JsonUtility;
import com.google.sps.utility.KeyProvider;
import com.google.sps.utility.LocationsUtility;
import com.google.sps.utility.TasksUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
      String apiKey) {
    this.directionsClientFactory = directionsClientFactory;
    this.placesClientFactory = placesClientFactory;
    this.tasksClientFactory = tasksClientFactory;
    this.geocodingClientFactory = geocodingClientFactory;
    this.apiKey = apiKey;
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
    // Get all tasks from user's tasks account
    TasksClient tasksClient = tasksClientFactory.getTasksClient(googleCredential);
    DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);

    // Initialize Tasks Response
    List<Task> tasks;

    String taskLists = request.getParameter("taskLists");
    if (taskLists == null) {
      tasks = TasksUtility.getAllTasksFromAllTaskLists(tasksClient);
    } else {
      Set<String> selectedTaskListIds = new HashSet<>(Arrays.asList(taskLists.split(",")));
      tasks = TasksUtility.getAllTasksFromSpecificTaskLists(tasksClient, selectedTaskListIds);
    }

    String origin = request.getParameter("origin");
    String destination = request.getParameter("destination");
    List<String> waypoints = LocationsUtility.getLocations("Location", tasks);

    try {
      List<String> mostOptimalWaypointCombination =
          optimizeSearchNearbyWaypoints(origin, destination, waypoints);
      DirectionsResult directionsResult =
          directionsClient.getDirections(origin, destination, mostOptimalWaypointCombination);
      List<String> optimizedRoute = DirectionsClient.parseDirectionsResult(directionsResult);
      JsonUtility.sendJson(response, optimizedRoute);
    } catch (DirectionsException | GeocodingException | PlacesException | IOException e) {
      throw new ServletException(e);
    }
  }

  /**
   * Separate waypoints into street addresses and non street addresses. Street addresses are
   * converted to coordinates and non street addresses are converted to place types. Scope of method
   * is public for testing purposes.
   *
   * @param waypoints A list of waypoints to filter into the two categories: street addresses and
   *     non street addresses.
   * @param streetAddressWaypoints A pointer to the result for street address waypoints to achieve
   *     the effect of returning multiple types at once.
   * @param streetAddressWaypointsAsCoordinates A pointer to the result for street address waypoints
   *     as coordinates to achieve the effect of returning multiple types at once.
   * @param nonStreetAddressWaypointsAsPlaceTypes A pointer to the result for non street address
   *     waypoints as place types to achieve the effect of returning multiple types at once.
   * @throws GeocodingException An exception thrown when an error occurs with the Geocoding API.
   */
  public void separateWaypoints(
      List<String> waypoints,
      List<String> streetAddressWaypoints,
      List<Optional<LatLng>> streetAddressWaypointsAsCoordinates,
      List<Optional<PlaceType>> nonStreetAddressWaypointsAsPlaceTypes)
      throws GeocodingException {
    for (String waypoint : waypoints) {
      GeocodingClient geocodingClient = geocodingClientFactory.getGeocodingClient(apiKey);
      List<GeocodingResult> geocodingResult = geocodingClient.getGeocodingResult(waypoint);
      if (GeocodingResultUtility.hasStreetAddress(geocodingResult)) {
        streetAddressWaypoints.add(waypoint);
        streetAddressWaypointsAsCoordinates.add(
            GeocodingResultUtility.getCoordinates(geocodingResult));
      } else {
        nonStreetAddressWaypointsAsPlaceTypes.add(
            GeocodingResultUtility.convertToPlaceType(waypoint));
      }
    }
  }

  /**
   * Search nearby every street address with known coordinates for a place type match. For example,
   * if we know exactly where our houses are and we are looking for a restaurant, we search for a
   * restaurant closest to your house and a restaurant closest to my house. In this case, the method
   * should return [[restaurantOne, restaurantTwo]]. Scope of method is public for testing purposes.
   *
   * @param nonStreetAddressWaypointsAsPlaceTypes A list of place types to call search for. (e.g.
   *     restaurant, supermarket, police station)
   * @param streetAddressesAsCoordinates A list of coordinates to look for the place types around.
   * @return A list of lists of place IDs where each list represents the search nearby result for
   *     every known coordinate.
   * @throws PlacesException An exception thrown when an error occurs with the Places API.
   */
  public List<List<String>> searchNearbyEveryKnownLocationForClosestPlaceTypeMatch(
      List<PlaceType> nonStreetAddressWaypointsAsPlaceTypes,
      List<LatLng> streetAddressesAsCoordinates)
      throws PlacesException {
    List<List<String>> allSearchNearbyResults = new ArrayList<>();
    for (PlaceType nonStreetAddressWaypoint : nonStreetAddressWaypointsAsPlaceTypes) {
      List<String> searchNearbyResults = new ArrayList<>();
      for (LatLng coordinate : streetAddressesAsCoordinates) {
        PlaceType placeType = nonStreetAddressWaypoint;
        RankBy rankBy = RankBy.DISTANCE;
        PlacesClient placesClient = placesClientFactory.getPlacesClient(apiKey);
        String nearestMatch = placesClient.searchNearby(coordinate, placeType, rankBy);
        if (nearestMatch != null) {
          searchNearbyResults.add("place_id:" + nearestMatch);
        }
      }
      allSearchNearbyResults.add(searchNearbyResults);
    }
    return allSearchNearbyResults;
  }

  /**
   * Chooses the combination of waypoints that results in the shortest travel time possible. Scope
   * of method is public for testing purposes.
   *
   * @param origin The starting point of travel.
   * @param destination The ending point of travel.
   * @param allWaypointCombinations A list of waypoint combinations to select between for the
   *     shortest travel time possible.
   * @return The most optimal combination of waypoint with the shortest travel time.
   * @throws DirectionsException An exception thrown when an error occurs with the Directions API.
   */
  public List<String> chooseWaypointCombinationWithShortestTravelTime(
      String origin, String destination, List<List<String>> allWaypointCombinations)
      throws DirectionsException {
    long minTravelTime = 0;
    List<String> mostOptimalWaypointCombination = new ArrayList<String>();
    for (List<String> waypointCombination : allWaypointCombinations) {
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      DirectionsResult directionsResult =
          directionsClient.getDirections(origin, destination, waypointCombination);
      long travelTime = DirectionsClient.getTotalTravelTime(directionsResult);
      if (minTravelTime == 0 || travelTime < minTravelTime) {
        minTravelTime = travelTime;
        mostOptimalWaypointCombination = waypointCombination;
      }
    }
    return mostOptimalWaypointCombination;
  }

  /**
   * Finds the most optimal route of travel between the origin and destination and a set of
   * waypoints which if the exact location is not known, an exact location is determined and chosen
   * based on the resulting travel time. Scope of method is public for testing purposes.
   *
   * @param origin The starting point of travel.
   * @param destination The ending point of travel.
   * @param waypoints The waypoints that should be visited while travelling from the origin to the
   *     destination.
   * @return The most optimal set of waypoints between origin and destination.
   * @throws GeocodingException An exception thrown when an error occurs with the Geocoding API.
   * @throws PlacesException An exception thrown when an error occurs with the Places API.
   * @throws DirectionsException An exception thrown when an error occurs with the Directions API.
   */
  public List<String> optimizeSearchNearbyWaypoints(
      String origin, String destination, List<String> waypoints)
      throws GeocodingException, PlacesException, DirectionsException {

    Optional<LatLng> originAsCoordinates =
        GeocodingResultUtility.getCoordinates(
            geocodingClientFactory.getGeocodingClient(apiKey).getGeocodingResult(origin));

    Optional<LatLng> destinationAsCoordinates =
        GeocodingResultUtility.getCoordinates(
            geocodingClientFactory.getGeocodingClient(apiKey).getGeocodingResult(destination));

    List<String> streetAddressWaypoints = new ArrayList<>();
    List<Optional<LatLng>> streetAddressWaypointsAsCoordinates = new ArrayList<>();
    List<Optional<PlaceType>> nonStreetAddressWaypointsAsPlaceTypes = new ArrayList<>();
    separateWaypoints(
        waypoints,
        streetAddressWaypoints,
        streetAddressWaypointsAsCoordinates,
        nonStreetAddressWaypointsAsPlaceTypes);

    // All street address coordinates including origin and destination are collected
    List<Optional<LatLng>> streetAddressesAsCoordinates = streetAddressWaypointsAsCoordinates;
    streetAddressesAsCoordinates.add(originAsCoordinates);
    streetAddressesAsCoordinates.add(destinationAsCoordinates);

    // Remove all null entries of street address coordinates and non street address waypoints
    List<LatLng> nonEmptyStreetAddressesAsCoordinates =
        streetAddressesAsCoordinates.stream()
            .filter(coordinates -> !coordinates.equals(Optional.empty()))
            .map(coordinates -> coordinates.get())
            .collect(Collectors.toList());
    List<PlaceType> nonEmptynonStreetAddressWaypointsAsPlaceTypes =
        nonStreetAddressWaypointsAsPlaceTypes.stream()
            .filter(waypoint -> !waypoint.equals(Optional.empty()))
            .map(waypoint -> waypoint.get())
            .collect(Collectors.toList());

    List<List<String>> allSearchNearbyResults =
        searchNearbyEveryKnownLocationForClosestPlaceTypeMatch(
            nonEmptynonStreetAddressWaypointsAsPlaceTypes, nonEmptyStreetAddressesAsCoordinates);

    List<List<String>> allWaypointCombinations =
        LocationsUtility.generateCombinations(allSearchNearbyResults);

    List<String> mostOptimalWaypointCombination =
        chooseWaypointCombinationWithShortestTravelTime(
            origin, destination, allWaypointCombinations);
    mostOptimalWaypointCombination.addAll(streetAddressWaypoints);

    return mostOptimalWaypointCombination;
  }
}
