package com.google.maps;

import java.util.List;

public interface DirectionsConsumer {
  String processDirections(
      String destination, String origin, List<String> waypoints, String apiKey);
}
