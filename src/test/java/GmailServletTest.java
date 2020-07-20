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

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.common.collect.ImmutableList;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailResponse;
import com.google.sps.servlets.GmailServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.Collections;
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

/**
 * Test Gmail Servlet to ensure response contains correctly parsed messageIds. Assumes
 * AuthenticatedHttpServlet is functioning properly (those tests will fail otherwise).
 */
@RunWith(JUnit4.class)
public final class GmailServletTest {
  private GmailClient gmailClient;
  private GmailServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  private static final Gson gson = new Gson();

  private static final boolean AUTHENTICATION_VERIFIED = true;
  private static final String ID_TOKEN_KEY = "idToken";
  private static final String ID_TOKEN_VALUE = "sampleId";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String ACCESS_TOKEN_VALUE = "sampleAccessToken";

  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

  private static final String DEFAULT_SENDER = "";
  private static final int DEFAULT_N_DAYS = 7;
  private static final int DEFAULT_M_HOURS = 3;
  private static final int NEGATIVE_N_DAYS = -1;
  private static final int NEGATIVE_M_HOURS = -1;
  private static final long LEAST_RECENT_TIMESTAMP = 12345;
  private static final long MOST_RECENT_TIMESTAMP = 123456;

  private static final String UNREAD_EMAIL_DAYS_QUERY =
      GmailClient.emailQueryString(DEFAULT_N_DAYS, "d", true, false, "");
  private static final String UNREAD_EMAIL_HOURS_QUERY =
      GmailClient.emailQueryString(DEFAULT_M_HOURS, "h", true, false, "");
  private static final String IMPORTANT_QUERY =
      GmailClient.emailQueryString(DEFAULT_N_DAYS, "d", true, true, "");

  private static final String MESSAGE_ID_LEAST_RECENT_SENDER_ONE = "leastRecentSenderMessageOne";
  private static final String MESSAGE_ID_LEAST_RECENT_SENDER_TWO = "leastRecentSenderMessageTwo";
  private static final String MESSAGE_ID_MOST_RECENT_SENDER_ONE = "mostRecentSenderMessageOne";
  private static final String MESSAGE_ID_MOST_RECENT_SENDER_TWO = "mostRecentSenderMessageTwo";
  private static final String LEAST_RECENT_SENDER_NAME = "Sender_1";
  private static final String LEAST_RECENT_SENDER_EMAIL = "senderOne@sender.com";
  private static final String MOST_RECENT_SENDER_EMAIL = "senderTwo@sender.com";
  private static final String LEAST_RECENT_SENDER_WITH_CONTACT_NAME_FROM_HEADER =
      String.format("%s <%s>", LEAST_RECENT_SENDER_NAME, LEAST_RECENT_SENDER_EMAIL);
  private static final String MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME_FROM_HEADER =
      String.format("<%s>", MOST_RECENT_SENDER_EMAIL);

  private static final MessagePart leastRecentSenderWithContactNamePayload =
      new MessagePart()
          .setHeaders(
              Collections.singletonList(
                  new MessagePartHeader()
                      .setName("From")
                      .setValue(LEAST_RECENT_SENDER_WITH_CONTACT_NAME_FROM_HEADER)));
  private static final MessagePart mostRecentSenderWithoutContactNamePayload =
      new MessagePart()
          .setHeaders(
              Collections.singletonList(
                  new MessagePartHeader()
                      .setName("From")
                      .setValue(MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME_FROM_HEADER)));

  private static final Message leastRecentSenderWithContactNameMessageOne =
      new Message()
          .setId(MESSAGE_ID_LEAST_RECENT_SENDER_ONE)
          .setInternalDate(LEAST_RECENT_TIMESTAMP)
          .setPayload(leastRecentSenderWithContactNamePayload);
  private static final Message leastRecentSenderWithContactNameMessageTwo =
      new Message()
          .setId(MESSAGE_ID_LEAST_RECENT_SENDER_TWO)
          .setInternalDate(LEAST_RECENT_TIMESTAMP)
          .setPayload(leastRecentSenderWithContactNamePayload);
  private static final Message mostRecentSenderWithoutContactNameMessageOne =
      new Message()
          .setId(MESSAGE_ID_MOST_RECENT_SENDER_ONE)
          .setInternalDate(MOST_RECENT_TIMESTAMP)
          .setPayload(mostRecentSenderWithoutContactNamePayload);
  private static final Message mostRecentSenderWithoutContactNameMessageTwo =
      new Message()
          .setId(MESSAGE_ID_MOST_RECENT_SENDER_TWO)
          .setInternalDate(MOST_RECENT_TIMESTAMP)
          .setPayload(mostRecentSenderWithoutContactNamePayload);

  private static final List<Message> NO_MESSAGES = ImmutableList.of();
  private static final List<Message> SOME_MESSAGES =
      ImmutableList.of(
          leastRecentSenderWithContactNameMessageOne, mostRecentSenderWithoutContactNameMessageOne);
  private static final List<Message> MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME =
      ImmutableList.of(
          leastRecentSenderWithContactNameMessageOne,
          leastRecentSenderWithContactNameMessageTwo,
          mostRecentSenderWithoutContactNameMessageOne);
  private static final List<Message> MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME =
      ImmutableList.of(
          leastRecentSenderWithContactNameMessageOne,
          mostRecentSenderWithoutContactNameMessageOne,
          mostRecentSenderWithoutContactNameMessageTwo);
  private static final List<Message> MESSAGES_EQUAL_SENDER_FREQUENCIES =
      ImmutableList.of(
          leastRecentSenderWithContactNameMessageOne,
          leastRecentSenderWithContactNameMessageTwo,
          mostRecentSenderWithoutContactNameMessageOne,
          mostRecentSenderWithoutContactNameMessageTwo);

  @Before
  public void setUp() throws IOException, GeneralSecurityException {
    AuthenticationVerifier authenticationVerifier = Mockito.mock(AuthenticationVerifier.class);
    GmailClientFactory gmailClientFactory = Mockito.mock(GmailClientFactory.class);
    gmailClient = Mockito.mock(GmailClient.class);
    servlet = new GmailServlet(authenticationVerifier, gmailClientFactory);

    Mockito.when(gmailClientFactory.getGmailClient(Mockito.any())).thenReturn(gmailClient);
    // Authentication will always pass
    Mockito.when(authenticationVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);

    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(request.getCookies()).thenReturn(validCookies);

    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  /**
   * Auxiliary method to get a GmailResponse object from a servlet when using doGet
   *
   * @param request Mock HttpRequest
   * @param response Mock HttpResponse
   * @return GmailResponse object from doGet method
   * @throws IOException if a read/write issue occurs while processing the request
   * @throws ServletException if another unexpected issue occurs while processing the request
   */
  private GmailResponse getGmailResponse(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    servlet.doGet(request, response);
    printWriter.flush();
    return gson.fromJson(stringWriter.toString(), GmailResponse.class);
  }

  @Test
  public void noNDaysParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays"))).thenReturn(null);
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(400), Mockito.anyString());
  }

  @Test
  public void noMHoursParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));
    Mockito.when(request.getParameter(Mockito.eq("mHours"))).thenReturn(null);

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(400), Mockito.anyString());
  }

  @Test
  public void negativeNDaysParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(NEGATIVE_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(400), Mockito.anyString());
  }

  @Test
  public void negativeMHoursParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(NEGATIVE_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(400), Mockito.anyString());
  }

  @Test
  public void checkDefaultNDaysInResponse() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_N_DAYS, gmailResponse.getNDays());
  }

  @Test
  public void checkDefaultMHoursInResponse() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_M_HOURS, gmailResponse.getMHours());
  }

  @Test
  public void checkDefaultUnreadEmailsFromNDaysCountInResponse()
      throws IOException, ServletException {
    // For all queries, return no messages. Unread emails days should have 0 length
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(0, gmailResponse.getUnreadEmailsDays());
  }

  @Test
  public void checkDefaultUnreadEmailsFromMHoursCountInResponse()
      throws IOException, ServletException {
    // For all queries, return no messages. Unread emails hours should have 0 length
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(0, gmailResponse.getUnreadEmailsHours());
  }

  @Test
  public void checkDefaultUnreadImportantEmailsFromNDaysCountInResponse()
      throws IOException, ServletException {
    // For all queries, return no messages. Unread important emails should have 0 length
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(0, gmailResponse.getUnreadImportantEmails());
  }

  @Test
  public void checkDefaultSenderInResponse() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_SENDER, gmailResponse.getSender());
  }

  @Test
  public void checkUnreadEmailsFromNDaysCountInResponse() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(SOME_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_HOURS_QUERY)))
        .thenReturn(NO_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(IMPORTANT_QUERY))).thenReturn(NO_MESSAGES);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES.get(0).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES.get(1).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES.get(1));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(SOME_MESSAGES.size(), gmailResponse.getUnreadEmailsDays());
  }

  @Test
  public void checkUnreadEmailsFromMHoursCountInResponse() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(SOME_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_HOURS_QUERY)))
        .thenReturn(SOME_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(IMPORTANT_QUERY))).thenReturn(NO_MESSAGES);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES.get(0).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES.get(1).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES.get(1));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(SOME_MESSAGES.size(), gmailResponse.getUnreadEmailsHours());
  }

  @Test
  public void checkUnreadImportantEmailsFromNDaysCountInResponse()
      throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(SOME_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_HOURS_QUERY)))
        .thenReturn(NO_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(IMPORTANT_QUERY)))
        .thenReturn(SOME_MESSAGES);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES.get(0).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES.get(1).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES.get(1));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(SOME_MESSAGES.size(), gmailResponse.getUnreadImportantEmails());
  }

  @Test
  public void getMostFrequentSenderWhenContactNameIsNotPresent()
      throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString()))
        .thenReturn(MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME.get(0).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME.get(1).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME.get(1));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME.get(2).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_MOST_RECENT_SENDER_WITHOUT_CONTACT_NAME.get(2));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(MOST_RECENT_SENDER_EMAIL, gmailResponse.getSender());
  }

  @Test
  public void getMostFrequentSenderWhenContactNameIsPresent() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString()))
        .thenReturn(MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME.get(0).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME.get(1).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME.get(1));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME.get(2).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_LEAST_RECENT_SENDER_WITH_CONTACT_NAME.get(2));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(LEAST_RECENT_SENDER_NAME, gmailResponse.getSender());
  }

  @Test
  public void checkSenderEqualFrequenciesPreferMostRecentSender()
      throws IOException, ServletException {
    // This is the case when there is an equal amount of sent messages from two senders
    // (i.e. 2 from sender A, 2 from sender B).
    // The sender who sent the latest/most recent contact should be returned.
    // NOTE: in the case that the frequencies are tied, and they both sent emails at the exact same
    // time
    // (which is absurdly rare), the system can return either of the senders (it won't matter to the
    // user).
    // Thus, that circumstance will not be tested.

    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString()))
        .thenReturn(MESSAGES_EQUAL_SENDER_FREQUENCIES);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(0).getId()), Mockito.any()))
        .thenReturn(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(1).getId()), Mockito.any()))
        .thenReturn(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(1));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(2).getId()), Mockito.any()))
        .thenReturn(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(2));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(3).getId()), Mockito.any()))
        .thenReturn(MESSAGES_EQUAL_SENDER_FREQUENCIES.get(3));

    // Most recent sender does not have a contact name
    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(MOST_RECENT_SENDER_EMAIL, gmailResponse.getSender());
  }
}
