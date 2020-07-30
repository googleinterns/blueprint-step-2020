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

import com.google.api.client.util.DateTime;
import com.google.api.services.gmail.model.Message;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.CalendarClient;
import com.google.sps.model.CalendarClientFactory;
import com.google.sps.model.CalendarClientImpl;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailClientImpl;
import com.google.sps.utility.FreeTimeUtility;
import com.google.sps.utility.DatePair;
import com.google.sps.utility.JsonUtility;
import com.google.sps.data.PlanMailResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.util.Base64;
import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.BodyPart;

/** GET function responds JSON string containing potential events to read mail. */
@WebServlet("/plan-mail")
public class PlanMailServlet extends AuthenticatedHttpServlet {
  private final CalendarClientFactory calendarClientFactory;
  private final GmailClientFactory gmailClientFactory;

  /** Create servlet with default CalendarClient and Authentication Verifier implementations */
  public PlanMailServlet() {
    calendarClientFactory = new CalendarClientImpl.Factory();
    gmailClientFactory = new GmailClientImpl.Factory();
  }

  /**
   * Create servlet with explicit implementations of CalendarClient and AuthenticationVerifier
   *
   * @param authenticationVerifier implementation of AuthenticationVerifier
   * @param calendarClientFactory implementation of CalendarClientFactory
   */
  public PlanMailServlet(
      AuthenticationVerifier authenticationVerifier, CalendarClientFactory calendarClientFactory, GmailClientFactory gmailClientFactory) {
    super(authenticationVerifier);
    this.calendarClientFactory = calendarClientFactory;
    this.gmailClientFactory = gmailClientFactory;
  }

  /**
   * Returns string containing the some of the user's free time intervals, where events can be created
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
    GmailClient gmailClient = gmailClientFactory.getGmailClient(googleCredential);
    long fiveDaysInMillis = TimeUnit.DAYS.toMillis(5);
    Date timeMin = calendarClient.getCurrentTime();
    Date timeMax = new Date(timeMin.getTime() + fiveDaysInMillis);
    List<Event> calendarEvents = getEvents(calendarClient, timeMin, timeMax);

    FreeTimeUtility freeTimeUtility = new FreeTimeUtility(timeMin);
    for (Event event : calendarEvents) {
      DateTime start = event.getStart().getDateTime();
      start = start == null ? event.getStart().getDate() : start;
      DateTime end = event.getEnd().getDateTime();
      end = end == null ? event.getEnd().getDate() : end;
      Date eventStart = new Date(start.getValue());
      Date eventEnd = new Date(end.getValue());
      if (eventStart.before(timeMin)) {
        eventStart = timeMin;
      }
      if (eventEnd.after(timeMax)) {
        eventEnd = timeMax;
      }
      freeTimeUtility.addEvent(eventStart, eventEnd);
    }

    int wordCount = getWordCount(gmailClient);
    int averageReadingSpeed = 200;
    int minutesToRead = wordCount / averageReadingSpeed;
    long timeNeeded = minutesToRead * TimeUnit.MINUTES.toMillis(1);
    List<DatePair> potentialTimes = getPotentialTimes(freeTimeUtility, timeNeeded);

    PlanMailResponse planMailResponse = new PlanMailResponse(wordCount, averageReadingSpeed, minutesToRead, potentialTimes);
    // Convert event list to JSON and print to response
    JsonUtility.sendJson(response, planMailResponse);
  }

  private List<DatePair> getPotentialTimes(FreeTimeUtility freeTimeUtility, long timeNeeded) {
    List<DatePair> workFreeInterval = freeTimeUtility.getWorkFreeInterval();
    List<DatePair> potentialEventTimes = new ArrayList<>();
    long remainingTime = timeNeeded;
    for (DatePair interval: workFreeInterval) {
      Date potentialEnd = new Date(interval.getKey().getTime() + remainingTime);
      if (interval.getValue().before(potentialEnd)) {
        remainingTime -= (interval.getValue().getTime() - interval.getKey().getTime());
        potentialEventTimes.add(interval);
      }
      else {
        potentialEventTimes.add(new DatePair(interval.getKey(), potentialEnd));
        break;
      }
    }
    return potentialEventTimes;
  }

  private int getWordCount(GmailClient gmailClient) {
    GmailClient.MessageFormat messageFormat = GmailClient.MessageFormat.RAW;
    int numberDays = 7;
    List<Message> unreadMessages = new ArrayList<>();
    try {
      gmailClient.getUnreadEmailsFromNDays(messageFormat, numberDays);
    } catch (IOException e) {
      System.out.println(e);
    }
    for (Message message: unreadMessages) {
      try {
        System.out.println(getMessageSize(message));
      } catch (MessagingException | IOException e) {
        System.out.println(e);
      }
    }
    return 10464;
  }

  /**
   * Get the events in the user's calendars
   *
   * @param calendarClient either a mock CalendarClient or a calendarClient with a valid credential
   * @param timeMin the minimum time to start looking for events
   * @param timeMax the maximum time to look for events
   * @return List of Events from all of the user's calendars
   * @throws IOException if an issue occurs in the method
   */
  private List<Event> getEvents(CalendarClient calendarClient, Date timeMin, Date timeMax)
      throws IOException {
    List<CalendarListEntry> calendarList = calendarClient.getCalendarList();
    List<Event> events = new ArrayList<>();
    for (CalendarListEntry calendar : calendarList) {
      events.addAll(calendarClient.getUpcomingEvents(calendar, timeMin, timeMax));
    }
    return events;
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

  public int getMessageSize(Message message) throws MessagingException, IOException {
    byte[] emailBytes = Base64.decodeBase64(message.getRaw());
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
    String emailString = "";
    if (email.isMimeType("text/*")) {
      emailString = (String) email.getContent();
    }
    else if (email.isMimeType("multipart/alternative")) {
      emailString = getTextFromMultiPartAlternative((Multipart) email.getContent());
    }
    else if (email.isMimeType("multipart/digest")) {
      emailString = getTextFromMultiPartDigest((Multipart) email.getContent());
    }
    else if (mimeTypeCanBeHandledAsMultiPartMixed(email)) {
      emailString = getTextHandledAsMultiPartMixed(email);
    }
    System.out.println(emailString);
    return 3;
  }

  private String getTextFromMultiPartAlternative(Multipart multipart) throws IOException, MessagingException {
    // search in reverse order as a multipart/alternative should have their most preferred format last
    for (int i = multipart.getCount() - 1; i >= 0; i--) {
      BodyPart bodyPart = multipart.getBodyPart(i);

      if (bodyPart.isMimeType("text/html")) {
        return (String) bodyPart.getContent();
      } else if (bodyPart.isMimeType("text/plain")) {
        // Since we are looking in reverse order, if we did not encounter a text/html first we can return the plain
        // text because that is the best preferred format that we understand. If a text/html comes along later it
        // means the agent sending the email did not set the html text as preferable or did not set their preferred
        // order correctly, and in that case we do not handle that.
        return (String) bodyPart.getContent();
      } else if (bodyPart.isMimeType("multipart/*") || bodyPart.isMimeType("message/rfc822")) {
        String text = getTextFromPart(bodyPart);
        if (text != null) {
          return text;
        }
      }
    }
    // we do not know how to handle the text in the multipart or there is no text
    return null;
  }

  private String getTextFromMultiPartDigest(Multipart multipart) throws IOException, MessagingException {
    StringBuilder textBuilder = new StringBuilder();
    for (int i = 0; i < multipart.getCount(); i++) {
      BodyPart bodyPart = multipart.getBodyPart(i);
      if (bodyPart.isMimeType("message/rfc822")) {
        String text = getTextFromPart(bodyPart);
        if (text != null) {
          textBuilder.append(text);
        }
      }
    }
    String text = textBuilder.toString();

    if (text.isEmpty()) {
      return null;
    }

    return text;
  }

  private boolean mimeTypeCanBeHandledAsMultiPartMixed(Part part) throws MessagingException {
    return part.isMimeType("multipart/mixed") || part.isMimeType("multipart/parallel")
      || part.isMimeType("message/rfc822")
      // as per the RFC2046 specification, other multipart subtypes are recognized as multipart/mixed
      || part.isMimeType("multipart/*");
  }

  private String getTextHandledAsMultiPartMixed(Part part) throws IOException, MessagingException {
    return getTextFromMultiPartMixed((Multipart) part.getContent());
  }

  private String getTextFromPart(Part part) throws MessagingException, IOException {
    if (part.isMimeType("multipart/alternative")) {
      return getTextFromMultiPartAlternative((Multipart) part.getContent());
    } else if (part.isMimeType("multipart/digest")) {
      return getTextFromMultiPartDigest((Multipart) part.getContent());
    } else if (mimeTypeCanBeHandledAsMultiPartMixed(part)) {
      return getTextHandledAsMultiPartMixed(part);
    }

    return null;
  }

  private String getTextFromMultiPartMixed(Multipart multipart) throws IOException, MessagingException {
    StringBuilder textBuilder = new StringBuilder();
    for (int i = 0; i < multipart.getCount(); i++) {
      BodyPart bodyPart = multipart.getBodyPart(i);
      if (bodyPart.isMimeType("text/*")) {
        textBuilder.append((String) bodyPart.getContent());
      } else if (bodyPart.isMimeType("multipart/*")) {
        String text = getTextFromPart(bodyPart);
        if (text != null) {
          textBuilder.append(text);
        }
      }
    }
    String text = textBuilder.toString();

    if (text.isEmpty()) {
      return null;
    }

    return text;
  }
}
