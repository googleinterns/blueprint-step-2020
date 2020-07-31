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

// Script for handling the behaviour of the panels' layout

document.querySelectorAll('.panel__toggle-icon')
    .forEach((element) => {
      switch (element.id) {
        case 'assignSettingsIcon':
          element.addEventListener('click', (event) => displayAssignSettings(event));
          break;
        default:
          element.addEventListener('click', (event) => toggleIconHandler(event));
      }
    });

/**
 * Changes icon from clicked to un-clicked, and vice versa.
 * Assumes parent div has two img elements, one hidden and one not.
 * TODO: Something broken here.
 * @param {Event} event event generated from click
 */
function toggleIconHandler (event) {
  const currentIcon = event.target;
  const currentDiv = currentIcon.parentNode;
  const newIcon = currentDiv.querySelector('img[hidden]');

  currentIcon.setAttribute('hidden', '');
  newIcon.removeAttribute('hidden');
}

/**
 * Shows/hides the settings display in the assign panel
 *
 * @param {Event} event event generated from click
 */
function displayAssignSettings(event) {
  // Change icon appearance
  toggleIconHandler(event);

  // TODO: show/hide the settings display
}

function incrementElement(elementId, max = 30) {
  const targetElement = document.getElementById(elementId);
  const currentValue = parseInt(targetElement.innerText);

  targetElement.innerText = Math.min(max, currentValue + 1);
}

function decrementElement(elementId, min = 1) {
  const targetElement = document.getElementById(elementId);
  const currentValue = parseInt(targetElement.innerText);

  targetElement.innerText = Math.max(min, currentValue - 1);
}

function createTextListElement(inputElementId, listElementId) {
  const inputElement = document.getElementById(inputElementId);
  const phrase = inputElement.value;

  const list = document.getElementById(listElementId);

  const listEntry = document.createElement('li');
  listEntry.className = 'panel__content-entry--list';

  const listEntryText = document.createElement('p');
  listEntryText.className = 'panel__list-text';

  const listEntryTextNode = document.createTextNode(phrase);
  listEntryText.appendChild(listEntryTextNode);

  const panelButton = document.createElement('a');
  panelButton.className = "panel__button-incremental--small u-right-align";
  panelButton.addEventListener('click', (event) => removeTextListElement(event));

  const panelButtonSpan = document.createElement('span');
  const panelButtonSpanTextNode = document.createTextNode('-');
  panelButtonSpan.appendChild(panelButtonSpanTextNode);
  panelButton.appendChild(panelButtonSpan);

  listEntry.appendChild(listEntryText);
  listEntry.appendChild(panelButton);

  list.appendChild(listEntry);
}

function removeTextListElement(event) {
  const targetElement = event.currentTarget;
  const parentListEntry = targetElement.parentNode;

  parentListEntry.remove();
}

function show(classId) {
  const elements = document.querySelectorAll(`.${classId}`);

  elements.forEach((element) => {
    element.removeAttribute('hidden');
  })
}

function hide(classId) {
  const elements = document.querySelectorAll(`.${classId}`);

  elements.forEach((element) => {
    element.setAttribute('hidden', '');
  })
}
