// OAUTH 2.0 Client ID
const CLIENT_ID =
    '196395003919-thc20nqbuukhnmsm1v12o7snh2od7jco.apps.googleusercontent.com';

// Function called when script https://apis.google.com/js/platform.js loads
function init() {
  gapi.load('auth2', function() {
    console.log("Hey");
    gapi.auth2.init({
      'client_id': CLIENT_ID
    }).then(renderButton);
  });
}

/**
 * Called when user signs in using rendered sign in button
 * @param googleUser object that contains information about authenticated user.
 *     Email is null if the email scope is not present.
 */
function onSignIn(googleUser) {
  var profile = googleUser.getBasicProfile();
  console.log('ID: ' + profile.getId()); // Do not send to your backend! Use an ID token instead.
  console.log('Name: ' + profile.getName());
  console.log('Image URL: ' + profile.getImageUrl());
  console.log('Email: ' + profile.getEmail()); // This is null if the 'email' scope is not present.
}

/**
 * Called when user signs out
 * Copied from https://developers.google.com/identity/sign-in/web/sign-in for
 * testing purposes
 */
function signOut() {
  var auth2 = gapi.auth2.getAuthInstance();
  auth2.signOut().then(function () {
    console.log('User signed out.');
  });
}

/**
 * Function to render a UI element included in the Google Sign-In for websites
 * package. See documentation:
 * https://developers.google.com/identity/sign-in/web/reference#gapisignin2renderid_options
 */
function renderButton() {
  gapi.signin2.render('google-sign-in-btn',
  {
    'scope': 'profile',
    'width': 240,
    'height': 40,
    'longtitle': true,
    'theme': 'dark',
    'onsuccess': onSignIn,
    'onfailure': () => {}
  });
}