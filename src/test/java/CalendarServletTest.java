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
  private static final List<Event> NO_EVENT = ImmutableList.of();
  private static final List<Event> EVENT_ONE =
      ImmutableList.of(
          new Event().setSummary(EVENT_SUMMARY_ONE));
  private static final List<Event> EVENT_TWO =
      ImmutableList.of(
          new Event().setSummary(EVENT_SUMMARY_TWO));
  private static final List<Event> EVENT_ONE_TWO =
      ImmutableList.of(
          new Event().setSummary(EVENT_SUMMARY_ONE), new Event().setSummary(EVENT_SUMMARY_TWO));
  private static final List<Event> EVENT_UNDEFINED = ImmutableList.of(new Event());
  private static final List<Event> EVENT_ALL =
      ImmutableList.of(
          new Event(),
          new Event().setSummary(EVENT_SUMMARY_ONE),
          new Event().setSummary(EVENT_SUMMARY_TWO));

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
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(NO_EVENT);
    List<Event> actual = checkReturn();
    Assert.assertEquals(NO_EVENT, actual);
  }

  @Test
  public void twoCalendars() throws IOException, ServletException {
    // Test case where there are two calendars with a defined event in each
    // Events must be returned in order of retrieval
    Mockito.when(calendarClient.getCalendarList()).thenReturn(TWO_CALENDARS);
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(EVENT_ONE);
    Mockito.when(calendarClient.getCalendarEvents(SECONDARY)).thenReturn(EVENT_TWO);
    List<Event> actual = checkReturn();
    Assert.assertEquals(EVENT_ONE_TWO, actual);
  }

  @Test
  public void undefinedEvent() throws IOException, ServletException {
    // Test case where there is an event with no summary
    Mockito.when(calendarClient.getCalendarList()).thenReturn(ONE_CALENDAR);
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(EVENT_UNDEFINED);
    List<Event> actual = checkReturn();
    Assert.assertEquals(EVENT_UNDEFINED, actual);
  }

  @Test
  public void allEvent() throws IOException, ServletException {
    // Test case where there are two defined and an undefined event
    // Events must be returned in order of retrieval - JSON includes tasks in desired order
    Mockito.when(calendarClient.getCalendarList()).thenReturn(ONE_CALENDAR);
    Mockito.when(calendarClient.getCalendarEvents(PRIMARY)).thenReturn(EVENT_ALL);
    List<Event> actual = checkReturn();
    Assert.assertEquals(EVENT_ALL, actual);
  }

  public List<Event> checkReturn() throws IOException, ServletException {
    // Method that handles the once the Calendar Client has been mocked
    servlet.doGet(request, response);
    printWriter.flush();
    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<Event>>() {}.getType();
    List<Event> actual = gson.fromJson(actualString, type);
    return actual;
  }
}
