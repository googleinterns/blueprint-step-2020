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

/** Class containing the user's free hours. */
public final class CalendarClientData {

  private final int startDay;
  private final List<Integer> workHours;
  private final List<Integer> personalHours;
  private final long numMillisecondsDay = 24 * 60 * 60 * 1000;
  private final int Hour = 60 * 60 * 1000;

  /**
   * Initialize the class by calculating the start day. The free hours should be eight hours for the
   * working hours and sixteen hours for the pesonal time initially.
   *
   * @param startTime parameter of type long that gives the Unix epoch time of the start/now. The
   *     timezone is UTC
   */
  public CalendarClientData(long startTime) {
    int numDays = (int) Math.floor(startTime / numMillisecondsDay);
    this.startDay = ((numDays + 3) % 7);
    int eightHours = 8 * Hour;
    int sixteenHours = 16 * Hour;
    this.workHours =
        new ArrayList<>(Arrays.asList(eightHours, eightHours, eightHours, eightHours, eightHours));
    this.personalHours =
        new ArrayList<>(
            Arrays.asList(sixteenHours, sixteenHours, sixteenHours, sixteenHours, sixteenHours));
  }

  /**
   * Substract the event durations from the free time. Whether an event is in the personal or work
   * time is determined by its start period. The working hours are hard-coded as beign between 10:00
   * AM and 6:00 PM. All time periods are in UTC timezone. TODO: Refactor the logic of this function
   * to account for edge cases like multiple events being created at the same time. (Issue #104)
   *
   * @param startTime parameter of type long that gives the Unix epoch time of the start of the
   *     event
   * @param endTime parameter of type long that gives the Unix epoch time of the end of the event
   */
  public void addEvent(long startTime, long endTime) {
    int numDays = (int) Math.floor(startTime / numMillisecondsDay);
    int beginDay = ((numDays + 3) % 7);
    int duration = (int) (endTime - startTime);
    int startHour = (int) (startTime % numMillisecondsDay);
    int workBegin = 10 * Hour;
    int workEnd = 18 * Hour;
    if (startHour < workBegin || startHour > workEnd) {
      personalHours.set(
          beginDay - startDay, Math.max(personalHours.get(beginDay - startDay) - duration, 0));
    } else {
      workHours.set(
          beginDay - startDay, Math.max(workHours.get(beginDay - startDay) - duration, 0));
    }
  }

  public int getStartDay() {
    // Useful for testing purposes
    return startDay;
  }

  public List<Integer> getWorkHours() {
    // Useful for testing purposes
    return workHours;
  }

  public List<Integer> getPersonalHours() {
    // Useful for testing purposes
    return personalHours;
  }
}
