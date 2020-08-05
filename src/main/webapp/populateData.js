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
        document
            .querySelector('#tasks-to-complete')
            .innerText = tasksResponse['tasksToCompleteCount'];
        document
            .querySelector('#tasks-due-today')
            .innerText = tasksResponse['tasksDueTodayCount'] +
                            ' due today';
        document
            .querySelector('#tasks-completed-today')
            .innerText = tasksResponse['tasksCompletedTodayCount'] +
                            ' completed today';
        document
            .querySelector('#tasks-overdue')
            .innerText = tasksResponse['tasksOverdueCount'] +
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
        const days = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        const panelContent = document.querySelector('#panel-content');
        panelContent.innerHTML = '';
        for (const day in hoursJson.workTimeFree) {
          if (typeof day === 'string') {
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
        const messageEventContainer = document.querySelector('#message-event');
        messageEventContainer.innerHTML = '';
        const intervalContainer = document.querySelector('#free-interval');
        intervalContainer.innerHTML = '';
        if (planMailResponse.potentialEventTimes.length === 0) {
          messageEventContainer.innerText = 'No new events needed';
        } else {
          messageEventContainer.innerText = 'Click to schedule';
          for (const index in planMailResponse.potentialEventTimes) {
            if (typeof index === 'string') {
              const buttonElement = document.createElement('button');
              buttonElement.innerText =
                `${planMailResponse.potentialEventTimes[index].start} to
                ${planMailResponse.potentialEventTimes[index].end}`;
              buttonElement.name =
                  planMailResponse.potentialEventTimes[index].start;
              buttonElement.value =
                  planMailResponse.potentialEventTimes[index].end;
              buttonElement.addEventListener('click', () => {
                createEvent(buttonElement.name, buttonElement.value);
              });
              intervalContainer.appendChild(buttonElement);
            }
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
 * Call a post request to create a new event in the calendar, then display the
 * updated information in calendar and plan panels
 *
 * @param {string} eventStart the start time of the event to create
 * @param {string} eventEnd the end time of the event to create
 */
function createEvent(eventStart, eventEnd) {
  const params = new URLSearchParams();
  params.append('start', eventStart);
  params.append('end', eventEnd);
  fetch('/calendar', {method: 'POST', body: params});
  populateCalendar();
  populatePlanMail();
}

/**
 * Set up the assign panel. For now, this just prints the response
 * from the server for /gmail-actionable-emails
 */
function setUpAssign() {
  const assignContent = document.querySelector('#assign');

  const subjectLinePhrases = ['Action Required', 'Action Requested'];
  const unreadOnly = true;
  const nDays = 7;
  fetchActionableEmails(subjectLinePhrases, unreadOnly, nDays)
      .then((response) => {
        assignContent.innerText = response
            .map((obj) => obj.subject)
            .join('\n');
      })
      .catch((e) => {
        console.log(e);
        if (e instanceof AuthenticationError) {
          signOut();
        }
      });
}

/**
 * Get actionable emails from server. Used for assign panel
 *
 * @param {string[]} listOfPhrases list of words/phrases that the subject line
 *     of user's emails should be queried for
 * @param {boolean} unreadOnly true if only unread emails should be returned,
 *     false otherwise
 * @param {number} nDays number of days to check unread emails for.
 *     Should be an integer > 0
 * @return {Promise<Object>} returns promise that returns the JSON response
 *     from client. Should be list of Gmail Message Objects. Will throw
 *     AuthenticationError in the case of a 403, or generic Error in
 *     case of other error code
 */
function fetchActionableEmails(listOfPhrases, unreadOnly, nDays) {
  const listOfPhrasesString = encodeListForUrl(listOfPhrases);
  const unreadOnlyString = unreadOnly.toString();
  const nDaysString = nDays.toString();

  const queryString =
      `/gmail-actionable-emails?subjectLinePhrases=${listOfPhrasesString}` +
      `&unreadOnly=${unreadOnlyString}&nDays=${nDaysString}`;

  return fetch(queryString)
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
