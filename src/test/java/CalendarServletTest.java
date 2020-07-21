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

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.CalendarClient;
import com.google.sps.model.CalendarClientFactory;
import com.google.sps.servlets.CalendarServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import com.google.sps.data.CalendarClientData;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

/** Test Calendar Servlet responds to client with correctly parsed Events. */
@RunWith(JUnit4.class)
public final class CalendarServletTest {
  private AuthenticationVerifier authenticationVerifier;
  private CalendarClientFactory calendarClientFactory;
  private CalendarClient calendarClient;
  private CalendarServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;
  private static final Gson gson = new Gson();

  private static final String ID_TOKEN_KEY = "idToken";
  private static final String ID_TOKEN_VALUE = "sampleId";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String ACCESS_TOKEN_VALUE = "sampleAccessToken";
  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

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
  private static final DateTime CURRENT_TIME = new DateTime("2020-05-19T09:00:00+00:00");
  private static final DateTime END_TIME = new DateTime(CURRENT_TIME.getValue() + 5 * 24 * 60 * 60 * 1000);
  private static final DateTime EVENT_ONE_START = new DateTime("2020-05-19T15:00:00+00:00");
  private static final DateTime EVENT_ONE_END = new DateTime("2020-05-19T16:00:00+00:00");
  private static final DateTime EVENT_TWO_START = new DateTime("2020-05-20T06:00:00+00:00");
  private static final DateTime EVENT_TWO_END = new DateTime("2020-05-20T08:00:00+00:00");
  private static final EventDateTime startOne = new EventDateTime().setDateTime(EVENT_ONE_START);
  private static final EventDateTime endOne = new EventDateTime().setDateTime(EVENT_ONE_END);
  private static final EventDateTime startTwo = new EventDateTime().setDateTime(EVENT_TWO_START);
  private static final EventDateTime endTwo = new EventDateTime().setDateTime(EVENT_TWO_END);
  private static final List<Event> NO_EVENT = ImmutableList.of();
  private static final List<Event> EVENT_ONE =
      ImmutableList.of(
          new Event().setSummary(EVENT_SUMMARY_ONE).setStart(startOne).setEnd(endOne));
  private static final List<Event> EVENT_TWO =
      ImmutableList.of(
          new Event().setSummary(EVENT_SUMMARY_TWO).setStart(startTwo).setEnd(endTwo));

  @Before
  public void setUp() throws IOException, GeneralSecurityException {
    authenticationVerifier = Mockito.mock(AuthenticationVerifier.class);
    calendarClientFactory = Mockito.mock(CalendarClientFactory.class);
    calendarClient = Mockito.mock(CalendarClient.class);
    servlet = new CalendarServlet(authenticationVerifier, calendarClientFactory);

    Mockito.when(calendarClientFactory.getCalendarClient(Mockito.any())).thenReturn(calendarClient);
    // Authentication will always pass
    Mockito.when(authenticationVerifier.verifyUserToken(Mockito.anyString())).thenReturn(true);

    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(request.getCookies()).thenReturn(validCookies);

    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void noCalendarEvent() throws IOException, ServletException {
    // Test case where there are no events in the user's calendar
    Mockito.when(calendarClient.getCalendarList()).thenReturn(ONE_CALENDAR);
    Mockito.when(calendarClient.getUpcomingEvents(PRIMARY, CURRENT_TIME, END_TIME)).thenReturn(NO_EVENT);
    Mockito.when(calendarClient.getCurrentTime()).thenReturn(CURRENT_TIME);
    CalendarClientData actual = getServletResponse();
    int hour = 60 * 60 * 1000;
    int[] workHours = new int[] {8 * hour, 8 * hour, 8 * hour, 8 * hour, 8 * hour};
    int[] personalHours = new int[] {16 * hour, 16 * hour, 16 * hour, 16 * hour, 16 * hour};
    Assert.assertEquals(1, actual.getStartDay());
    Assert.assertTrue(Arrays.equals(workHours, actual.getWorkHours()));
    Assert.assertTrue(Arrays.equals(personalHours, actual.getPersonalHours()));
  }

  @Test
  public void twoCalendars() throws IOException, ServletException {
    // Test case where there are two calendars with a defined event in each
    // Event with 1 working hour and event with 2 personal hours. 
    Mockito.when(calendarClient.getCalendarList()).thenReturn(TWO_CALENDARS);
    Mockito.when(calendarClient.getUpcomingEvents(PRIMARY, CURRENT_TIME, END_TIME)).thenReturn(EVENT_ONE);
    Mockito.when(calendarClient.getUpcomingEvents(SECONDARY, CURRENT_TIME, END_TIME)).thenReturn(EVENT_TWO);
    Mockito.when(calendarClient.getCurrentTime()).thenReturn(CURRENT_TIME);
    CalendarClientData actual = getServletResponse();
    int hour = 60 * 60 * 1000;
    int[] workHours = new int[] {7 * hour, 8 * hour, 8 * hour, 8 * hour, 8 * hour};
    int[] personalHours = new int[] {16 * hour, 14 * hour, 16 * hour, 16 * hour, 16 * hour};
    Assert.assertEquals(1, actual.getStartDay());
    Assert.assertTrue(Arrays.equals(workHours, actual.getWorkHours()));
    Assert.assertTrue(Arrays.equals(personalHours, actual.getPersonalHours()));
  }

  private CalendarClientData getServletResponse() throws IOException, ServletException {
    // Method that handles the request once the Calendar Client has been mocked
    servlet.doGet(request, response);
    printWriter.flush();
    String actualString = stringWriter.toString();
    CalendarClientData actual = gson.fromJson(actualString, CalendarClientData.class);
    return actual;
  }
}
