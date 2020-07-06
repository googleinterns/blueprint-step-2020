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

function populateGo() {
  fetch('/directions')
    .then((response) => (response.json()))
    .then((json) => {
      console.log(json);
      var html = "";
      json.map.routes.myArrayList.forEach(route => { 
        route.map.legs.myArrayList.forEach(leg => {
          var duration = leg.map.duration.map.text;
          var distance = leg.map.distance.map.text;
          var startAddress = leg.map.start_address;
          var endAddress = leg.map.end_address;
          var waypointOrder = route.map.waypoint_order.myArrayList.forEach(order => {
            waypointOrder += order + ", ";
          });
          legHtml = `<p>Duration: ${duration}<p>Distance: ${distance}<p>Start: ${startAddress}<p>End: ${endAddress}<p>Waypoint Order: ${waypointOrder}`;
          html += legHtml;
        })
      })
      document.getElementById('directions-container').innerHTML = html;
    });
}
