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

import com.google.appengine.repackaged.com.google.gson.Gson;

/**
 * Utility functions for handling HTTP requests
 */
public class ServletUtility {
  private ServletUtility() {}

  /**
   * Transforms object to JSON.
   * For classes: Instance variable names -> keys, and field values -> values.
   * For lists: index -> key, value at index -> value
   * for primitives: value -> value (does not print a key)
   * @param o any valid java object
   * @return a String representation of a JSON object
   */
  public static String objectToJson(Object o) {
    Gson gson = new Gson();
    return gson.toJson(o);
  }
}
