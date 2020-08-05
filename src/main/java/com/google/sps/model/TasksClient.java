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

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Contract for trivial get requests from the Tasks API. */
public interface TasksClient {
  /**
   * Get all tasks from a taskList in a user's Tasks account.
   *
   * @param taskList TaskList object that contains the desired tasks
   * @return List of all tasks including hidden/completed tasks in the taskList
   * @throws IOException if an issue occurs with the TasksService
   */
  List<Task> listTasks(TaskList taskList) throws IOException;

  /**
   * Get all taskLists in a user's Tasks account.
   *
   * @return List of all taskLists
   * @throws IOException if an issue occurs with the TasksService
   */
  List<TaskList> listTaskLists() throws IOException;

  /**
   * Add a new task list to user's Tasks account
   *
   * @param title title of task list
   * @return TaskList entity that matches what was posted
   * @throws IOException if an issue occurs with the TasksService
   */
  TaskList postTaskList(String title) throws IOException;

  /**
   * Add a new task to a tasklist in a user's tasks account
   *
   * @param parentTaskListId id of tasklist that the new task will belong to
   * @param task task object to be posted to user's tasks account
   * @return Task object that contains passed information
   * @throws IOException if an issue occurs with the tasksService
   */
  Task postTask(String parentTaskListId, Task task) throws IOException;

  /**
   * Get the all tasks in all the user's task lists
   *
   * @param tasksClient Either a mock TaskClient or a taskClient with a valid credential
   * @return List of tasks from all task lists in user's account
   * @throws IOException if an issue occurs with the tasksService
   */
  static List<Task> getAllTasksFromAllTaskLists(TasksClient tasksClient) throws IOException {
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
  static List<Task> getAllTasksFromSpecificTaskLists(
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
