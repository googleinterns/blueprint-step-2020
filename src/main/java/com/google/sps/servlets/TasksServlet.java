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

package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;
import com.google.sps.utility.AuthenticationUtility;
import com.google.sps.utility.TasksUtility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

import java.lang.reflect.Type;


/**
 * Serves selected information from the User's Tasks Account. TODO: Create Servlet Utility to handle
 * common functions (Issue #26)
 */
@WebServlet("/tasks")
public class TasksServlet extends HttpServlet {

  String get(String name) throws IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream stream = loader.getResourceAsStream("API_KEYS.json");
    if (stream == null) {
      throw new FileNotFoundException("make sure your local keys are stored under src/main/resources/API_KEYS.json");
    }
    String rawJson = IOUtils.toString(stream, StandardCharsets.UTF_8);
    stream.close();
    Gson gson = new Gson();
    Type mapType = new TypeToken<Map<String, String>>(){}.getType();
    Map<String, String> keys = gson.fromJson(rawJson, mapType);
    return keys.get(name);
  }


  /**
   * Returns taskNames from the user's Tasks account
   *
   * @param request Http request from client. Should contain idToken and accessToken
   * @param response 403 if user is not authenticated, list of taskNames otherwise
   * @throws IOException if an issue arises while processing the request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // get Google credential object. Ensure it is valid - otherwise return an error to client
    Credential googleCredential = AuthenticationUtility.getGoogleCredential(request);
    if (googleCredential == null) {
      response.sendError(403, AuthenticationUtility.ERROR_403);
      return;
    }



    List<String> tasks = new ArrayList<>();
    tasks.add(get("example1"));

    // Convert tasks to JSON and print to response
    Gson gson = new Gson();
    String tasksJson = gson.toJson(tasks);

    response.setContentType("application/json");
    response.getWriter().println(tasksJson);
  }
}
