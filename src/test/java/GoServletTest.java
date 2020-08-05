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

import com.google.common.collect.ImmutableList;
import com.google.maps.model.AddressType;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.RankBy;
import com.google.sps.model.DirectionsClient;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.model.GeocodingClient;
import com.google.sps.model.GeocodingClientFactory;
import com.google.sps.model.PlacesClient;
import com.google.sps.model.PlacesClientFactory;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.servlets.GoServlet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class GoServletTest extends AuthenticatedServletTestBase {

  private static DirectionsClientFactory directionsClientFactory;
  private static DirectionsClient directionsClient;
  private static PlacesClientFactory placesClientFactory;
  private static PlacesClient placesClient;
  private static TasksClientFactory tasksClientFactory;
  private static TasksClient tasksClient;
  private static GeocodingClientFactory geocodingClientFactory;
  private static GeocodingClient geocodingClient;
  private static GoServlet servlet;

  private static GeocodingResult streetAddressGeocodingResult = new GeocodingResult();
  private static Geometry streetAddressGeometry = new Geometry();
  private static LatLng streetAddressCoordinates = new LatLng(0, 0);
  private static GeocodingResult restaurantGeocodingResult = new GeocodingResult();

  private static Duration longerDuration = new Duration();
  private static DirectionsLeg longerLeg = new DirectionsLeg();
  private static DirectionsRoute longerRoute = new DirectionsRoute();
  private static DirectionsResult longerResult = new DirectionsResult();

  private static Duration shorterDuration = new Duration();
  private static DirectionsLeg shorterLeg = new DirectionsLeg();
  private static DirectionsRoute shorterRoute = new DirectionsRoute();
  private static DirectionsResult shorterResult = new DirectionsResult();

  private static final String API_KEY = "fake api key";

  private static final String ORIGIN = "A";
  private static final String DESTINATION = "B";

  private static final String RESTAURANT_ONE = "restaurant near coordinate one";
  private static final String RESTAURANT_TWO = "restaurant near coordinate two";
  private static final String BANK_ONE = "bank near coordinate one";
  private static final String BANK_TWO = "bank near coordinate two";

  private static final long FIVE_SECONDS = 5;
  private static final long TEN_SECONDS = 10;

  private static final List<String> STREET_ADDRESS_AND_RESTAURANT_WAYPOINTS =
      ImmutableList.of("street address", "restaurant");

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    directionsClientFactory = Mockito.mock(DirectionsClientFactory.class);
    directionsClient = Mockito.mock(DirectionsClient.class);
    placesClientFactory = Mockito.mock(PlacesClientFactory.class);
    placesClient = Mockito.mock(PlacesClient.class);
    tasksClientFactory = Mockito.mock(TasksClientFactory.class);
    tasksClient = Mockito.mock(TasksClient.class);
    geocodingClientFactory = Mockito.mock(GeocodingClientFactory.class);
    geocodingClient = Mockito.mock(GeocodingClient.class);

    streetAddressGeocodingResult.geometry = streetAddressGeometry;
    streetAddressGeocodingResult.geometry.location = streetAddressCoordinates;
    streetAddressGeocodingResult.types = new AddressType[] {AddressType.STREET_ADDRESS};
    restaurantGeocodingResult.types = new AddressType[] {AddressType.RESTAURANT};

    longerDuration.inSeconds = TEN_SECONDS;
    longerLeg.duration = longerDuration;
    longerRoute.legs = new DirectionsLeg[] {longerLeg};
    longerResult.routes = new DirectionsRoute[] {longerRoute};

    shorterDuration.inSeconds = FIVE_SECONDS;
    shorterLeg.duration = shorterDuration;
    shorterRoute.legs = new DirectionsLeg[] {shorterLeg};
    shorterResult.routes = new DirectionsRoute[] {shorterRoute};

    servlet =
        new GoServlet(
            directionsClientFactory,
            placesClientFactory,
            tasksClientFactory,
            geocodingClientFactory,
            API_KEY);

    Mockito.when(directionsClientFactory.getDirectionsClient(API_KEY)).thenReturn(directionsClient);
    Mockito.when(placesClientFactory.getPlacesClient(API_KEY)).thenReturn(placesClient);
    Mockito.when(tasksClientFactory.getTasksClient(Mockito.any())).thenReturn(tasksClient);
    Mockito.when(geocodingClientFactory.getGeocodingClient(API_KEY)).thenReturn(geocodingClient);
  }

  @Test
  public void separateWaypoints() throws Exception {
    // Street address should be converted into coordinates, restaurant should be converted into
    // place type
    List<String> streetAddressWaypoints = new ArrayList<String>();
    List<Optional<LatLng>> streetAddressWaypointsAsCoordinates = new ArrayList<Optional<LatLng>>();
    List<Optional<PlaceType>> nonStreetAddressWaypointsAsPlaceTypes =
        new ArrayList<Optional<PlaceType>>();

    Mockito.when(geocodingClient.getGeocodingResult("street address"))
        .thenReturn(ImmutableList.of(streetAddressGeocodingResult));
    Mockito.when(geocodingClient.getGeocodingResult("restaurant"))
        .thenReturn(ImmutableList.of(restaurantGeocodingResult));

    servlet.separateWaypoints(
        STREET_ADDRESS_AND_RESTAURANT_WAYPOINTS,
        streetAddressWaypoints,
        streetAddressWaypointsAsCoordinates,
        nonStreetAddressWaypointsAsPlaceTypes);

    Assert.assertEquals(ImmutableList.of("street address"), streetAddressWaypoints);
    Assert.assertEquals(
        ImmutableList.of(Optional.ofNullable(streetAddressCoordinates)),
        streetAddressWaypointsAsCoordinates);
    Assert.assertEquals(
        ImmutableList.of(Optional.ofNullable(PlaceType.RESTAURANT)),
        nonStreetAddressWaypointsAsPlaceTypes);
  }

  @Test
  public void searchNearbyEveryKnownLocationForClosestPlaceTypeMatch() throws Exception {
    // Search for one restaurant and one bank nearest to coordinate one, one restaurant and one bank
    // nearest to coordinate two
    List<PlaceType> nonStreetAddressWaypointsAsPlaceTypes =
        ImmutableList.of(PlaceType.RESTAURANT, PlaceType.BANK);
    LatLng coordinateOne = new LatLng(0, 0);
    LatLng coordinateTwo = new LatLng(0, 10);
    List<LatLng> streetAddressesAsCoordinates = ImmutableList.of(coordinateOne, coordinateTwo);

    Mockito.when(placesClient.searchNearby(coordinateOne, PlaceType.RESTAURANT, RankBy.DISTANCE))
        .thenReturn(RESTAURANT_ONE);
    Mockito.when(placesClient.searchNearby(coordinateTwo, PlaceType.RESTAURANT, RankBy.DISTANCE))
        .thenReturn(RESTAURANT_TWO);
    Mockito.when(placesClient.searchNearby(coordinateOne, PlaceType.BANK, RankBy.DISTANCE))
        .thenReturn(BANK_ONE);
    Mockito.when(placesClient.searchNearby(coordinateTwo, PlaceType.BANK, RankBy.DISTANCE))
        .thenReturn(BANK_TWO);

    List<List<String>> actual =
        servlet.searchNearbyEveryKnownLocationForClosestPlaceTypeMatch(
            nonStreetAddressWaypointsAsPlaceTypes, streetAddressesAsCoordinates);

    Assert.assertEquals(
        ImmutableList.of(
            ImmutableList.of("place_id:" + RESTAURANT_ONE, "place_id:" + RESTAURANT_TWO),
            ImmutableList.of("place_id:" + BANK_ONE, "place_id:" + BANK_TWO)),
        actual);
  }

  @Test
  public void chooseWaypointCombinationWithShortestTravelTime() throws Exception {
    // Find the waypoints which result in the shorter travel time
    List<String> longerTravelTimeWaypoints = ImmutableList.of("long", "long");
    List<String> shorterTravelTimeWaypoints = ImmutableList.of("short", "medium");

    Mockito.when(directionsClient.getDirections(ORIGIN, DESTINATION, longerTravelTimeWaypoints))
        .thenReturn(longerResult);
    Mockito.when(directionsClient.getDirections(ORIGIN, DESTINATION, shorterTravelTimeWaypoints))
        .thenReturn(shorterResult);

    List<String> actual =
        servlet.chooseWaypointCombinationWithShortestTravelTime(
            ORIGIN,
            DESTINATION,
            ImmutableList.of(longerTravelTimeWaypoints, shorterTravelTimeWaypoints));

    Assert.assertEquals(shorterTravelTimeWaypoints, actual);
  }

  @Test
  public void optimizeSearchNearbyWaypoints() throws Exception {
    // Optimize waypoints for one street address and one restaurant, street address is returned as
    // it is, the nearest restaurant is found and returned
    GeocodingResult originResult = new GeocodingResult();
    Geometry originGeometry = new Geometry();
    LatLng originCoordinates = new LatLng(-10, 10);
    originResult.geometry = originGeometry;
    originGeometry.location = originCoordinates;
    originResult.types = new AddressType[] {AddressType.STREET_ADDRESS};

    GeocodingResult destinationResult = new GeocodingResult();
    Geometry destinationGeometry = new Geometry();
    LatLng destinationCoordinates = new LatLng(10, -10);
    destinationResult.geometry = destinationGeometry;
    destinationGeometry.location = destinationCoordinates;
    destinationResult.types = new AddressType[] {AddressType.STREET_ADDRESS};

    Mockito.when(geocodingClient.getGeocodingResult(ORIGIN))
        .thenReturn(ImmutableList.of(originResult));
    Mockito.when(geocodingClient.getGeocodingResult(DESTINATION))
        .thenReturn(ImmutableList.of(destinationResult));

    Mockito.when(geocodingClient.getGeocodingResult("street address"))
        .thenReturn(ImmutableList.of(streetAddressGeocodingResult));
    Mockito.when(geocodingClient.getGeocodingResult("restaurant"))
        .thenReturn(ImmutableList.of(restaurantGeocodingResult));

    Mockito.when(
            placesClient.searchNearby(originCoordinates, PlaceType.RESTAURANT, RankBy.DISTANCE))
        .thenReturn(RESTAURANT_ONE);
    Mockito.when(
            placesClient.searchNearby(
                destinationCoordinates, PlaceType.RESTAURANT, RankBy.DISTANCE))
        .thenReturn(RESTAURANT_TWO);

    Mockito.when(
            directionsClient.getDirections(
                ORIGIN, DESTINATION, ImmutableList.of("place_id:" + RESTAURANT_ONE)))
        .thenReturn(longerResult);
    Mockito.when(
            directionsClient.getDirections(
                ORIGIN, DESTINATION, ImmutableList.of("place_id:" + RESTAURANT_TWO)))
        .thenReturn(shorterResult);

    List<String> actual =
        servlet.optimizeSearchNearbyWaypoints(
            ORIGIN, DESTINATION, STREET_ADDRESS_AND_RESTAURANT_WAYPOINTS);

    Assert.assertEquals(ImmutableList.of("place_id:" + RESTAURANT_TWO, "street address"), actual);
  }
}
