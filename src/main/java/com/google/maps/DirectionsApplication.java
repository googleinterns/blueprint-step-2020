package com.google.maps;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.client.utils.URIBuilder;

public class DirectionsApplication implements DirectionsConsumer {
  private DirectionsService directionsService;

  public DirectionsApplication(DirectionsService service) {
    this.directionsService = service;
  }

  private static String buildWaypoints(List<String> waypoints) {
    StringBuilder waypointsBuilder = new StringBuilder("optimize:true");
    if (waypoints.isEmpty()) {
      return waypointsBuilder.toString();
    }
    for (String waypoint : waypoints) {
      waypointsBuilder.append("|" + waypoint);
    }
    return waypointsBuilder.toString();
  }

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
