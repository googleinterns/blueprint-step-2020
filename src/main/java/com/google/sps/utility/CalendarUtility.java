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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.io.IOException;
import java.util.List;

public class CalendarUtility {
  
  private CalendarUtility() {}

  public static Calendar getCalendarService(Credential credential) {
    HttpTransport HTTP_TRANSPORT = AuthenticationUtility.getAppEngineTransport();
    JsonFactory JSON_FACTORY = AuthenticationUtility.getJsonFactory();

    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
  }

  public static List<Event> getCalendarEvents(Calendar calendarService) throws IOException {
    return calendarService.events().list("primary").setPageToken(null).execute().getItems();
  }
}
