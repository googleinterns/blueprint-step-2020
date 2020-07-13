package com.google.maps;

import java.io.IOException;
import org.apache.http.client.fluent.Request;

public class DirectionsServiceImpl implements DirectionsService {
  @Override
  public String getDirections(String uri) {
    try {
      return Request.Get(uri).execute().returnContent().toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
