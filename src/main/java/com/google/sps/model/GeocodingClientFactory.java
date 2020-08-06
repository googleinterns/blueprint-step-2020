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

/**
 * Contract for creating a GeocodingClient with a given API key. Implement getGeocodingClient to
 * obtain an instance of GeocodingClient.
 */
public interface GeocodingClientFactory {
  /**
   * Gets a GeocodingClient which executes against the given API key.
   *
   * @param apiKey A string representing the API key to authenticate a Google Geocoding API call.
   * @return GeocodingClientImpl instance which executes against the given API key.
   */
  GeocodingClient getGeocodingClient(String apiKey);
}
