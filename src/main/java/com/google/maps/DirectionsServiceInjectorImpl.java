package com.google.maps;

public class DirectionsServiceInjectorImpl implements DirectionsServiceInjector {
  @Override
  public DirectionsConsumer getConsumer() {
    return new DirectionsApplication(new DirectionsServiceImpl());
  }
}
