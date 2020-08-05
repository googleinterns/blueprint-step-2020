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

import com.google.maps.model.GeocodingResult;
import com.google.sps.exceptions.GeocodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Contract for sending GET requests to the Google Geocoding API. Implement getGeocodingResult to
 * obtain the corresponding GeocodingResult of an address.
 */
public interface GeocodingClient {
  /**
   * Sends a GET request to the Google Geocoding API to convert from address to GeocodingResult. The
   * Geocoding API could return multiple results in case of partial matches and in that case, the
   * first match is the best match hence it would be the result returned. No results would cause a
   * GeocodingException to be thrown.
   *
   * @param address A String representing the address to geocode.
   * @return A List of GeocodingResult returned from the Geocoding API.
   * @throws GeocodingException A custom exception is thrown to signal an error pertaining to the
   *     Geocoding API.
   */
  List<GeocodingResult> getGeocodingResult(String address) throws GeocodingException;
}
