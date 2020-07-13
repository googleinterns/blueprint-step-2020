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

package com.google.maps;

import java.util.List;

/** Consumer interface to declare contract for Directions consumer classes. */
public interface DirectionsConsumer {
  /**
   * Gets the result of a call to the Directions API.
   *
   * @param destination A string representing the destination to get directions to.
   * @param origin A string representing the origin to get directions from.
   * @param waypoints A list of string consisting of waypoints to visit between the destination and
   *     the origin.
   * @param apiKey A string representing the API key to authenticate a Google Directions API call.
   * @return A string representing the result from a call to the Google Directions API.
   */
  String processDirections(
      String destination, String origin, List<String> waypoints, String apiKey);
}
