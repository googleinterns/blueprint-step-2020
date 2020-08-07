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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/** Serves key information from optimizing between addresses. */
@WebServlet("/go")
public class GoServlet extends AuthenticatedHttpServlet {

  private final DirectionsClientFactory directionsClientFactory;
  private final PlacesClientFactory placesClientFactory;
  private final TasksClientFactory tasksClientFactory;
  private final GeocodingClientFactory geocodingClientFactory;
  private final String apiKey;

  public class GoResponse {
    List<String> streetAddressWaypoints;
    List<Optional<LatLng>> streetAddressWaypointsAsCoordinates;
    List<Optional<PlaceType>> nonStreetAddressWaypointsAsPlaceTypes;

    GoResponse(
        List<String> streetAddressWaypoints,
        List<Optional<LatLng>> streetAddressWaypointsAsCoordinates,
        List<Optional<PlaceType>> nonStreetAddressWaypointsAsPlaceTypes) {
      this.streetAddressWaypoints = streetAddressWaypoints;
      this.streetAddressWaypointsAsCoordinates = streetAddressWaypointsAsCoordinates;
      this.nonStreetAddressWaypointsAsPlaceTypes = nonStreetAddressWaypointsAsPlaceTypes;
    }

    public List<String> getStreetAddressWaypoints() {
      return streetAddressWaypoints;
    }

    public List<Optional<LatLng>> getStreetAddressWaypointsAsCoordinates() {
      return streetAddressWaypointsAsCoordinates;
    }

    public List<Optional<PlaceType>> getNonStreetAddressWaypointsAsPlaceTypes() {
      return nonStreetAddressWaypointsAsPlaceTypes;
    }
  }

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
   * @param taskLists A String of comma separated task list IDs from the query string.
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
    // Get all tasks from user's tasks account TODO: Allow user to pick specific task lists PR #149
    TasksClient tasksClient = tasksClientFactory.getTasksClient(googleCredential);
    DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);

    // Initialize Tasks Response
    List<Task> tasks;

    String taskLists = request.getParameter("taskLists");
    if (StringUtils.isEmpty(taskLists)) {
      tasks = TasksUtility.getAllTasksFromAllTaskLists(tasksClient);
    } else {
      Set<String> selectedTaskListIds = new HashSet<>(Arrays.asList(taskLists.split(",")));
      tasks = TasksUtility.getAllTasksFromSpecificTaskLists(tasksClient, selectedTaskListIds);
    }

    String origin = request.getParameter("origin");
    String destination = request.getParameter("destination");
    List<String> waypoints = LocationsUtility.getLocations("Location", tasks);

    try {
      List<String> optimalWaypointCombination =
          optimizeSearchNearbyWaypoints(origin, destination, waypoints);
      DirectionsResult directionsResult =
          directionsClient.getDirections(origin, destination, optimalWaypointCombination);
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
   * @throws GeocodingException An exception thrown when an error occurs with the Geocoding API.
   */
  public GoResponse separateWaypoints(List<String> waypoints) throws GeocodingException {
    List<String> streetAddressWaypoints = new ArrayList<>();
    List<Optional<LatLng>> streetAddressWaypointsAsCoordinates = new ArrayList<>();
    List<Optional<PlaceType>> nonStreetAddressWaypointsAsPlaceTypes = new ArrayList<>();
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
    return new GoResponse(
        streetAddressWaypoints,
        streetAddressWaypointsAsCoordinates,
        nonStreetAddressWaypointsAsPlaceTypes);
  }

  /**
   * Search nearby every street address with known coordinates for a place type match. For example,
   * if we know exactly where our houses are and we are looking for a restaurant, we search for a
   * restaurant closest to your house and a restaurant closest to my house. In this case, the method
   * should return [[restaurantOne, restaurantTwo]]. Scope of method is public for testing purposes.
   *
   * @param placeTypeWaypoints A list of place types to call search for. (e.g. restaurant,
   *     supermarket, police station)
   * @param streetAddressWaypoints A list of coordinates to look for place type matches around.
   * @return A list of lists of place IDs where each list represents the search nearby result for
   *     every known coordinate.
   * @throws PlacesException An exception thrown when an error occurs with the Places API.
   */
  public List<List<String>> searchForPlacesNearLocations(
      List<PlaceType> placeTypeWaypoints, List<LatLng> streetAddressWaypoints)
      throws PlacesException {
    List<List<String>> allSearchNearbyResults = new ArrayList<>();
    for (PlaceType placeTypeWaypoint : placeTypeWaypoints) {
      List<String> searchNearbyResults = new ArrayList<>();
      for (LatLng streetAddressWaypoint : streetAddressWaypoints) {
        PlacesClient placesClient = placesClientFactory.getPlacesClient(apiKey);
        String nearestMatch =
            placesClient.searchNearby(streetAddressWaypoint, placeTypeWaypoint, RankBy.DISTANCE);
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
   * @param streetAddressWaypoints A list of street address waypoints to be included in every
   *     waypoint combination.
   * @return The most optimal combination of waypoint with the shortest travel time.
   * @throws DirectionsException An exception thrown when an error occurs with the Directions API.
   */
  public List<String> chooseWaypointCombinationWithShortestTravelTime(
      String origin,
      String destination,
      List<List<String>> allWaypointCombinations,
      List<String> streetAddressWaypoints)
      throws DirectionsException {
    OptionalLong minTravelTime = OptionalLong.empty();
    List<String> optimalWaypointCombination = new ArrayList<String>();
    for (List<String> waypointCombination : allWaypointCombinations) {
      List<String> waypoints =
          Stream.of(waypointCombination, streetAddressWaypoints)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      DirectionsResult directionsResult =
          directionsClient.getDirections(origin, destination, waypoints);
      long travelTime = DirectionsClient.getTotalTravelTime(directionsResult);
      if (!minTravelTime.isPresent() || travelTime < minTravelTime.getAsLong()) {
        minTravelTime = OptionalLong.of(travelTime);
        optimalWaypointCombination = waypoints;
      }
    }
    return optimalWaypointCombination;
  }

  private <T> List<T> filterNonNull(List<Optional<T>> objects) {
    return objects.stream()
        .flatMap(object -> object.isPresent() ? Stream.of(object.get()) : Stream.empty())
        .collect(Collectors.toList());
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

    if (!originAsCoordinates.isPresent() || !destinationAsCoordinates.isPresent()) {
      throw new GeocodingException("Origin or destination is invalid");
    }

    GoResponse separatedWaypoints = separateWaypoints(waypoints);

    // All street address coordinates including origin and destination are collected
    List<Optional<LatLng>> streetAddressesAsCoordinates = new ArrayList<>();
    streetAddressesAsCoordinates.addAll(separatedWaypoints.streetAddressWaypointsAsCoordinates);
    streetAddressesAsCoordinates.add(originAsCoordinates);
    streetAddressesAsCoordinates.add(destinationAsCoordinates);

    // Remove all optional empty entries of street address coordinates and non street address
    // waypoints
    List<LatLng> nonEmptyStreetAddressesAsCoordinates = filterNonNull(streetAddressesAsCoordinates);
    List<PlaceType> nonEmptyNonStreetAddressWaypointsAsPlaceTypes =
        filterNonNull(separatedWaypoints.nonStreetAddressWaypointsAsPlaceTypes);

    List<List<String>> allSearchNearbyResults =
        searchForPlacesNearLocations(
            nonEmptyNonStreetAddressWaypointsAsPlaceTypes, nonEmptyStreetAddressesAsCoordinates);

    List<List<String>> allWaypointCombinations =
        LocationsUtility.generateCombinations(allSearchNearbyResults);

    List<String> optimalWaypointCombination =
        chooseWaypointCombinationWithShortestTravelTime(
            origin,
            destination,
            allWaypointCombinations,
            separatedWaypoints.streetAddressWaypoints);

    return optimalWaypointCombination;
  }
}
