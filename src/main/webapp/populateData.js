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
      .then((hoursJson) => {
        // Display the days and the free hours for each one of them
        const days = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
        for (let day = 0; day < 5; day++) {
          const dayContainer = document.querySelector('#day-'+day);
          dayContainer.innerText = days[hoursJson.startDay + day];
          const workContainer = document.querySelector('#work-'+day);
          workContainer.innerText = 
              convertTime(hoursJson.workHours[day]) + ' (working)';
          const personalContainer = document.querySelector('#personal-'+day);
          personalContainer.innerText = 
              convertTime(hoursJson.personalHours[day]) + ' (personal)';
        }
        console.log(hoursJson);
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
