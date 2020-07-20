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

package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.CalendarClient;
import com.google.sps.model.CalendarClientFactory;
import com.google.sps.model.CalendarClientImpl;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET function responds JSON string containing events in user's calendar. */
@WebServlet("/calendar")
public class CalendarServlet extends AuthenticatedHttpServlet {
  private final CalendarClientFactory calendarClientFactory;

  /** Create servlet with default CalendarClient and Authentication Verifier implementations */
  public CalendarServlet() {
    calendarClientFactory = new CalendarClientImpl.Factory();
  }

  /**
   * Create servlet with explicit implementations of CalendarClient and AuthenticationVerifier
   *
   * @param authenticationVerifier implementation of AuthenticationVerifier
   * @param calendarClientFactory implementation of CalendarClientFactory
   */
  public CalendarServlet(
      AuthenticationVerifier authenticationVerifier, CalendarClientFactory calendarClientFactory) {
    super(authenticationVerifier);
    this.calendarClientFactory = calendarClientFactory;
  }

  /**
   * Returns List of events from the user's calendar
   *
   * @param request Http request from the client. Should contain idToken and accessToken
   * @param response 403 if user is not authenticated, or Json string with the user's events
   * @throws IOException if an issue arises while processing the request
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    assert googleCredential != null
        : "Null credentials (i.e. unauthenticated requests) should already be handled";

    CalendarClient calendarClient = calendarClientFactory.getCalendarClient(googleCredential);
    List<Event> calendarEvents = getEvents(calendarClient);

    // Convert event list to JSON and print to response
    Gson gson = new Gson();
    String eventJson = gson.toJson(calendarEvents);

    response.setContentType("application/json");
    response.getWriter().println(eventJson);
  }

  /**
   * Get the events in the user's calendars
   *
   * @param calendarClient either a mock CalendarClient or a calendarClient with a valid credential
   * @return List of Events from all of the user's calendars
   * @throws IOException if an issue occurs in the method
   */
  private List<Event> getEvents(CalendarClient calendarClient) throws IOException {
    List<CalendarListEntry> calendarList = calendarClient.getCalendarList();
    List<Event> events = new ArrayList<>();
    for (CalendarListEntry calendar : calendarList) {
      events.addAll(calendarClient.getCalendarEvents(calendar));
    }
    return events;
  }
}
