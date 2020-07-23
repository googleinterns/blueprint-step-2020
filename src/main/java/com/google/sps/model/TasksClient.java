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
import java.util.List;

/** Contract for trivial get requests from the Tasks API. */
public interface TasksClient {
  /**
   * Get all tasks from a tasklist in a user's Tasks account.
   *
   * @param taskList TaskList object that contains the desired tasks
   * @return List of all tasks including hidden/completed tasks in the tasklist
   * @throws IOException if an issue occurs with the TasksService
   */
  List<Task> listTasks(TaskList taskList) throws IOException;

  /**
   * Get all tasklists in a user's Tasks account.
   *
   * @return List of all tasklists
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
}
