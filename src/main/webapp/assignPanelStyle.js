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
/* global show, hide, setUpAssign, assignRevertSettings, assignAddCurrentEmail,
 assignSkipCurrentEmail, setUpAssign, assignDisplayNextEmail,
 actionableEmails */

// Script for handling the behaviour of the Assign panel's layout

// Add default event listeners
const acceptButton = document.getElementById('assign-accept-button');
const rejectButton = document.getElementById('assign-reject-button');
acceptButton.addEventListener('click', assignAddCurrentEmail);
rejectButton.addEventListener('click', assignSkipCurrentEmail);

/**
 * Display the settings in the Assign panel (and hide the content)
 */
function displayAssignSettings() {
  show('assign-settings');
  hide('assign-content');

  acceptButton.innerText = 'Confirm';
  rejectButton.innerText = 'Reset';

  acceptButton.addEventListener('click', setUpAssign);
  rejectButton.addEventListener('click', assignRevertSettings);
  acceptButton.removeEventListener('click', assignAddCurrentEmail);
  rejectButton.removeEventListener('click', assignSkipCurrentEmail);

  enableAssignAcceptRejectButtons();
}

/**
 * Display the content of the Assign panel (and hide the settings)
 */
function displayAssignContent() {
  show('assign-content');
  hide('assign-settings');

  acceptButton.innerText = 'Add Task';
  rejectButton.innerText = 'Skip Item';

  acceptButton.removeEventListener('click', setUpAssign);
  rejectButton.removeEventListener('click', assignRevertSettings);
  acceptButton.addEventListener('click', assignAddCurrentEmail);
  rejectButton.addEventListener('click', assignSkipCurrentEmail);

  const assignStartResetButtonIcon =
      document.getElementById('assignStartResetButton')
          .querySelector('.button-circle__ascii-icon');

  if (assignStartResetButtonIcon.innerText === '▶' ||
      actionableEmails.length === 0) {
    disableAssignAcceptRejectButtons();
  }
}

/**
 * Start the process of presenting emails to the user
 */
function startAssign() {
  const assignStartResetButtonElement =
      document.getElementById('assignStartResetButton');
  assignStartResetButtonElement
      .querySelector('.button-circle__ascii-icon')
      .innerText = '↻';
  assignStartResetButtonElement.removeEventListener('click', startAssign);
  assignStartResetButtonElement.addEventListener('click', restartAssign);

  const assignStartResetTextElement =
      document.getElementById('assignStartResetText');
  assignStartResetTextElement.innerText = 'Click to Restart';

  enableAssignAcceptRejectButtons();

  assignDisplayNextEmail();
}

/**
 * Restart the process of presenting emails to the user. Disable the
 * start button while the messages load
 */
function restartAssign() {
  const assignStartResetButtonElement =
      document.getElementById('assignStartResetButton');
  assignStartResetButtonElement
      .querySelector('.button-circle__ascii-icon')
      .innerText = '▶';
  assignStartResetButtonElement
      .removeEventListener('click', restartAssign);
  assignStartResetButtonElement.addEventListener('click', startAssign);

  const assignStartResetTextElement =
      document.getElementById('assignStartResetText');
  assignStartResetTextElement.innerText = 'Click to Start';

  const assignSuspectedActionItemsElement =
      document.getElementById('assignSuspectedActionItems');
  assignSuspectedActionItemsElement.innerText = '-';

  disableAssignAcceptRejectButtons();
  disableAssignStartResetButton();

  setUpAssign();
}

/**
 * Enable the accept and reject buttons for the assign panel.
 * Done when there are emails to assign and the user has pressed play,
 * or the user is toggling settings.
 */
function enableAssignAcceptRejectButtons() {
  const assignAcceptButtonElement =
      document.getElementById('assign-accept-button');
  const assignRejectButtonElement =
      document.getElementById('assign-reject-button');
  assignAcceptButtonElement.classList.remove('u-button-disable');
  assignRejectButtonElement.classList.remove('u-button-disable');
}

/**
 * Disable the accept and reject buttons for the assign panel.
 * Done when there are no emails left to assign, or the user has not yet
 * pressed play
 */
function disableAssignAcceptRejectButtons() {
  const assignAcceptButtonElement =
      document.getElementById('assign-accept-button');
  const assignRejectButtonElement =
      document.getElementById('assign-reject-button');
  assignAcceptButtonElement.classList.add('u-button-disable');
  assignRejectButtonElement.classList.add('u-button-disable');
}

/**
 * Disable the start button for the assign panel. Done while the messages
 * are loading
 */
function enableAssignStartResetButton() {
  const assignStartResetButtonElement =
      document.getElementById('assignStartResetButton');
  assignStartResetButtonElement.classList.remove('u-button-disable');
}

/**
 * Disable the start button for the assign panel. Done when the messages load
 */
function disableAssignStartResetButton() {
  const assignStartResetButtonElement =
      document.getElementById('assignStartResetButton');
  assignStartResetButtonElement.classList.add('u-button-disable');
}

