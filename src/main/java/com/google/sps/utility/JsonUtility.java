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

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/** Utility class for JSON operations. */
public final class JsonUtility {

  /** Prevent instances of this class. */
  private JsonUtility() {}

  /**
   * Sends data to a HTTP Servlet Response. TODO: overload method to access JsonObject objects
   * (Issue #107)
   *
   * @param response HTTP Servlet Response that data is sent to.
   * @param object Contains the data that is to be sent. JsonObject instances behave unexpectedly.
   * @throws IOException
   */
  public static void sendJson(HttpServletResponse response, Object object) throws IOException {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }
}
