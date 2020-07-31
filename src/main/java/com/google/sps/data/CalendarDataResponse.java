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
public final class CalendarDataResponse {

  final long startDay;
  final List<Long> workHoursPerDay;
  final List<Long> personalHoursPerDay;

  /**
   * Initialize the class with all the parameters required.
   *
   * @param startDay parameter of type long that gives start day
   * @param workHoursPerDay parameter that specifies the free work hours in the next five days
   * @param personalHoursPerDay parameter that specifies the free personal time
   */
  public CalendarDataResponse(
      long startDay, List<Long> workHoursPerDay, List<Long> personalHoursPerDay) {
    this.startDay = startDay;
    this.workHoursPerDay = workHoursPerDay;
    this.personalHoursPerDay = personalHoursPerDay;
  }

  public long getStartDay() {
    return startDay;
  }

  public List<Long> getWorkHoursPerDay() {
    return workHoursPerDay;
  }

  public List<Long> getPersonalHoursPerDay() {
    return personalHoursPerDay;
  }
}
