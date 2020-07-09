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
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.utility.AuthenticationUtility;
import com.google.sps.utility.CalendarUtility;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/calendar")
public class CalendarServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Credential googleCredential = AuthenticationUtility.getGoogleCredential(request);
    if (googleCredential == null) {
      response.sendError(403, AuthenticationUtility.ERROR_403);
      return;
    }
    Calendar calendarService = CalendarUtility.getCalendarService(googleCredential);
    List<Event> calendarEvents = CalendarUtility.getCalendarEvents(calendarService);

    // Convert event list to JSON and print to response
    Gson gson = new Gson();
    String eventJson = gson.toJson(calendarEvents);

    response.setContentType("application/json");
    response.getWriter().println(eventJson);
  }
}
