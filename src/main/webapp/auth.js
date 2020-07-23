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

// This file contains functionality relating to Google Sign in
// and storing cookies

/* eslint-disable no-unused-vars */
/* global gapi, addCookie, isCookiePresent,
deleteCookie, populateGmail, populateTasks,
populateCalendar, populateGo */
// TODO: Handle CommonJS (Issue #31)

/**
 * Function called when script https://apis.google.com/js/platform.js loads
 * Initializes the Google Sign-In with a client ID hosted on the server
 * and renders the button on the page
 */
function init() {
  gapi.load('auth2', () => {
    fetch('/client-id')
        .then((response) => response.json())
        .then((clientIdObject) => {
          const clientId = clientIdObject['client-id'];

          // Initialize Google Sign-in with the clientID
          gapi.auth2.init({
            'client_id': clientId,
          }).then(() => {
            handleAuthenticationState();
            renderButton();
          });
        });
  });
}

/**
 * Will initialize the page based on the current authentication state
 */
function handleAuthenticationState() {
  const featureContainer = document.querySelector('.feature-container');
  const signInButton = document.querySelector('#google-sign-in-btn');
  if (isCookiePresent('idToken')) {
    // User is logged in.
    // Hide sign in button, show features
    signInButton.setAttribute('hidden', '');
    featureContainer.removeAttribute('hidden');

    // Populate information panels at top of dashboard
    populateGmail();
    populateTasks();
    populateCalendar();

    // Populate magic feature panels at the bottom of the dashboard
    //populateGo();
  } else {
    // User is not logged in.
    // Show sign in button, hide features
    signInButton.removeAttribute('hidden');
    featureContainer.setAttribute('hidden', '');
  }
}

/**
 * Called when user signs in using rendered sign in button
 * @param {Object} googleUser object that contains information about
 *     authenticated user.
 */
function onSignIn(googleUser) {
  // Get the authentication object. Always include accessID, even if null
  const userAuth = googleUser.getAuthResponse(true);

  const idToken = userAuth.id_token;
  const accessToken = userAuth.access_token;
  const expiry = userAuth.expires_at;
  const expiryUtcTime = new Date(expiry).toUTCString();

  // Set cookie that contains idToken to authenticate the user
  // Will automatically delete when the access token expires
  // or when the browser is closed
  addCookie('idToken', idToken, expiryUtcTime);

  // Set cookie that contains idToken to authenticate the user
  // Will automatically delete when the access token expires
  // or when the browser is closed
  addCookie('accessToken', accessToken, expiryUtcTime);

  handleAuthenticationState();
}

/**
 * Signs out of Google Account
 */
function signOut() {
  const auth2 = gapi.auth2.getAuthInstance();
  auth2.signOut().then(() => {
    // Remove Authentication Cookies
    deleteCookie('idToken');
    deleteCookie('accessToken');

    handleAuthenticationState();
  });
}

/**
 * Function to render a UI element included in the Google Sign-In for websites
 * package. See documentation:
 * https://developers.google.com/identity/sign-in/web/reference#gapisignin2renderid_options
 * Request readonly access to Gmail, Tasks, and Calendar
 */
function renderButton() {
  gapi.signin2.render('google-sign-in-btn', {
    'scope': 'https://www.googleapis.com/auth/gmail.readonly ' +
        'https://www.googleapis.com/auth/calendar ' +
        'https://www.googleapis.com/auth/tasks.readonly',
    'width': 240,
    'height': 40,
    'longtitle': true,
    'theme': 'dark',
    'onsuccess': onSignIn,
    'onfailure': () => {},
  });
}

/**
 * Custom error type to handle cases where user is not authenticated
 */
class AuthenticationError extends Error {}
