package com.google.maps;

import java.util.List;

/** Contract for sending GET requests to the Google Directions API. */
public interface DirectionsClient {
  List<String> getDirections(String origin, String destination, String[] waypoints) throws Exception;
}
