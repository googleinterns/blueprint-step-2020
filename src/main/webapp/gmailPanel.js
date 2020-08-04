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
/* global AuthenticationError, signOut */

// Script for handling the behaviour of the Mail panel's features

let gmailNDays;
let gmailMHours;

/**
 * Populate Gmail container with user information
 */
function populateGmail() {
  // Get values for nDays and mHours
  const nDaysSettingsContainer =
      document.querySelector('#gmail-settings-n-days');
  const mHoursSettingsContainer =
      document.querySelector('#gmail-settings-m-hours');
  gmailNDays = parseInt(nDaysSettingsContainer.innerText);
  gmailMHours = parseInt(mHoursSettingsContainer.innerText);

  // Get containers for all gmail fields
  const nDaysContainer = document.querySelector('#gmail-n-days');
  const mHoursContainer = document.querySelector('#gmail-m-hours');
  const unreadEmailsContainer =
      document.querySelector('#gmail-unread-emails-days');
  const unreadEmailsThreeHrsContainer =
      document.querySelector('#gmail-unread-emails-hours');
  const importantEmailsContainer =
      document.querySelector('#gmail-unread-important-emails');
  const senderInitialContainer =
      document.querySelector('#gmail-sender-initial');
  const senderContainer =
      document.querySelector('#gmail-sender');

  nDaysContainer.innerText = gmailNDays;
  mHoursContainer.innerText = gmailMHours;

  // Get GmailResponse object that reflects user's gmail account
  // Should contain a field for each datapoint in the Gmail panel
  fetch(`/gmail?nDays=${gmailNDays}&mHours=${gmailMHours}`)
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
 * Reset the values for nDays and mHours in the settings panel
 */
function gmailRevertSettings() {
  const nDaysSettingsContainer =
      document.querySelector('#gmail-settings-n-days');
  const mHoursSettingsContainer =
      document.querySelector('#gmail-settings-m-hours');
  nDaysSettingsContainer.innerText = gmailNDays;
  mHoursSettingsContainer.innerText = gmailMHours;
}
