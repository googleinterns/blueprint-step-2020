package com.google.maps;

public class DirectionsClientImpl implements DirectionsClient {
  // private Directions directionsService;

  public static class Factory implements DirectionsClientFactory {
    public DirectionsClient create() {
      // instantiate some client library service
      return new DirectionsClientImpl();
    }
  }

  void DirectionsClient(/* Directions service */) {
    //directionsService = service;
  }

  // private methods to build uri

  public String getDirections(String uri) {
    // do the http request
    return "result of http request";
  }
}
