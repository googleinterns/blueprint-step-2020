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
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.common.collect.ImmutableList;
import com.google.sps.exceptions.DirectionsException;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.DirectionsClient;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.model.DirectionsClientImpl;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.model.TasksClientImpl;
import com.google.sps.utility.JsonUtility;
import com.google.sps.utility.KeyProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves key information from optimizing between addresses. */
@WebServlet("/go")
public class GoServlet extends AuthenticatedHttpServlet {

  private final DirectionsClientFactory directionsClientFactory;
  private final TasksClientFactory tasksClientFactory;
  private final String apiKey;
  private final String origin;
  private final String destination;
  private final List<String> waypoints;

  /**
   * Construct servlet with default DirectionsClient.
   *
   * @throws IOException
   */
  public GoServlet() throws IOException {
    directionsClientFactory = new DirectionsClientImpl.Factory();
    tasksClientFactory = new TasksClientImpl.Factory();
    apiKey = (new KeyProvider()).getKey("apiKey");
    origin = "Waterloo, ON";
    destination = "Waterloo, ON";
    waypoints = ImmutableList.of("Montreal, QC", "Windsor, ON", "Kitchener, ON");
  }

  /**
   * Construct servlet with explicit implementation of DirectionsClient.
   *
   * @param factory A DirectionsClientFactory containing the implementation of
   *     DirectionsClientFactory.
   */
  public GoServlet(
      DirectionsClientFactory directionsClientFactory,
      TasksClientFactory tasksClientFactory,
      String fakeApiKey,
      String fakeOrigin,
      String fakeDestination,
      List<String> fakeWaypoints) {
    this.directionsClientFactory = directionsClientFactory;
    this.tasksClientFactory = tasksClientFactory;
    apiKey = fakeApiKey;
    origin = fakeOrigin;
    destination = fakeDestination;
    waypoints = fakeWaypoints;
  }

  private List<Task> getTasks(TasksClient tasksClient) throws IOException {
    List<TaskList> taskLists = tasksClient.listTaskLists();
    List<Task> tasks = new ArrayList<>();
    for (TaskList taskList : taskLists) {
      tasks.addAll(tasksClient.listTasks(taskList));
    }
    return tasks;
  }

  /**
   * Parses for locations in Tasks. Scope is public for testing purposes.
   *
   * @param prefix String which represents the prefix wrapped in square brackets to look for. (e.g.
   *     Location if looking for [Location: ])
   * @param tasks List of tasks to parse for locations from.
   * @return List of strings representing the locations.
   */
  public List<String> getLocations(String prefix, List<Task> tasks) {
    return tasks.stream()
        .map(task -> task.getNotes())
        .filter(Objects::nonNull)
        .map(notes -> getLocation(prefix, notes))
        .filter(place -> !place.equals("No " + prefix))
        .collect(Collectors.toList());
  }

  private String getLocation(String prefix, String taskNotes) {
    // taskNotes = ... [prefix: ... ] ...
    String regex = "\\[" + prefix + ": (.*?)\\]";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(taskNotes);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "No " + prefix;
  }

  /**
   * Returns the most optimal order of travel between addresses.
   *
   * @param request HTTP request from the client.
   * @param response HTTP response to the client.
   * @throws ServletException
   * @throws IOException
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws ServletException, IOException {
    assert googleCredential != null
        : "Null credentials (i.e. unauthenticated requests) should already be handled";
    // Get all tasks from user's tasks account TODO: Get relevant tasks using task titles
    TasksClient tasksClient = tasksClientFactory.getTasksClient(googleCredential);
    List<Task> tasks = getTasks(tasksClient);

    // Get descriptions of relevant tasks
    // Parse for locations from descriptions
    List<String> allGenericPlaces = getLocations("Search", tasks);
    List<String> allAddresses = getLocations("Address", tasks);

    // Split waypoints into exact addresses and generic locations by looking for the presence of ","
    // For every exact address, generic location is sent to Places to obtain the closest match

    // Create 'route' for every permutation send to Directions
    // Route with shortest travel time is kept at every iteration
    try {
      DirectionsClient directionsClient = directionsClientFactory.getDirectionsClient(apiKey);
      List<String> directions = directionsClient.getDirections(origin, destination, waypoints);
      JsonUtility.sendJson(response, directions);
    } catch (DirectionsException | IOException e) {
      throw new ServletException(e);
    }
  }
}
