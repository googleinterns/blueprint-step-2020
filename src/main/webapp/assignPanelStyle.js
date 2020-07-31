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
/* global show, hide */

// Script for handling the behaviour of the Assign panel's layout

/**
 * Display the settings in the Assign panel (and hide the content)
 */
function displaySettings() {
  show('assign-settings');
  hide('assign-content');

  const acceptButton = document.getElementById('assign-accept-button');
  const rejectButton = document.getElementById('assign-reject-button');

  acceptButton.innerText = 'Confirm';
  rejectButton.innerText = 'Reset';

  acceptButton.addEventListener('click', setUpAssign);
  rejectButton.addEventListener('click', revertSettings);
  acceptButton.removeEventListener('click', addCurrentEmail);
  rejectButton.removeEventListener('click', skipCurrentEmail);
}

/**
 * Display the content of the Assign panel (and hide the settings)
 */
function displayContent() {
  show('assign-content');
  hide('assign-settings');

  const acceptButton = document.getElementById('assign-accept-button');
  const rejectButton = document.getElementById('assign-reject-button');

  acceptButton.innerText = 'Add Task';
  rejectButton.innerText = 'Skip Item';

  acceptButton.removeEventListener('click', setUpAssign);
  rejectButton.removeEventListener('click', revertSettings);
  acceptButton.addEventListener('click', addCurrentEmail);
  rejectButton.addEventListener('click', skipCurrentEmail);
}

function startAssign() {
  const assignStartResetButtonElement = document.getElementById('assignStartResetButton');
  assignStartResetButtonElement.querySelector('.button-circle__ascii-icon')
      .innerText = '↻';
  assignStartResetButtonElement.removeEventListener('click', startAssign);
  assignStartResetButtonElement.addEventListener('click', restartAssign);

  const assignStartResetTextElement = document.getElementById('assignStartResetText');
  assignStartResetTextElement.innerText = 'Click to Restart';

  enableAssignAcceptRejectButtons();

  displayNextEmail();
}

function restartAssign() {
  const assignStartResetButtonElement = document.getElementById('assignStartResetButton');
  assignStartResetButtonElement.querySelector('.button-circle__ascii-icon')
      .innerText = '▶';
  assignStartResetButtonElement.removeEventListener('click', restartAssign);
  assignStartResetButtonElement.addEventListener('click', startAssign);

  const assignStartResetTextElement = document.getElementById('assignStartResetText');
  assignStartResetTextElement.innerText = 'Click to Start';

  const assignSuspectedActionItemsElement = document.getElementById('assignSuspectedActionItems');
  assignSuspectedActionItemsElement.innerText = '-';

  disableAssignAcceptRejectButtons();
  disableAssignStartResetButton();

  setUpAssign();
}

function enableAssignAcceptRejectButtons() {
  const assignAcceptButtonElement = document.getElementById('assign-accept-button');
  const assignRejectButtonElement = document.getElementById('assign-reject-button');
  assignAcceptButtonElement.classList.remove('u-button-disable');
  assignRejectButtonElement.classList.remove('u-button-disable');
}

function disableAssignAcceptRejectButtons() {
  const assignAcceptButtonElement = document.getElementById('assign-accept-button');
  const assignRejectButtonElement = document.getElementById('assign-reject-button');
  assignAcceptButtonElement.classList.add('u-button-disable');
  assignRejectButtonElement.classList.add('u-button-disable');
}

function enableAssignStartResetButton() {
  const assignStartResetButtonElement =
      document.getElementById('assignStartResetButton');
  assignStartResetButtonElement.classList.remove('u-button-disable');
}

function disableAssignStartResetButton() {
  const assignStartResetButtonElement =
      document.getElementById('assignStartResetButton');
  assignStartResetButtonElement.classList.add('u-button-disable');
}

