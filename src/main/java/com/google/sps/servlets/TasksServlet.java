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
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.model.TasksClientImpl;
import com.google.sps.model.TasksResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves selected information from the User's Tasks Account. TODO: Implement Post (Issue #53) */
@WebServlet("/tasks")
public class TasksServlet extends AuthenticatedHttpServlet {
  private final TasksClientFactory tasksClientFactory;

  /** Create servlet with default TasksClient and Authentication Verifier implementations */
  public TasksServlet() {
    tasksClientFactory = new TasksClientImpl.Factory();
  }

  /**
   * Create servlet with explicit implementations of TasksClient and AuthenticationVerifier
   *
   * @param authenticationVerifier implementation of AuthenticationVerifier
   * @param tasksClientFactory implementation of TasksClientFactory
   */
  public TasksServlet(
      AuthenticationVerifier authenticationVerifier, TasksClientFactory tasksClientFactory) {
    super(authenticationVerifier);
    this.tasksClientFactory = tasksClientFactory;
  }

  /**
   * Get the names of the tasks in all of the user's tasklists
   *
   * @param tasksClient either a mock TaskClient or a taskClient with a valid credential
   * @return List of tasks from user's account
   * @throws IOException if an issue occurs with the tasksService
   */
  private List<Task> getTasks(TasksClient tasksClient) throws IOException {
    List<TaskList> taskLists = tasksClient.listTaskLists();
    List<Task> tasks = new ArrayList<>();
    for (TaskList taskList : taskLists) {
      tasks.addAll(tasksClient.listTasks(taskList));
    }
    return tasks;
  }

  private List<String> getTaskListTitles(TasksClient tasksClient) throws IOException {
    List<TaskList> taskLists = tasksClient.listTaskLists();
    return taskLists.stream().map(taskList -> taskList.getTitle()).collect(Collectors.toList());
  }

  private int getTasksToComplete(TasksClient tasksClient, TaskList taskList) throws IOException {
    List<Task> tasks = getTasks(tasksClient);
    List<Task> tasksCompletedToday = tasks
    .stream()
    .filter(task -> task.getCompleted() == null)
    .collect(Collectors.toList());
    return tasksCompletedToday.size();
  }

  private int getTasksDueToday(TasksClient tasksClient, TaskList taskList) throws IOException {
    List<Task> tasks = getTasks(tasksClient);
    String today = LocalDate.now().toString();
    List<Task> tasksDueToday = tasks
    .stream()
    .filter(task -> task.getDue() != null && task.getDue().contains(today))
    .collect(Collectors.toList());
    return tasksDueToday.size();
  }

  private int getTasksCompletedToday(TasksClient tasksClient, TaskList taskList) throws IOException {
    // this is not working
    List<Task> tasks = getTasks(tasksClient);
    List<Task> tasksCompletedToday = tasks
    .stream()
    .filter(task -> task.getStatus() == "completed")
    .collect(Collectors.toList());
    return tasksCompletedToday.size();
  }

  private int getTasksOverdue(TasksClient tasksClient, TaskList taskList) throws IOException {
    // to consider: time is not taken into account in the Tasks API
    List<Task> tasks = getTasks(tasksClient);
    long currentTimeMillis = System.currentTimeMillis();
    List<Task> tasksOverdue = new ArrayList();
    for (Task task : tasks) {
      String dueDate = task.getDue();
      if (dueDate != null) {
        DateTime dateTime = DateTime.parseRfc3339(dueDate);
        long dateTimeMillis = dateTime.getValue();
        if (dateTimeMillis < currentTimeMillis) {
          tasksOverdue.add(task);
        }
      }
    }
    return tasksOverdue.size();
  }

    /**
   * Returns Tasks from the user's Tasks account
   *
   * @param request Http request from client. Should contain idToken and accessToken
   * @param response 403 if user is not authenticated, list of Tasks otherwise
   * @param googleCredential a valid google credential object (already verified)
   * @throws IOException if an issue arises while processing the request
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    assert googleCredential != null
        : "Null credentials (i.e. unauthenticated requests) should already be handled";

    // Get tasks from Google Tasks
    TasksClient tasksClient = tasksClientFactory.getTasksClient(googleCredential);
    // List<Task> tasks = getTasks(tasksClient);
    List<TaskList> taskLists = tasksClient.listTaskLists();
    TaskList taskList = taskLists.get(0);

    // Initialize Tasks Response
    List<String> taskListTitles = getTaskListTitles(tasksClient);
    int tasksToComplete = getTasksToComplete(tasksClient, taskList);
    int tasksDueToday = getTasksDueToday(tasksClient, taskList);
    int tasksCompletedToday = getTasksCompletedToday(tasksClient, taskList);
    int tasksOverdue = getTasksOverdue(tasksClient, taskList);
    TasksResponse tasksResponse = new TasksResponse(taskListTitles, tasksToComplete, tasksDueToday, tasksCompletedToday, tasksOverdue);

    System.out.println("tasksToComplete" + tasksToComplete);
    System.out.println("tasksDueToday " + tasksDueToday);
    System.out.println("tasksCompletedToday " + tasksCompletedToday);
    System.out.println("tasksOverdue " + tasksOverdue);

    // Convert tasks to JSON and print to response
    Gson gson = new Gson();
    String tasksJson = gson.toJson(tasksResponse);

    response.setContentType("application/json");
    response.getWriter().println(tasksJson);
  }
}
