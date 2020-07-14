package com.google.maps;

/** Contract for creating a DirectionsClient with a given API key. */
public interface DirectionsClientFactory {
  DirectionsClient getDirectionsClient(String apiKey);
}
