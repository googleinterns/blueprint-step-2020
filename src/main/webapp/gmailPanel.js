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
  const nDaysSettingsContainer = document.querySelector('#gmailSettingsNDays');
  const mHoursSettingsContainer =
      document.querySelector('#gmailSettingsMHours');
  gmailNDays = parseInt(nDaysSettingsContainer.innerText);
  gmailMHours = parseInt(mHoursSettingsContainer.innerText);

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
  // Get values for nDays and mHours
  const nDaysSettingsContainer = document.querySelector('#gmailSettingsNDays');
  const mHoursSettingsContainer =
      document.querySelector('#gmailSettingsMHours');
  nDaysSettingsContainer.innerText = gmailNDays;
  mHoursSettingsContainer.innerText = gmailMHours;
}
