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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import java.io.IOException;
import java.util.List;

public class TasksUtility {
  // Make constructor private so no instances of this class can be made
  private TasksUtility() {}

  /**
   * Get instance of Tasks Service
   *
   * @param credential valid Google credential object with user's accessKey inside
   * @return Google Tasks service instance
   */
  public static Tasks getTasksService(Credential credential) {
    HttpTransport transport = AuthenticationUtility.getAppEngineTransport();
    JsonFactory jsonFactory = AuthenticationUtility.getJsonFactory();

    return new Tasks.Builder(transport, jsonFactory, credential).build();
  }

  /**
   * Get a list of all TaskLists in the user's account
   *
   * @param tasksService a valid Google Tasks service instance
   * @return a list of TaskLists that belong to the user's account
   * @throws IOException if an issue occurs with the Tasks service
   */
  public static List<TaskList> listTaskLists(Tasks tasksService) throws IOException {
    return tasksService.tasklists().list().execute().getItems();
  }

  /**
   * Get a list of all tasks in a specific TaskList from a user's account
   *
   * @param tasksService a valid Google Tasks service instance
   * @param taskList a TaskList object that represents a user's tasklist
   * @return a list of Tasks that belong to the list in the user's account
   * @throws IOException if an issue occurs with the Tasks service
   */
  public static List<Task> listTasks(Tasks tasksService, TaskList taskList) throws IOException {
    return tasksService.tasks().list(taskList.getId()).execute().getItems();
  }
}