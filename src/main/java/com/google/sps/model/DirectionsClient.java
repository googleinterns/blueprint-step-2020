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

import com.google.sps.exceptions.DirectionsException;
import java.util.List;

/**
 * Contract for sending GET requests to the Google Directions API. Implement getDirections to obtain
 * optimized route from the API.
 */
public interface DirectionsClient {
  /**
   * Gets the result of a GET request to the Google Directions API.
   *
   * @param destination A string representing the destination to get directions to.
   * @param origin A string representing the origin to get directions from.
   * @param waypoints A list of string consisting of waypoints to visit between the destination and
   *     the origin.
   * @return A string representing the result from a GET request to the Google Directions API.
   * @throws DirectionsException A custom exception is thrown to signal an error pertaining to the
   *     Directions API.
   */
  List<String> getDirections(String origin, String destination, List<String> waypoints)
      throws DirectionsException;
}
