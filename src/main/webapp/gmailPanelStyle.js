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
/* global show, hide, populateGmail */

// Script for handling the behaviour of the Mail panel's layout

/**
 * Display the settings in the Mail panel (and hide the content)
 */
function displayGmailSettings() {
  show('gmail-settings');
  hide('gmail-content');
}

/**
 * Display the content of the Mail panel (and hide the settings)
 */
function displayGmailContent() {
  show('gmail-content');
  hide('gmail-settings');
}

/**
 * Will confirm the settings entered by the user in the settings panel,
 * and "click" the settings icon so the panel swtiches to the content view
 * (and all associated side effects of clicking the button, etc, are handled)
 */
function confirmSettingsAndShowGmailPanel() {
  populateGmail();
  const assignSettingsButton =
      document.querySelector('#gmail-settings-icon img:not([hidden])');
  assignSettingsButton.click();
}
