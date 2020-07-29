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

/* eslint-disable no-unused-vars */

/**
 * A class to encapsulate some of the values from a Google Tasks task, as well
 * as the parent taskList name
 * https://developers.google.com/tasks/v1/reference/tasks
 */
class Task {
  /**
   * Creates a Task object with a due date
   *
   * @param {string} title a title for the task
   * @param {string} notes messages associated with a given task
   * @param {Date} due a Date object / null if not present
   */
  constructor(title, notes, due) {
    this.title = title;
    this.notes = notes;
    if (due !== null) {
      this.due = due.toISOString();
    }
  }
}
