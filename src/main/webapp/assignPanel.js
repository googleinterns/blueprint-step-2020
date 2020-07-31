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

// Script for handling the behaviour of the Assign panel's features

let assignTaskListId;
const assignTask = [];
let nDays;
let unreadOnly;
const subjectLinePhrases = [];

/**
 * Process for initializing the assign panel after login.
 * Must be called before using any other assign panel functions
 */
function setUpAssign() {
  updateSettings();
  console.log(nDays);
  console.log(unreadOnly);
  console.log(subjectLinePhrases);
}

/**
 * Sets the values of the settings for the assign panel based on what is
 * present in the panel
 */
function updateSettings() {
  const nDaysElement = document.getElementById('assignNDays');
  nDays = parseInt(nDaysElement.innerText);

  const unreadOnlyContainerElement =
      document.getElementById('assignUnreadOnlyIcon');
  const unreadOnlyUnselectedElement =
      unreadOnlyContainerElement
          .querySelector('.panel__toggle-icon--unselected');
  unreadOnly = unreadOnlyUnselectedElement.hasAttribute('hidden');

  const phrasesListElement = document.getElementById('assignList');
  const listElements = phrasesListElement.querySelectorAll('.panel__list-text');
  listElements.forEach((element) => subjectLinePhrases.push(element.innerText));
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
