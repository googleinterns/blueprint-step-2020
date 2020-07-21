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
/* global signOut, AuthenticationError, Task */
// TODO: Refactor so populate functions are done in parallel (Issue #26)

let tasklists = [];
let tasks = {};

/**
 * Populate Gmail container with user information
 */
function populateGmail() {
  // Get container for Gmail content
  const gmailContainer = document.querySelector('#gmail');

  // Get list of messageIds from user's Gmail account
  // and display them on the screen
  fetch('/gmail')
      .then((response) => {
        // If response is a 403, user is not authenticated
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((emailList) => {
        // Convert JSON to string containing all messageIds
        // and display it on client
        if (emailList.length !== 0) {
          const emails =
              emailList.map((a) => a.id).reduce((a, b) => a + '\n' + b);
          gmailContainer.innerText = emails;
        } else {
          gmailContainer.innerText = 'No emails found';
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
 * Populate Tasks container with user information
 */
function populateTasks() {
  // Get Container for Tasks content
  const tasksContainer = document.querySelector('#tasks');

  // Get list of tasks from user's Tasks account
  // and display the task titles from all task lists on the screen
  fetch('/tasks')
      .then((response) => {
        // If response is a 403, user is not authenticated
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((tasksList) => {
        // Convert JSON to string containing all task titles
        // and display it on client
        if (tasksList.length !== 0) {
          const tasks =
              tasksList.map((a) => a.title).reduce((a, b) => a + '\n' + b);
          tasksContainer.innerText = tasks;
        } else {
          tasksContainer.innerText = 'No tasks found';
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
      .then((eventList) => {
        // Convert JSON to string containing all event summaries
        // and display it on client
        // Handle case where user has no events to avoid unwanted behaviour
        if (eventList.length !== 0) {
          const events =
              eventList.map((a) => a.summary).reduce((a, b) => a + '\n' + b);
          calendarContainer.innerText = events;
        } else {
          calendarContainer.innerText = 'No events in the calendar';
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
 * Function to test the POST method of tasks.
 * Will 1) Get all of the tasklists, 2) request a new tasklist be made
 * with a random name then, 3) add a test task to that tasklist.
 * Then, the method will populate the response into the "assign" panel
 */
function postTaskToSampleList() {
  fetch('/tasklists')
      .then((response) => {
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((response) => {
        const taskListWithTasks = {};
        tasklists = response.tasklists;
        tasks = response.tasks;

        console.log(tasklists);
        console.log(tasks);

        const newTaskListRequest =
            new Request(
                '/tasklists?taskListTitle=' + new Date().getTime(),
                {method: 'POST'}
            );

        fetch(newTaskListRequest)
            .then((response) => response.json())
            .then((taskListObject) => {
              tasklists.push(taskListObject);

              const taskListId = taskListObject.id;
              tasks[taskListId] = [];

              console.log(tasklists);
              console.log(tasks);

              const dateObject = new Date();
              const currentTime =
                  dateObject.getTime() - dateObject.getTimezoneOffset()*60*1000;
              const sampleTask =
                  new Task(
                      'test',
                      'This is a test',
                      new Date().getDateObjectWithLocalTime()
                  );

              const sampleTaskJson = JSON.stringify(sampleTask);

              const newTaskRequest =
                  new Request(
                      '/tasks?taskListId=' + taskListId,
                      {method: 'POST', body: sampleTaskJson}
                  );

              fetch(newTaskRequest)
                  .then((response) => response.json())
                  .then((taskObject) => {
                    tasks[taskListId].push(taskObject);
                    console.log(tasklists);
                    console.log(tasks);
                  });
            });
      });
}
