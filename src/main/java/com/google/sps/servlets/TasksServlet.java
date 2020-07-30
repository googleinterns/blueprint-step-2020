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
import com.google.gson.Gson;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.model.TasksClientImpl;
import com.google.sps.model.TasksResponse;
import com.google.sps.utility.JsonUtility;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves selected information from the User's Tasks Account. */
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
    List<TaskList> allTaskLists = tasksClient.listTaskLists();

    // Initialize Tasks Response
    List<String> allTaskListTitles = getTaskListTitles(allTaskLists);
    List<Task> tasks;

    String taskLists = request.getParameter("taskLists");
    if (taskLists == null) {
      tasks = getTasks(tasksClient, allTaskListTitles);
    } else {
      List<String> selectedTaskListTitles = Arrays.asList(taskLists.split(","));
      tasks = getTasks(tasksClient, selectedTaskListTitles);
    }

    List<String> taskListTitles = allTaskListTitles;
    long tasksToCompleteCount = countTasksToComplete(tasks);
    long tasksDueTodayCount = countTasksDueToday(tasks);
    long tasksCompletedTodayCount = countTasksCompletedToday(tasks);
    long tasksOverdueCount = countTasksOverdue(tasks);
    TasksResponse tasksResponse =
        TasksResponse.builder()
            .taskListTitles(taskListTitles)
            .tasksToCompleteCount(tasksToCompleteCount)
            .tasksDueTodayCount(tasksDueTodayCount)
            .tasksCompletedTodayCount(tasksCompletedTodayCount)
            .tasksOverdueCount(tasksOverdueCount)
            .build();

    // Convert tasks to JSON and print to response
    JsonUtility.sendJson(response, tasksResponse);
  }

  /**
   * Add a new task object to a tasklist
   *
   * @param request HTTP request from client. Must contain a taskListId. Body must contain a task
   *     entity (https://developers.google.com/tasks/v1/reference/tasks)
   * @param response Http response to be sent to client
   * @param googleCredential valid, verified google credential object
   * @throws IOException if an issue occurs with the tasksService
   */
  @Override
  public void doPost(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    assert googleCredential != null
        : "Null credentials (i.e. unauthenticated requests) should already be handled";
    TasksClient tasksClient = tasksClientFactory.getTasksClient(googleCredential);

    String taskListId = request.getParameter("taskListId");

    if (taskListId == null || taskListId.equals("")) {
      response.sendError(400, "taskListId must be present in request");
      return;
    }

    Gson gson = new Gson();
    Task taskToPost = gson.fromJson(request.getReader(), Task.class);

    // Check if passed task is present and valid
    if (taskToPost == null || taskToPost.isEmpty()) {
      response.sendError(400, "Task body must be non-empty");
      return;
    } else if (!(taskToPost.containsKey("title")
        || taskToPost.containsKey("notes")
        || taskToPost.containsKey("due"))) {
      response.sendError(400, "Task invalid. Task must contain at least one of: title, notes, due");
      return;
    }

    try {
      JsonUtility.sendJson(response, tasksClient.postTask(taskListId, taskToPost));
    } catch (IOException e) {
      throw new IOException(
          "There was an issue posting the task. Check the taskListId and try again", e);
    }
  }

  private List<String> getTaskListTitles(List<TaskList> taskLists) throws IOException {
    return taskLists.stream().map(taskList -> taskList.getTitle()).collect(Collectors.toList());
  }

  private long countTasksToComplete(List<Task> tasks) {
    // getHidden is defined for incomplete tasks
    return tasks.stream().filter(task -> task.getHidden() == null).count();
  }

  private long countTasksDueToday(List<Task> tasks) {
    String today = LocalDate.now().toString();
    return tasks.stream()
        .filter(task -> task.getDue() != null && task.getDue().contains(today))
        .count();
  }

  private long countTasksCompletedToday(List<Task> tasks) {
    ZoneId zoneId = ZoneId.systemDefault();
    Instant startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant();
    Instant endOfDay = LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toInstant();
    // getHidden is defined for completed tasks
    return tasks.stream()
        .filter(task -> task.getHidden() != null)
        .filter(
            task -> {
              Instant completionDateTime = ZonedDateTime.parse(task.getUpdated()).toInstant();
              return completionDateTime.isAfter(startOfDay)
                  && completionDateTime.isBefore(endOfDay);
            })
        .count();
  }

  private long countTasksOverdue(List<Task> tasks) {
    ZoneId zoneId = ZoneId.systemDefault();
    String zoneOffset = zoneId.getRules().getOffset(Instant.now()).toString();
    Instant endOfDay = LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toInstant();
    return tasks.stream()
        .filter(task -> task.getDue() != null)
        .filter(
            task -> {
              DateTime dateTime = DateTime.parseRfc3339(task.getDue().replace("Z", zoneOffset));
              long millis = dateTime.getValue();
              Instant dueDate = new Date(millis).toInstant().plus(Period.ofDays(1));
              return dueDate.isBefore(endOfDay);
            })
        .count();
  }

  /**
   * Get the tasks in the user's task lists with the given task list titles
   *
   * @param tasksClient Either a mock TaskClient or a taskClient with a valid credential
   * @param taskListTitles List of task list titles which tasks should be obtained from
   * @return List of tasks from specified task lists in user's account
   * @throws IOException if an issue occurs with the tasksService
   */
  private List<Task> getTasks(TasksClient tasksClient, List<String> taskListTitles)
      throws IOException {
    List<TaskList> taskLists = tasksClient.listTaskLists();
    List<Task> tasks = new ArrayList<>();
    for (TaskList taskList : taskLists) {
      if (taskListTitles.contains(taskList.getTitle())) {
        tasks.addAll(tasksClient.listTasks(taskList));
      }
    }
    return tasks;
  }
}
