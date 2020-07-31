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

// Script for handling the behaviour of the panels' layout

// Add appropriate event listeners to all toggle icons
document.querySelectorAll('.panel__toggle-icon')
    .forEach((element) => {
      switch (element.id) {
        case 'assignSettingsIcon':
          element
              .addEventListener(
                  'click',
                  (event) => displayAssignSettings(event)
              );
          break;
        default:
          element
              .addEventListener(
                  'click',
                  (event) => toggleIconHandler(event)
              );
      }
    });

/**
 * Changes icon from clicked to un-clicked, and vice versa.
 * Assumes parent div has two img elements, one hidden and one not.
 *
 * @param {Event} event event generated from click
 */
function toggleIconHandler(event) {
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

/**
 * Will increase a the integer value of a text element up to a
 * specified max.
 *
 * @param {string} elementId the ID of the element containing the integer value
 * @param {number} max the maximum integer the text element should increase to.
 *     30 by default
 */
function incrementElement(elementId, max = 30) {
  const targetElement = document.getElementById(elementId);
  const currentValue = parseInt(targetElement.innerText);

  targetElement.innerText = Math.min(max, currentValue + 1);
}

/**
 * Will decrease a the integer value of a text element down to a
 * specified minimum.
 *
 * @param {string} elementId the ID of the element containing the integer value
 * @param {number} min the minimum integer the text element should decrease to.
 *     1 by default
 */
function decrementElement(elementId, min = 1) {
  const targetElement = document.getElementById(elementId);
  const currentValue = parseInt(targetElement.innerText);

  targetElement.innerText = Math.max(min, currentValue - 1);
}

/**
 * Add a panel__content-entry-list entry to a list with text and a
 * remove button (to delete the entry)
 *
 * @param {string} inputElementId the id of the input field
 * @param {string} listElementId the id of the list that contains the entries
 */
function createTextListElement(inputElementId, listElementId) {
  const inputElement = document.getElementById(inputElementId);
  const phrase = inputElement.value;

  if (phrase === null || phrase.length === 0) {
    return;
  }

  const list = document.getElementById(listElementId);

  const listEntry = document.createElement('li');
  listEntry.className = 'panel__content-entry--list';

  const listEntryText = document.createElement('p');
  listEntryText.className = 'panel__list-text';

  const listEntryTextNode = document.createTextNode(phrase);
  listEntryText.appendChild(listEntryTextNode);

  const panelButton = document.createElement('a');
  panelButton.className = 'panel__button-incremental--small u-right-align';
  panelButton
      .addEventListener('click', (event) => removeTextListElement(event));

  const panelButtonSpan = document.createElement('span');
  const panelButtonSpanTextNode = document.createTextNode('-');
  panelButtonSpan.appendChild(panelButtonSpanTextNode);
  panelButton.appendChild(panelButtonSpan);

  listEntry.appendChild(listEntryText);
  listEntry.appendChild(panelButton);

  list.appendChild(listEntry);
}

/**
 * Click event handler for the remove button in a list entry. Deletes the entry
 *
 * @param {Event} event event generated from click
 */
function removeTextListElement(event) {
  const targetElement = event.currentTarget;
  const parentListEntry = targetElement.parentNode;

  parentListEntry.remove();
}

/**
 * Shows (i.e. un-hides) all elements with a certain class name.
 *
 * @param {string} className the class name to unhide
 */
function show(className) {
  const elements = document.querySelectorAll(`.${className}`);

  elements.forEach((element) => {
    element.removeAttribute('hidden');
  });
}

/**
 * Hides all elements with a certain class name.
 *
 * @param {string} className the class name to hide
 */
function hide(className) {
  const elements = document.querySelectorAll(`.${className}`);

  elements.forEach((element) => {
    element.setAttribute('hidden', '');
  });
}
