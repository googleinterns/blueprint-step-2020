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

/** Class containing the response to be converted to Json. */
public final class CalendarSummaryResponse {

  /**This variable is an integer between 0 and 6 representing the current day (from sunday to saturday) */
  final int startDay;
  final List<TimeHourMin> workTimeFree;
  final List<TimeHourMin> personalTimeFree;

  /**
   * Initialize the class with all the parameters required.
   *
   * @param startDay parameter that gives start day
   * @param workTimeFree parameter that specifies the free work hours in the next five days
   * @param personalTimeFree parameter that specifies the free personal time
   */
  public CalendarSummaryResponse(
      int startDay, List<TimeHourMin> workTimeFree, List<TimeHourMin> personalTimeFree) {
    this.startDay = startDay;
    this.workTimeFree = workTimeFree;
    this.personalTimeFree = personalTimeFree;
  }

  public int getStartDay() {
    return startDay;
  }

  public List<TimeHourMin> getWorkTimeFree() {
    return workTimeFree;
  }

  public List<TimeHourMin> getPersonalTimeFree() {
    return personalTimeFree;
  }
}
