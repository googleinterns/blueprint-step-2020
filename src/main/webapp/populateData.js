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

// Script to handle populating data in the panels

/* eslint-disable no-unused-vars */

/**
 * Function to populate Gmail container with API response
 */
function populateGmail() {
  const gmailContainer = document.querySelector('#gmail');
  fetch('/gmail')
      .then((response) => response.json())
      .then((emailList) => {
        console.log(emailList);
        const emails =
            emailList.map((a) => a.id).reduce((a, b) => a + '\n' + b);
        gmailContainer.innerText = emails;
      });
}

