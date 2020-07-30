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
/* global signOut, AuthenticationError, Task, getDateInLocalTimeZone */
// TODO: Refactor so populate functions are done in parallel (Issue #26)

// Stores the last retrieved copy of the user's taskLists and tasks
// (mapped by taskListId)
let taskLists = [];
let tasks = {};

/**
 * Populate Gmail container with user information
 */
function populateGmail() {
  // Get containers for all gmail fields
  const nDaysContainer = document.querySelector('#gmailNDays');
  const mHoursContainer = document.querySelector('#gmailMHours');
  const unreadEmailsContainer =
      document.querySelector('#gmailUnreadEmailsDays');
  const unreadEmailsThreeHrsContainer =
      document.querySelector('#gmailUnreadEmailsHours');
  const importantEmailsContainer =
      document.querySelector('#gmailUnreadImportantEmails');
  const senderInitialContainer =
      document.querySelector('#gmailSenderInitial');
  const senderContainer =
      document.querySelector('#gmailSender');

  // TODO: Allow user to select query parameters (Issue #83)
  const nDays = 7;
  const mHours = 3;

  nDaysContainer.innerText = nDays;
  mHoursContainer.innerText = mHours;

  // Get GmailResponse object that reflects user's gmail account
  // Should contain a field for each datapoint in the Gmail panel
  fetch(`/gmail?nDays=${nDays}&mHours=${mHours}`)
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
      .then((gmailResponse) => {
        unreadEmailsContainer.innerText =
            gmailResponse['unreadEmailsDays'];
        unreadEmailsThreeHrsContainer.innerText =
            gmailResponse['unreadEmailsHours'];
        importantEmailsContainer.innerText =
            gmailResponse['unreadImportantEmails'];
        if (parseInt(gmailResponse['unreadEmailsDays']) !== 0) {
          senderContainer.innerText =
              gmailResponse['sender'];
          senderInitialContainer.innerText =
              gmailResponse['sender'][0].toUpperCase();
        } else {
          senderContainer.innerText = 'N/A';
          senderInitialContainer.innerText = '-';
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
      .then((tasksResponse) => {
        document
            .querySelector('#panel__tasks-to-complete')
            .innerText = tasksResponse['tasksToComplete'];
        document
            .querySelector('#panel__tasks-due-today')
            .innerText = tasksResponse['tasksDueToday'] +
                            ' due today';
        document
            .querySelector('#panel__tasks-completed-today')
            .innerText = tasksResponse['tasksCompletedToday'] +
                            ' completed today';
        document
            .querySelector('#panel__tasks-overdue')
            .innerText = tasksResponse['tasksOverdue'] +
                            ' overdue';
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
      .then((hoursJson) => {
        // Display the days and the free hours for each one of them
        const days = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
        for (let day = 0; day < 5; day++) {
          const dayContainer = document.querySelector('#day-'+day);
          dayContainer.innerText = days[(hoursJson.startDay + day) % 7];
          const workContainer = document.querySelector('#work-day-'+day);
          workContainer.innerText =
              convertTime(hoursJson.workHoursPerDay[day]) + ' (working)';
          const personalContainer =
              document.querySelector('#personal-day-'+day);
          personalContainer.innerText =
              convertTime(hoursJson.personalHoursPerDay[day]) + ' (personal)';
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
 * Convert the time in hours and minutes,
 * and concatenate the string to be displayed
 * @param {int} timeMilli the time in milliseconds that needs to be converted
 * @return {String} String representing the converted free time
 */
function convertTime(timeMilli) {
  const hours = Math.floor(timeMilli / (60 * 60 * 1000));
  const minutes = Math.floor((timeMilli - hours * 60 * 60 * 1000)/(60*1000));
  return hours + 'h ' + minutes + 'm free';
}

/**
 * Function to test getting taskLists and adding a new taskList.
 * Will 1) request a new taskList be made with a default name (the current time)
 * and 2) get the new list of taskLists and log them in the console.

 * Function to test getting taskLists, adding a new taskList, and then getting
 * a new task.
 *
 * Will 1) request a new taskList be made with a default name (current time)
 * then, 2) add a task to the new taskList and
 * 3) get the new list of taskLists and log them in the console.
 */
function postAndGetTaskList() {
  const sampleTitle =
      getDateInLocalTimeZone().getTime().toString();

  postNewTaskList(sampleTitle)
      .then((taskList) => {
        const sampleTask =
                  new Task(
                      'test',
                      'This is a test',
                      getDateInLocalTimeZone()
                  );
        const taskListId = taskList.id;

        postNewTask(taskListId, sampleTask)
            .then(() => {
              getTaskListsAndTasks()
                  .then(() => {
                    console.log(tasks);
                    console.log(taskLists);
                  });
            });
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
        tasks = response.tasks;
        taskLists = response.taskLists;
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

/**
 * Populate Plan-mail panel with potential times to read the emails
 */
function populatePlanMail() {
  const planContainer = document.querySelector('#plan');
  fetch('/plan-mail')
      .then((response) => {
        // If response is a 403, user is not authenticated
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((planMailResponse) => {
        // Display the potential times to create events
        const numWordsContainer = document.querySelector('#word-count');
        numWordsContainer.innerText = planMailResponse.wordCount;
        const averageReplyContainer = document.querySelector('#average-reply');
        averageReplyContainer.innerText = planMailResponse.averageReadingSpeed;
        const timeNeededContainer = document.querySelector('#time-needed');
        timeNeededContainer.innerText = planMailResponse.minutesToRead;
        const intervalContainer = document.querySelector('#free-interval');
        for (const index in planMailResponse.potentialMeetingTimes) {
          if (typeof index == 'number') {
            const liElement = document.createElement('li');
            liElement.innerText =
              planMailResponse.potentialMeetingTimes[index].key +
              ' ' +
              planMailResponse.potentialMeetingTimes[index].value;
            intervalContainer.appendChild(liElement);
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
