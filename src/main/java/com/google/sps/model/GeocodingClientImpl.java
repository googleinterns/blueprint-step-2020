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

package com.google.sps.model;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.sps.exceptions.GeocodingException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Handles GET requests to the Google Geocoding API */
public class GeocodingClientImpl implements GeocodingClient {
  private GeocodingApiRequest geocodingService;

  private GeocodingClientImpl(GeocodingApiRequest service) {
    geocodingService = service;
  }

  /** Factory to create a GeocodingClientImpl instance with given API key */
  public static class Factory implements GeocodingClientFactory {
    @Override
    public GeocodingClient getGeocodingClient(String apiKey) {
      GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
      GeocodingApiRequest service = GeocodingApi.newRequest(context);
      return new GeocodingClientImpl(service);
    }
  }

  @Override
  public List<GeocodingResult> getGeocodingResult(String address) throws GeocodingException {
    try {
      GeocodingResult[] response = geocodingService.address(address).await();
      return Arrays.asList(response);
    } catch (ApiException | InterruptedException | IOException | IndexOutOfBoundsException e) {
      throw new GeocodingException("Failed to geocode address", e);
    }
  }
}
