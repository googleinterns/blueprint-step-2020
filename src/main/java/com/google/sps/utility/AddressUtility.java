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

package com.google.sps.utility;

import java.util.ArrayList;
import java.util.List;

/** Utility class to obtain addresses from user's tasks */
public final class AddressUtility {

  // TODO: Use KeyProvider here (Issue #77)
  public static String getApiKey() {
    return "";
  }

  // TODO: Implement to get origin from addresses in Tasks (Issue #78)
  public static String getOrigin() {
    return "";
  }

  // TODO: Implement to get destination from addresses in Tasks (Issue #78)
  public static String getDestination() {
    return "";
  }

  // TODO: Implement to get waypoints from addresses in Tasks (Issue #78)
  public static List<String> getWaypoints() {
    return new ArrayList<>();
  }
}
