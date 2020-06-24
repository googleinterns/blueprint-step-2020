// Copyright 2019 Google LLC
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

// This file contains functionality relating to Google Sign in and storing cookies
// TODO: Manage User Sign In State

// OAUTH 2.0 Client ID
// TODO: Add Client ID once GCP project is made
const CLIENT_ID =
    'INPUT_CLIENT_ID_HERE';

// Function called when script https://apis.google.com/js/platform.js loads
// Renders the sign in button and stores clientID as cookie.
function init() {
  gapi.load('auth2', () => {
    gapi.auth2.init({
      'client_id': CLIENT_ID
    }).then(() => {
      document.cookie = 'clientId=' + CLIENT_ID;
      renderButton();
    });
  });
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
  document.cookie = 'idToken=' + idToken + '; expires=' + expiryUtcTime;

  // Set cookie that contains idToken to authenticate the user
  // Will automatically delete when the access token expires
  // or when the browser is closed
  document.cookie = 'accessToken=' + accessToken + '; expires=' + expiryUtcTime;
}

/**
 * Signs out of Google Account
 */
function signOut() {
  let auth2 = gapi.auth2.getAuthInstance();
  auth2.signOut().then(function () {
    console.log('User signed out.');
  });
}

/**
 * Function to render a UI element included in the Google Sign-In for websites
 * package. See documentation:
 * https://developers.google.com/identity/sign-in/web/reference#gapisignin2renderid_options
 * Request readonly access to Gmail, Tasks, and Calendar
 */
function renderButton() {
  gapi.signin2.render('google-sign-in-btn',
  {
    'scope': 'https://www.googleapis.com/auth/gmail.readonly ' +
        'https://www.googleapis.com/auth/calendar.readonly ' +
        'https://www.googleapis.com/auth/tasks.readonly',
    'width': 240,
    'height': 40,
    'longtitle': true,
    'theme': 'dark',
    'onsuccess': onSignIn,
    'onfailure': () => {}
  });
}

