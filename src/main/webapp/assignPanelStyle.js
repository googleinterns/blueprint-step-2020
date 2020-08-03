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
function displayAssignSettings() {
  show('assign-settings');
  hide('assign-content');

  const acceptButton = document.getElementById('assign-accept-button');
  const rejectButton = document.getElementById('assign-reject-button');

  acceptButton.innerText = 'Confirm';
  rejectButton.innerText = 'Reset';
}

/**
 * Display the content of the Assign panel (and hide the settings)
 */
function displayAssignContent() {
  show('assign-content');
  hide('assign-settings');

  const acceptButton = document.getElementById('assign-accept-button');
  const rejectButton = document.getElementById('assign-reject-button');

  acceptButton.innerText = 'Add Task';
  rejectButton.innerText = 'Skip Item';
}

