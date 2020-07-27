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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.sps.utility.ServletUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Handles basic GET/POST requests to and from the Google Tasks service */
public class TasksClientImpl implements TasksClient {

  private final Tasks tasksService;

  private TasksClientImpl(Credential credential) {
    HttpTransport transport = UrlFetchTransport.getDefaultInstance();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    String applicationName = ServletUtility.APPLICATION_NAME;

    tasksService =
        new Tasks.Builder(transport, jsonFactory, credential)
            .setApplicationName(applicationName)
            .build();
  }

  @Override
  public List<Task> listTasks(TaskList taskList) throws IOException {
    // Returns null if no tasks exist. Convert to empty list for ease.
    // setShowHidden indicates that hidden/completed tasks are retrieved as well.
    List<Task> tasks =
        tasksService.tasks().list(taskList.getId()).setShowHidden(true).execute().getItems();

    return tasks != null ? tasks : new ArrayList<>();
  }

  @Override
  public List<TaskList> listTaskLists() throws IOException {
    // returns null if no taskLists exist. Convert to empty list for ease.
    List<TaskList> taskLists = tasksService.tasklists().list().execute().getItems();

    return taskLists != null ? taskLists : new ArrayList<>();
  }

  @Override
  public List<TaskList> listTaskLists(List<String> taskListTitles) throws IOException {
    // returns null if no taskLists exist. Convert to empty list for ease.
    List<TaskList> taskLists = tasksService.tasklists().list().execute().getItems();
    taskLists = taskLists != null ? taskLists : new ArrayList<>();
    taskLists.stream()
        .filter(taskList -> taskListTitles.contains(taskList.getTitle()))
        .collect(Collectors.toList());
    return taskLists;
  }

  @Override
  public TaskList postTaskList(String title) throws IOException {
    TaskList taskList = new TaskList();
    taskList.setTitle(title);

    return tasksService.tasklists().insert(taskList).execute();
  }

  /** Factory to create a TasksClientImpl instance with given credential */
  public static class Factory implements TasksClientFactory {
    /**
     * Create a TasksClientImpl instance
     *
     * @param credential valid Google credential object
     * @return TasksClientImpl instance with credential
     */
    @Override
    public TasksClient getTasksClient(Credential credential) {
      return new TasksClientImpl(credential);
    }
  }
}
