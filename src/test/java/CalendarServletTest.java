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

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;
import com.google.sps.model.CalendarClient;
import com.google.sps.model.CalendarClientFactory;
import com.google.sps.servlets.CalendarServlet;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Test Calendar Servlet responds to client with correctly parsed Events. */
@RunWith(JUnit4.class)
public final class CalendarServletTest extends AuthenticatedServletTestBase {
  private CalendarClientFactory calendarClientFactory;
  private CalendarClient calendarClient;
  private CalendarServlet servlet;

  private static final Gson gson = new Gson();

  private static final String EVENT_SUMMARY_ONE = "test event one";
  private static final String EVENT_SUMMARY_TWO = "test event two";
  private static final String EVENT_ONE_TWO_JSON =
      String.format(
          "[{\"summary\":\"%s\"},{\"summary\":\"%s\"}]", EVENT_SUMMARY_ONE, EVENT_SUMMARY_TWO);
  private static final String EVENT_ALL_JSON =
      String.format(
          "[{},{\"summary\":\"%s\"},{\"summary\":\"%s\"}]", EVENT_SUMMARY_ONE, EVENT_SUMMARY_TWO);
  private static final String EVENT_UNDEFINED_JSON = "[{}]";
  private static final String EMPTY_JSON = "[]";
  private static final CalendarListEntry PRIMARY = new CalendarListEntry().setId("primary");
  private static final CalendarListEntry SECONDARY = new CalendarListEntry().setId("secondary");
  private static final List<CalendarListEntry> ONE_CALENDAR = ImmutableList.of(PRIMARY);
  private static final List<CalendarListEntry> TWO_CALENDARS = ImmutableList.of(PRIMARY, SECONDARY);
  private static final List<Event> NO_EVENT = ImmutableList.of();
  private static final List<Event> EVENT_ONE =
      ImmutableList.of(new Event().setSummary(EVENT_SUMMARY_ONE));
  private static final List<Event> EVENT_TWO =
      ImmutableList.of(new Event().setSummary(EVENT_SUMMARY_TWO));
  private static final List<Event> EVENT_ONE_TWO =
      ImmutableList.of(
          new Event().setSummary(EVENT_SUMMARY_ONE), new Event().setSummary(EVENT_SUMMARY_TWO));
  private static final List<Event> EVENT_UNDEFINED = ImmutableList.of(new Event());
  private static final List<Event> EVENT_ALL =
      ImmutableList.of(
          new Event(),
          new Event().setSummary(EVENT_SUMMARY_ONE),
          new Event().setSummary(EVENT_SUMMARY_TWO));

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    calendarClientFactory = Mockito.mock(CalendarClientFactory.class);
    calendarClient = Mockito.mock(CalendarClient.class);
    servlet = new CalendarServlet(authenticationVerifier, calendarClientFactory);

    Mockito.when(calendarClientFactory.getCalendarClient(Mockito.any())).thenReturn(calendarClient);
  }

  @Test
  public void noCalendarEvent() throws Exception {
    // Test case where there are no events in the user's calendar
    Mockito.when(calendarClient.getCalendarList()).thenReturn(ONE_CALENDAR);
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(NO_EVENT);
    List<Event> actual = getServletResponse();
    Assert.assertEquals(NO_EVENT, actual);
  }

  @Test
  public void twoCalendars() throws Exception {
    // Test case where there are two calendars with a defined event in each
    // Events must be returned in order of retrieval
    Mockito.when(calendarClient.getCalendarList()).thenReturn(TWO_CALENDARS);
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(EVENT_ONE);
    Mockito.when(calendarClient.getCalendarEvents(SECONDARY)).thenReturn(EVENT_TWO);
    List<Event> actual = getServletResponse();
    Assert.assertEquals(EVENT_ONE_TWO, actual);
  }

  @Test
  public void undefinedEvent() throws Exception {
    // Test case where there is an event with no summary
    Mockito.when(calendarClient.getCalendarList()).thenReturn(ONE_CALENDAR);
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(EVENT_UNDEFINED);
    List<Event> actual = getServletResponse();
    Assert.assertEquals(EVENT_UNDEFINED, actual);
  }

  @Test
  public void allEvent() throws Exception {
    // Test case where there are two defined and an undefined event
    // Events must be returned in order of retrieval - JSON includes tasks in desired order
    Mockito.when(calendarClient.getCalendarList()).thenReturn(ONE_CALENDAR);
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(EVENT_ALL);
    List<Event> actual = getServletResponse();
    Assert.assertEquals(EVENT_ALL, actual);
  }

  private List<Event> getServletResponse() throws Exception {
    // Method that handles the once the Calendar Client has been mocked
    servlet.doGet(request, response);

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<Event>>() {}.getType();
    List<Event> actual = gson.fromJson(actualString, type);
    return actual;
  }
}
