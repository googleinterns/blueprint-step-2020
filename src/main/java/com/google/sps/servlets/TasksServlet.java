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

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.utility.TasksUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves selected information from the User's Tasks Account. TODO: Create Servlet Utility to handle
 * common functions (Issue #26)
 */
@WebServlet("/tasks")
public class TasksServlet extends AuthenticatedHttpServlet {
  /**
   * Returns taskNames from the user's Tasks account
   *
   * @param request Http request from client. Should contain idToken and accessToken
   * @param response 403 if user is not authenticated, list of taskNames otherwise
   * @throws IOException if an issue arises while processing the request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Load and verify google credential
    super.doGet(request, response);

    if (googleCredential != null) {
      // Get tasks from Google Tasks
      Tasks tasksService = TasksUtility.getTasksService(googleCredential);
      List<TaskList> taskLists = TasksUtility.listTaskLists(tasksService);
      List<Task> tasks = new ArrayList<>();
      for (TaskList taskList : taskLists) {
        tasks.addAll(TasksUtility.listTasks(tasksService, taskList));
      }

      // Convert tasks to JSON and print to response
      Gson gson = new Gson();
      String tasksJson = gson.toJson(tasks);

      response.setContentType("application/json");
      response.getWriter().println(tasksJson);
    }
  }
}
