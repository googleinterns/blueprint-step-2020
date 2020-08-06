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

// Script to handle populating data in the panels

/* eslint-disable no-unused-vars */
/* global signOut, AuthenticationError, Task, getDateInLocalTimeZone,
 encodeListForUrl */

/**
 * Populate Tasks container with user information
 */
function populateTasks() {
  let fetchFrom;
  const select = document.querySelector('#tasks-select');
  // Cast from HTMLOptionsCollection to Array
  const options = Array(...select.options);

  if (options.length === 0) {
    fetchFrom = '/tasks';
  } else {
    const selectedOptions = [];
    options.forEach((option) => {
      if (option.selected) {
        selectedOptions.push(option.value);
      }
    });
    fetchFrom = '/tasks?taskLists=' + selectedOptions.join();
  }

  // Set default values while loading
  const tasksToCompleteElement = document.getElementById('tasks-to-complete');
  const tasksDueTodayElement = document.getElementById('tasks-due-today');
  const tasksCompletedTodayElement =
      document.getElementById('tasks-completed-today');
  const tasksOverdueElement = document.getElementById('tasks-overdue');
  tasksToCompleteElement.innerText = '...';
  tasksDueTodayElement.innerText = '...';
  tasksCompletedTodayElement.innerText = '...';
  tasksOverdueElement.innerText = '...';

  fetch(fetchFrom)
      .then((response) => {
        // If response is a 403, user is not authenticated
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((tasksResponse) => {
        if (options.length === 0) {
          const taskListIdsToTitles = tasksResponse['taskListIdsToTitles'];
          select.innerText = '';
          for (const taskListId in taskListIdsToTitles) {
            if (Object.prototype
                .hasOwnProperty
                .call(taskListIdsToTitles, taskListId)) {
              const option = document.createElement('option');
              option.value = taskListId;
              option.innerText = taskListIdsToTitles[taskListId];
              select.append(option);
            }
          }
        }
        tasksToCompleteElement
            .innerText = tasksResponse['tasksToCompleteCount'];
        tasksDueTodayElement
            .innerText = tasksResponse['tasksDueTodayCount'];
        tasksCompletedTodayElement
            .innerText = tasksResponse['tasksCompletedTodayCount'];
        tasksOverdueElement
            .innerText = tasksResponse['tasksOverdueCount'];
      })
      .catch((e) => {
        console.log(e);
        if (e instanceof AuthenticationError) {
          signOut();
        }
      });
}

/**
 * Will reset the tasklists selector and populate the panel again,
 * giving the system the chance to add new tasklists to the tasklists options.
 */
function resetTasks() {
  document.querySelector('#tasks-select').options.length = 0;
  populateTasks();
}

/**
 * Populate Calendar container with user's events
 */
function populateCalendar() {
  const calendarContainer = document.querySelector('#calendar');
  fetch('/calendar')
      .then((response) => {
        // If response is a 403, user is not authenticated
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((hoursJson) => {
        // Display the days and the free hours for each one of them
        const days = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        const panelContent = document.querySelector('#panel-content');
        panelContent.innerHTML = '';
        for (const day in hoursJson.workTimeFree) {
          if (typeof day == 'string') {
            const panelContentEntry = document.createElement('div');
            panelContentEntry.className = 'panel__content-entry';
            const dayContainer = document.createElement('p');
            dayContainer.className = 'panel__text-icon u-text-calendar';
            dayContainer.innerText =
                days[(hoursJson.startDay + parseInt(day)) % 7];
            const timeContainer = document.createElement('div');
            const workContainer = document.createElement('p');
            workContainer.className = 'u-block';
            workContainer.innerText =
                hoursJson.workTimeFree[day].hours +
                'h ' + hoursJson.workTimeFree[day].minutes +
                'm free (working)';
            const personalContainer = document.createElement('p');
            personalContainer.className = 'u-block';
            personalContainer.innerText =
                hoursJson.personalTimeFree[day].hours +
                'h ' + hoursJson.personalTimeFree[day].minutes +
                'm free (personal)';
            timeContainer.appendChild(workContainer);
            timeContainer.appendChild(personalContainer);
            panelContentEntry.appendChild(dayContainer);
            panelContentEntry.appendChild(timeContainer);
            panelContent.appendChild(panelContentEntry);
          }
        }
      })
      .catch((e) => {
        console.log(e);
        if (e instanceof AuthenticationError) {
          signOut();
        }
      });
}

/**
 * Post a new task to a given taskList
 *
 * @param {string} taskListId the id of the taskList that the new task should
 *     belong to
 * @param {Task} taskObject valid Task object
 * @return {Promise<any>} A promise that is resolved once the task is
 *     posted
 */
function postNewTask(taskListId, taskObject) {
  const taskJson = JSON.stringify(taskObject);

  const newTaskRequest =
      new Request(
          '/tasks?taskListId=' + taskListId,
          {method: 'POST', body: taskJson}
      );

  return fetch(newTaskRequest)
      .then((response) => {
        switch (response.status) {
          case 200:
            resetTasks();
            return response.json();
          case 403:
            throw new AuthenticationError();
          default:
            throw new Error(response.status + ' ' + response.statusText);
        }
      });
}

/**
 * Update the tasks and taskLists lists.
 *
 * @return {Promise<any>} A promise that is resolved once the tasks and
 *     and taskLists arrays are updated, and rejected if there's an error
 */
function getTaskListsAndTasks() {
  return fetch('/taskLists')
      .then((response) => {
        switch (response.status) {
          case 200:
            return response.json();
          case 403:
            throw new AuthenticationError();
          default:
            throw new Error(response.status + ' ' + response.statusText);
        }
      })
      .then((response) => {
        return response;
      });
}

/**
 * Post a new taskList to the server
 *
 * @param {string} title title of new taskList.
 * @return {Promise<any>} A promise that is resolved once the taskList is
 *     posted
 */
function postNewTaskList(title) {
  const newTaskListRequest =
      new Request(
          '/taskLists?taskListTitle=' + title,
          {method: 'POST'}
      );

  return fetch(newTaskListRequest)
      .then((response) => {
        switch (response.status) {
          case 200:
            resetTasks();
            return response.json();
          case 403:
            throw new AuthenticationError();
          default:
            throw new Error(response.status + ' ' + response.statusText);
        }
      })
      .then((taskListObject) => {
        return taskListObject;
      });
}

/**
 * Populate Go container with hardcoded values
 */
function populateGo() {
  const goContainer = document.querySelector('#go');

  fetch('/directions')
      .then((response) => {
        // If response is a 403, user is not authenticated
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((legs) => {
        // Convert JSON to string containing all legs
        // and display it on client
        // Handle case where user has no events to avoid unwanted behaviour
        if (legs.length !== 0) {
          goContainer.innerText = legs;
        } else {
          goContainer.innerText = 'No direction legs returned';
        }
      })
      .catch((e) => {
        console.log(e);
        if (e instanceof AuthenticationError) {
          signOut();
        }
      });
}
