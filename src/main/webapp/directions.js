directions.js
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

/* exported getDirections */

/**
 * Gets key information from each leg and displays them on the DOM.
 */
function getDirections() {
  fetch('/directions')
      .then((response) => (response.json()))
      .then((json) => {
        let text = '';
        json.map.routes.myArrayList.forEach((route) => {
          route.map.legs.myArrayList.forEach((leg) => {
            const DURATION = leg.map.duration.map.text;
            const DISTANCE = leg.map.distance.map.text;
            const START_ADDRESS = leg.map.start_address;
            const END_ADDRESS = leg.map.end_address;
            const LEG_HTML = `Duration: ${DURATION}
              Distance: ${DISTANCE}
              Start: ${START_ADDRESS}
              End: ${END_ADDRESS}`;
            text += LEG_HTML;
          });
        });
        document.getElementById('directions-container').innerHTML = text;
      });
}
