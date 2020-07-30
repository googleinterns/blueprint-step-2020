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

  private final Date startDate;
  private List<Long> workHoursPerDay;
  private List<Long> personalHoursPerDay;
  private List<DatePair> morningFreeInterval;
  private List<DatePair> workFreeInterval;
  private List<DatePair> eveningFreeInterval;
  private static final long HOUR_MILLI = TimeUnit.HOURS.toMillis(1);
  private static final long DAY_MILLI = TimeUnit.DAYS.toMillis(1);
  private static final long PERSONAL_BEGIN = 7 * HOUR_MILLI;
  private static final long WORK_BEGIN = 10 * HOUR_MILLI;
  private static final long WORK_END = 18 * HOUR_MILLI;
  private static final long PERSONAL_END = 23 * HOUR_MILLI;
  private static final int OFFSET = 7;

  /**
   * Initialize the class with the start day. The work hours are harrrd-coded between 10 AM and 6
   * PM. The rest of the free hours are between 7 AM and 11 PM.
   *
   * @param startTime parameter that gives the time of the start/now.
   */
  public FreeTimeUtility(Date startDate) {
    this.startDate = startDate;
    Date basisDate = new Date(startDate.getYear(), startDate.getMonth(), startDate.getDate(), 0, 0);

    this.morningFreeInterval = new ArrayList<>();
    this.workFreeInterval = new ArrayList<>();
    this.eveningFreeInterval = new ArrayList<>();

    for (int day = 0; day < 5; day++) {
      Date workStart = new Date(basisDate.getTime() + day * DAY_MILLI + WORK_BEGIN);
      Date workEnd = new Date(basisDate.getTime() + day * DAY_MILLI + WORK_END);
      Date personalStart = new Date(basisDate.getTime() + day * DAY_MILLI + PERSONAL_BEGIN);
      Date personalEnd = new Date(basisDate.getTime() + day * DAY_MILLI + PERSONAL_END);
      morningFreeInterval.add(new DatePair(personalStart, workStart));
      workFreeInterval.add(new DatePair(workStart, workEnd));
      eveningFreeInterval.add(new DatePair(workEnd, personalEnd));
    }
    addEvent(basisDate, startDate);
  }

  /**
   * Change the free intervals based on the new event added.
   *
   * @param evenStart parameter that gives start time.
   * @param eventEnd parameter that gives end tome of the event
   */
  public void addEvent(Date eventStart, Date eventEnd) {
    this.morningFreeInterval = updateInterval(this.morningFreeInterval, eventStart, eventEnd);
    this.workFreeInterval = updateInterval(this.workFreeInterval, eventStart, eventEnd);
    this.eveningFreeInterval = updateInterval(this.eveningFreeInterval, eventStart, eventEnd);
  }

  /**
   * Method to update the free intervals in a particular list of intervals.
   *
   * @param freeTime the list of intervals to update.
   * @param eventStart the start time of the new event
   * @param eventEnd the end time of the new event
   * @return the new list of intervals
   */
  private List<DatePair> updateInterval(List<DatePair> freeTime, Date eventStart, Date eventEnd) {
    List<DatePair> updatedTime = new ArrayList<>();
    for (DatePair interval : freeTime) {
      if (interval.getKey().before(eventStart) && interval.getValue().after(eventEnd)) {
        updatedTime.add(new DatePair(interval.getKey(), eventStart));
        updatedTime.add(new DatePair(eventEnd, interval.getValue()));
      } else if (interval.getKey().before(eventStart) && interval.getValue().after(eventStart)) {
        updatedTime.add(new DatePair(interval.getKey(), eventStart));
      } else if (interval.getKey().before(eventEnd) && interval.getValue().after(eventEnd)) {
        updatedTime.add(new DatePair(eventEnd, interval.getValue()));
      } else if (!(eventStart.before(interval.getKey()) && eventEnd.after(interval.getValue()))) {
        updatedTime.add(interval);
      }
    }
    return updatedTime;
  }

  /** Method to calculate the free time for each day based on the free intervals. */
  private void updateFreeTime() {
    long zero = 0;
    this.workHoursPerDay = Arrays.asList(zero, zero, zero, zero, zero);
    this.personalHoursPerDay = Arrays.asList(zero, zero, zero, zero, zero);
    this.workHoursPerDay = updateHoursPerDay(this.workHoursPerDay, this.workFreeInterval);
    this.personalHoursPerDay =
        updateHoursPerDay(this.personalHoursPerDay, this.morningFreeInterval);
    this.personalHoursPerDay =
        updateHoursPerDay(this.personalHoursPerDay, this.eveningFreeInterval);
  }

  /**
   * Method to update the free time on a particular list of free time based on a particular list of
   * intervals
   *
   * @param hoursPerDay the list of free time in milliseconds to update.
   * @param freeInterval the list of free intervals
   * @return the updated list of free time
   */
  private List<Long> updateHoursPerDay(List<Long> hoursPerDay, List<DatePair> freeInterval) {
    for (DatePair interval : freeInterval) {
      int index = interval.getKey().getDay() - this.startDate.getDay();
      index = index < 0 ? index + OFFSET : index;
      hoursPerDay.set(
          index,
          hoursPerDay.get(index) + interval.getValue().getTime() - interval.getKey().getTime());
    }
    return hoursPerDay;
  }

  /**
   * Method to create and return the calendar data response.
   *
   * @return the calendar data response to be sent as servlet response
   */
  public CalendarDataResponse getCalendarDataResponse() {
    updateFreeTime();
    return new CalendarDataResponse(
        this.startDate.getDay() - 1, this.workHoursPerDay, this.personalHoursPerDay);
  }

  public List<DatePair> getWorkFreeInterval() {
    return this.workFreeInterval;
  }
}
