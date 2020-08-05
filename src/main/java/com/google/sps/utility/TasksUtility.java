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

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.sps.model.TasksClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TasksUtility {

  private TasksUtility() {}

  /**
   * Get the all tasks in all the user's task lists
   *
   * @param tasksClient Either a mock TaskClient or a taskClient with a valid credential
   * @return List of tasks from all task lists in user's account
   * @throws IOException if an issue occurs with the tasksService
   */
  public static List<Task> getAllTasksFromAllTaskLists(TasksClient tasksClient) throws IOException {
    List<TaskList> taskLists = tasksClient.listTaskLists();
    List<Task> tasks = new ArrayList<>();
    for (TaskList taskList : taskLists) {
      tasks.addAll(tasksClient.listTasks(taskList));
    }
    return tasks;
  }

  /**
   * Get the tasks in the user's task lists with the given task list IDs
   *
   * @param tasksClient Either a mock TaskClient or a taskClient with a valid credential
   * @param taskListTitles List of task list IDs which tasks should be obtained from
   * @return List of tasks from specified task lists in user's account
   * @throws IOException if an issue occurs with the tasksService
   */
  public static List<Task> getAllTasksFromSpecificTaskLists(
      TasksClient tasksClient, Set<String> taskListIds) throws IOException {
    List<TaskList> taskLists = tasksClient.listTaskLists();
    List<Task> tasks = new ArrayList<>();
    for (TaskList taskList : taskLists) {
      if (taskListIds.contains(taskList.getId())) {
        tasks.addAll(tasksClient.listTasks(taskList));
      }
    }
    return tasks;
  }
}
