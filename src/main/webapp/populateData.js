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
/* global signOut, AuthenticationError */
// TODO: Refactor so populate functions are done in parallel (Issue #26)
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

  // Get GmailResponse object that reflects user's gmail account
  // Should contain a field for each datapoint in the Gmail panel
  fetch('/gmail')
      .then((response) => {
        // If response is a 403, user is not authenticated
        if (response.status === 403) {
          throw new AuthenticationError();
        }
        return response.json();
      })
      .then((gmailResponse) => {
        nDaysContainer.innerText =
            gmailResponse['nDays'];
        nDaysContainer.innerText =
            gmailResponse['mHours'];
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
