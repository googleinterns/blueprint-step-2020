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

// A series of utility functions that will be helpful throughout the front-end

/* eslint-disable no-unused-vars */
/* eslint-disable no-extend-native */

/**
 * Get cookie's value from document
 * @param {string} cookieName the name of the cookie that needs to be found
 * @return {string|null} either cookie's value or null if the cookie not found
 */
function getCookie(cookieName) {
  // Cookie strings are in the format of
  // cookieName1=cookieValue1; cookieName2=cookieValue2; ...
  const allCookies = document.cookie.split('; ');

  // Get the value of all matching cookies
  const matchingCookies = allCookies.filter((cookie) => {
    // Gets the name of the first cookie
    const retrievedCookieName = cookie.split('=')[0];
    return retrievedCookieName === cookieName;
  }).map((cookieString) => {
    // Extracts the value of each cookie
    return cookieString.split('=')[1];
  });

  // If cookie is not found, or the name is duplicated, return null
  if (matchingCookies.length !== 1) {
    if (matchingCookies > 1) {
      // This is unexpected behaviour
      console.log('Duplicate cookie!');
    }
    return null;
  }

  // Return the only cookie in the list (as expected)
  return matchingCookies[0];
}

/**
 * Checks if a cookie is present without returning its value
 * @param {string} cookieName the name of the cookie that needs to be found
 * @return {boolean} true if present, false otherwise
 */
function isCookiePresent(cookieName) {
  return getCookie(cookieName) !== null;
}

/**
 * Adds a cookie to the document with the specified value
 * @param {string} cookieName the name of the cookie to be added
 * @param {string} cookieValue the value of the cookie
 * @param {string} expiryUtcTime the time the cookie expires in UTC time
 */
function addCookie(cookieName, cookieValue, expiryUtcTime) {
  document.cookie = `${cookieName}=${cookieValue}; expires=${expiryUtcTime};`;
}

/**
 * Deletes cookie from document
 * @param {string} cookieName name of cookie to be deleted
 */
function deleteCookie(cookieName) {
  // Set time to 1 second past EPOCH time.
  // Will prompt cookie to immediately expire
  document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:01 GMT;`;
}

/**
 * Gets a Date object with the time from the user's current time zone.
 * @return {Date} Date object with local time
 */
Date.prototype.getDateObjectWithLocalTime = function() {
  const dateObject = new Date();
  const currentTime =
      dateObject.getTime() - dateObject.getTimezoneOffset()*60*1000;
  return new Date(currentTime);
};
