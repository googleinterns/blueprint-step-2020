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

package com.google.sps.utility;

import com.google.sps.data.CalendarDataResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Class containing the user's free hours. */
public final class FreeTimeUtility {

  private final long startDay;
  private final List<Long> workHoursPerDay;
  private final List<Long> personalHoursPerDay;
  private final long numMillisecondsDay = TimeUnit.HOURS.toMillis(24);
  private final long Hour = TimeUnit.HOURS.toMillis(1);
  private final int UnixEpochDayShift = 3;
  private final long personalBegin = 7 * Hour;
  private final long workBegin = 10 * Hour;
  private final long workEnd = 18 * Hour;
  private final long personalEnd = 23 * Hour;

  /**
   * Initialize the class by calculating the start day. The free hours should be eight hours for the
   * working hours and for the pesonal time initially. Adjust the free hours on the first day to
   * account for the fact that there are hours that have already passed
   *
   * @param startTime parameter of type long that gives the Unix epoch time of the start/now. The
   *     timezone is UTC
   */
  public FreeTimeUtility(long startTime) {
    long numDays = startTime / numMillisecondsDay;
    this.startDay = (numDays + UnixEpochDayShift) % 7;
    long eightHours = 8 * Hour;
    this.workHoursPerDay =
        Arrays.asList(eightHours, eightHours, eightHours, eightHours, eightHours);
    this.personalHoursPerDay =
        Arrays.asList(eightHours, eightHours, eightHours, eightHours, eightHours);
    setEndDayEvent(0, startTime % numMillisecondsDay);
  }

  /**
   * Substract the event durations from the free time. The working hours are hard-coded as beign
   * between 10:00 AM and 6:00 PM. The personal time is between 7:00 AM and 11:00 PM, without
   * accounting for the work period in there. All time periods are in UTC timezone. TODO: Refactor
   * the logic of this function to account for edge cases like multiple events being created at the
   * same time. (Issue #104)
   *
   * @param startTime parameter of type long that gives the Unix epoch time of the start of the
   *     event
   * @param endTime parameter of type long that gives the Unix epoch time of the end of the event
   */
  public void addEvent(long startTime, long endTime) {
    long numDaysBegin = startTime / numMillisecondsDay;
    long numDaysEnd = endTime / numMillisecondsDay;
    long beginDay = (numDaysBegin + UnixEpochDayShift) % 7;
    long endDay = (numDaysEnd + UnixEpochDayShift) % 7;
    long startHour = startTime % numMillisecondsDay;
    long endHour = endTime % numMillisecondsDay;
    int indexBegin = (int) (beginDay - startDay);
    int indexEnd = (int) (endDay - startDay);
    for (int index = indexBegin; index <= Math.min(5, indexEnd); index++) {
      if (index == indexBegin && index == indexEnd) {
        setSameDayEvent(index, startHour, endHour);
      } else if (index == indexBegin) {
        setStartDayEvent(index, startHour);
      } else if (index == indexEnd) {
        setEndDayEvent(index, endHour);
      } else {
        setAllDayEvent(index);
      }
    }
  }

  private void setSameDayEvent(int index, long startHour, long endHour) {
    if (startHour < personalBegin && endHour >= personalEnd) {
      // Case where we have an all day event
      setAllDayEvent(index);
    } else if (startHour < personalBegin && endHour < personalEnd) {
      // Case where the event starts before the personal time
      setEndDayEvent(index, endHour);
    } else if (startHour >= personalBegin && endHour >= personalEnd) {
      // Case where the event ends after the personal time
      setStartDayEvent(index, startHour);
    } else {
      // Case where the entire event is within the personal and work time
      long morningStart = Math.min(workBegin, startHour);
      long morningEnd = Math.min(workBegin, endHour);
      long eveningStart = Math.max(workEnd, startHour);
      long eveningEnd = Math.max(workEnd, endHour);
      long duration = endHour - startHour;
      long personalDuration = (morningEnd - morningStart) + (eveningEnd - eveningStart);
      long workDuration = duration - personalDuration;
      personalHoursPerDay.set(
          index, Math.max(personalHoursPerDay.get(index) - personalDuration, 0));
      workHoursPerDay.set(index, Math.max(workHoursPerDay.get(index) - workDuration, 0));
    }
  }

  private void setAllDayEvent(int index) {
    personalHoursPerDay.set(index, (long) 0);
    workHoursPerDay.set(index, (long) 0);
  }

  private void setStartDayEvent(int index, long startHour) {
    if (startHour < personalBegin) {
      // This case is similar as an all day event
      setAllDayEvent(index);
    } else if (startHour < workBegin) {
      // This means only a portion of the morning personal time is free
      long morningDuration = workBegin - startHour;
      long eveningDuration = 5 * Hour;
      long personalDuration = morningDuration + eveningDuration;
      workHoursPerDay.set(index, (long) 0);
      personalHoursPerDay.set(
          index, Math.max(personalHoursPerDay.get(index) - personalDuration, 0));
    } else if (startHour < workEnd) {
      // Case where part of the work hours and the morning personal time are free
      long workDuration = workEnd - startHour;
      long personalDuration = 5 * Hour;
      workHoursPerDay.set(index, Math.max(workHoursPerDay.get(index) - workDuration, 0));
      personalHoursPerDay.set(
          index, Math.max(personalHoursPerDay.get(index) - personalDuration, 0));
    } else if (startHour < personalEnd) {
      // Case where only part of the personal hours in the evening are occupied
      long personalDuration = personalEnd - startHour;
      personalHoursPerDay.set(
          index, Math.max(personalHoursPerDay.get(index) - personalDuration, 0));
    }
  }

  private void setEndDayEvent(int index, long endHour) {
    if (endHour >= personalEnd) {
      // This case is similar to an all day event
      setAllDayEvent(index);
    } else if (endHour >= workEnd) {
      // The entire day is occupied except for a portion of the evening
      long morningDuration = 3 * Hour;
      long eveningDuration = endHour - workEnd;
      long personalDuration = morningDuration + eveningDuration;
      workHoursPerDay.set(index, (long) 0);
      personalHoursPerDay.set(
          index, Math.max(personalHoursPerDay.get(index) - personalDuration, 0));
    } else if (endHour >= workBegin) {
      // The morning and part of the work hours are occupied
      long personalDuration = 3 * Hour;
      long workDuration = endHour - workBegin;
      workHoursPerDay.set(index, Math.max(workHoursPerDay.get(index) - workDuration, 0));
      personalHoursPerDay.set(
          index, Math.max(personalHoursPerDay.get(index) - personalDuration, 0));
    } else if (endHour >= personalBegin) {
      // Only part of the morning is occupied
      long personalDuration = endHour - personalBegin;
      personalHoursPerDay.set(
          index, Math.max(personalHoursPerDay.get(index) - personalDuration, 0));
    }
  }

  public CalendarDataResponse getCalendarDataResponse() {
    return new CalendarDataResponse(this.startDay, this.workHoursPerDay, this.personalHoursPerDay);
  }
}
