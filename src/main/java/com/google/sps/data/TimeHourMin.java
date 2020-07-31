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

package com.google.sps.data;

import java.util.*;

/** Class containing the duration in hours and minutes to be sent as a response. */
public final class TimeHourMin {

  private final int hours;
  private final int minutes;

  /**
   * Initialize the class with all the parameters required.
   *
   * @param hours parameter that gives the number of hours in the duration
   * @param minutes parameter that gives the number of minutes in the duration
   */
  public TimeHourMin(
      int hours, int minutes) {
    this.hours = hours;
    this.minutes = minutes;
  }

  public int getHours() {
    return hours;
  }

  public int getMinutes() {
    return minutes;
  }
}
