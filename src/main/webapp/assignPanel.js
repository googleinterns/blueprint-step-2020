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
/* global getTaskListsAndTasks, postNewTaskList,
 enableAssignStartResetButton, createTextListElementFromString,
 encodeListForUrl, AuthenticationError, Task, postNewTask,
 disableAssignAcceptRejectButtons */

// Script for handling the behaviour of the Assign panel's features

const assignTaskListTitle = 'Actionable Emails (Blueprint)';
let assignTaskListId;
let assignTasks = [];
let nDays;
let unreadOnly;
let subjectLinePhrases = [];
let actionableEmails = [];

const taskNotesPrefix =
    '*** ' +
    'Made automatically with Blueprint. ' +
    'Please add notes above this line, ' +
    'and do not edit anything below this line. ' +
    'Blueprint needs this information to better serve you. ' +
    'Thank you for your understanding. ' +
    '***' +
    '\n';
const emailIdPrefix = 'emailId:';

/**
 * Process for initializing the assign panel after login or a change in settings
 *
 * @return {Promise<null>} promise that resolves when all assign global
 *     variables are initialized
 */
function initializeAssignFeatures() {
  const actionItemsCountElement =
      document.getElementById('assign-suspected-action-items');

  return Promise.all(
      [
        getAssignPanelTasks(),
        fetchActionableEmails(subjectLinePhrases, unreadOnly, nDays),
      ]
  ).then((values) => {
    // Parse values from server
    const assignTasksResponse = values[0];
    const actionableEmailsResponse = values[1];

    assignTaskListId = assignTasksResponse.taskListId;
    assignTasks = assignTasksResponse.tasks;

    const tasksEmailIds =
        new Set(assignTasks
            .map((task) => parseEmailIdFromTaskNotes(task))
            .filter((id) => id !== null));

    actionableEmails =
        actionableEmailsResponse
            .filter((message) => !tasksEmailIds.has(message.id));

    // Set the panel statistics
    actionItemsCountElement.innerText = actionableEmails.length;
  });
}

/**
 * Will get the email ID from the task notes.
 * Assumes that the email ID will be in the last line of the notes.
 *
 * @param {Task} task task object
 * @return {string|null} id if present, null otherwise
 */
function parseEmailIdFromTaskNotes(task) {
  // Get emailId from end of message
  const notes = task.notes;
  if (notes == null) {
    return null;
  }

  const emailIdIndex = notes.lastIndexOf(emailIdPrefix);
  if (emailIdIndex === -1) {
    console.log('No email ID found!');
    return null;
  }
  return notes.substr(emailIdIndex + emailIdPrefix.length);
}

/**
 * Get the tasks for the assign panel from the server
 *
 * @return {Promise<Object>} promise that will return an object containing
 *     1) the id of the relevant taskList for the assign panel and
 *     2) a list of Task objects associated with that taskList
 */
function getAssignPanelTasks() {
  return getTaskListsAndTasks()
      .then((response) => {
        const assignTaskLists =
            response.taskLists
                .filter((taskList) => taskList.title === assignTaskListTitle);

        if (assignTaskLists.length === 0) {
          return postNewTaskList(assignTaskListTitle).
              then((taskList) => {
                return {
                  'taskListId': taskList.id,
                  'tasks': [],
                };
              });
        }

        const assignTaskList = assignTaskLists[0];
        const assignTaskListTasks = response.tasks[assignTaskList.id];
        return {
          'taskListId': assignTaskLists[0].id,
          'tasks': assignTaskListTasks,
        };
      });
}

/**
 * Sets the values of the settings for the assign panel based on what is
 * present in the panel (will be the default values on sign-in)
 */
function setUpAssign() {
  const nDaysElement = document.getElementById('assign-n-days');
  nDays = parseInt(nDaysElement.innerText);

  const unreadOnlyContainerElement =
      document.getElementById('assign-unread-only-icon');
  const unreadOnlyUnselectedElement =
      unreadOnlyContainerElement
          .querySelector('.panel__toggle-icon--unselected');
  unreadOnly = unreadOnlyUnselectedElement.hasAttribute('hidden');

  const phrasesListElement = document.getElementById('assign-list');
  const listElements =
      phrasesListElement.querySelectorAll('.panel__list-text');
  subjectLinePhrases = [];
  listElements
      .forEach((element) => subjectLinePhrases.push(element.innerText));

  initializeAssignFeatures()
      .then(() => enableAssignStartResetButton());
}

/**
 * Reset the settings panel to display the values currently set for the
 * assignPanel (i.e. ignore new changes - reset to old values)
 */
function assignRevertSettings() {
  const nDaysElement = document.getElementById('assign-n-days');
  nDaysElement.innerText = nDays;

  const unreadOnlyContainerElement =
      document.getElementById('assign-unread-only-icon');
  const unreadOnlyUnselectedElement =
      unreadOnlyContainerElement
          .querySelector('.panel__toggle-icon--unselected');
  const unreadOnlySelectedElement =
      unreadOnlyContainerElement
          .querySelector('.panel__toggle-icon--selected');
  if (unreadOnly) {
    unreadOnlyUnselectedElement.setAttribute('hidden', '');
    unreadOnlySelectedElement.removeAttribute('hidden');
  } else {
    unreadOnlyUnselectedElement.removeAttribute('hidden');
    unreadOnlySelectedElement.setAttribute('hidden', '');
  }

  const phrasesListElement = document.getElementById('assign-list');
  phrasesListElement.innerHTML = '';
  subjectLinePhrases
      .forEach(
          (phrase) => createTextListElementFromString(phrase, 'assign-list'));
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
 *     from client. Should be list of ActionableMessage Objects. Will throw
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

/**
 * Add the current actionable email to the assign taskList
 */
function assignAddCurrentEmail() {
  const currentEmail = actionableEmails.shift();
  const newTaskNotes = taskNotesPrefix + emailIdPrefix + currentEmail.id;
  const newTask = new Task(currentEmail.subject, newTaskNotes);

  postNewTask(assignTaskListId, newTask);
  assignDisplayNextEmail();
}

/**
 * Skip the current actionable email - don't add to the taskList
 */
function assignSkipCurrentEmail() {
  actionableEmails.shift();
  assignDisplayNextEmail();
}

/**
 * Display the next email for user review. If none left, disable the buttons and
 * reset the subject line field to the default value
 */
function assignDisplayNextEmail() {
  const actionItemsCountElement =
      document.getElementById('assign-suspected-action-items');
  const subjectLineElement = document.getElementById('assign-subject');

  actionItemsCountElement.innerText = actionableEmails.length;
  if (actionableEmails.length === 0) {
    subjectLineElement.innerText = '';
    disableAssignAcceptRejectButtons();
    return;
  }

  subjectLineElement.innerText =
      `(From: ${actionableEmails[0].sender}) ${actionableEmails[0].subject}`;
}
