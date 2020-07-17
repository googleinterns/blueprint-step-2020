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

  private static final String DEFAULT_QUERY_STRING = "";

  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

  private static final String MESSAGE_ID_ONE = "messageIdOne";
  private static final String MESSAGE_ID_TWO = "messageIdTwo";
  private static final String MESSAGE_ID_THREE = "messageIdThree";
  private static final String SENDER_ONE_NAME = "Sender_1";
  private static final String SENDER_ONE_EMAIL = "senderOne@sender.com";
  private static final String SENDER_TWO_EMAIL = "senderTwo@sender.com";
  private static final String SENDER_WITH_NAME =
      String.format("%s <%s>", SENDER_ONE_NAME, SENDER_ONE_EMAIL);
  private static final String SENDER_WITHOUT_NAME = String.format("<%s>", SENDER_TWO_EMAIL);

  private static final String DEFAULT_SENDER = "";
  private static final int DEFAULT_N_DAYS = 7;
  private static final int DEFAULT_M_HOURS = 3;

  private static final String UNREAD_EMAIL_DAYS_QUERY =
      String.format("newer_than:%dd is:unread", DEFAULT_N_DAYS);
  private static final String UNREAD_EMAIL_HOURS_QUERY =
      String.format("newer_than:%dh is:unread", DEFAULT_M_HOURS);
  private static final String IMPORTANT_QUERY =
      String.format("newer_than:%dd is:unread is:important", DEFAULT_N_DAYS);

  private static final List<Message> NO_MESSAGES = ImmutableList.of();
  private static final List<Message> THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME =
      ImmutableList.of(
          new Message()
              .setId(MESSAGE_ID_ONE)
              .setPayload(
                  new MessagePart()
                      .setHeaders(
                          Collections.singletonList(
                              new MessagePartHeader().setName("From").setValue(SENDER_WITH_NAME)))),
          new Message()
              .setId(MESSAGE_ID_TWO)
              .setPayload(
                  new MessagePart()
                      .setHeaders(
                          Collections.singletonList(
                              new MessagePartHeader().setName("From").setValue(SENDER_WITH_NAME)))),
          new Message()
              .setId(MESSAGE_ID_THREE)
              .setPayload(
                  new MessagePart()
                      .setHeaders(
                          Collections.singletonList(
                              new MessagePartHeader()
                                  .setName("From")
                                  .setValue(SENDER_WITHOUT_NAME)))));
  private static final List<Message> THREE_MESSAGES_MAJORITY_SENDER_WITHOUT_NAME =
      ImmutableList.of(
          new Message()
              .setId(MESSAGE_ID_ONE)
              .setPayload(
                  new MessagePart()
                      .setHeaders(
                          Collections.singletonList(
                              new MessagePartHeader().setName("From").setValue(SENDER_WITH_NAME)))),
          new Message()
              .setId(MESSAGE_ID_TWO)
              .setPayload(
                  new MessagePart()
                      .setHeaders(
                          Collections.singletonList(
                              new MessagePartHeader()
                                  .setName("From")
                                  .setValue(SENDER_WITHOUT_NAME)))),
          new Message()
              .setId(MESSAGE_ID_THREE)
              .setPayload(
                  new MessagePart()
                      .setHeaders(
                          Collections.singletonList(
                              new MessagePartHeader()
                                  .setName("From")
                                  .setValue(SENDER_WITHOUT_NAME)))));

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
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void checkDefaultNDays() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(DEFAULT_N_DAYS, gmailResponse.getNDays());
  }

  @Test
  public void checkDefaultMHours() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(DEFAULT_M_HOURS, gmailResponse.getMHours());
  }

  @Test
  public void checkDefaultUnreadEmailsDays() throws IOException, ServletException {
    // For all queries, return no messages. Unread emails days should have 0 length
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(0, gmailResponse.getUnreadEmailsDays());
  }

  @Test
  public void checkDefaultUnreadEmailsHours() throws IOException, ServletException {
    // For all queries, return no messages. Unread emails hours should have 0 length
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(0, gmailResponse.getUnreadEmailsHours());
  }

  @Test
  public void checkDefaultUnreadImportantEmails() throws IOException, ServletException {
    // For all queries, return no messages. Unread important emails should have 0 length
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(0, gmailResponse.getUnreadImportantEmails());
  }

  @Test
  public void checkDefaultSender() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(DEFAULT_SENDER, gmailResponse.getSender());
  }

  @Test
  public void checkSomeUnreadEmailsDays() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_HOURS_QUERY)))
        .thenReturn(NO_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(IMPORTANT_QUERY))).thenReturn(NO_MESSAGES);

    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_ONE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(0));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_TWO), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(1));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_THREE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(2));

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(
        THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.size(), gmailResponse.getUnreadEmailsDays());
  }

  @Test
  public void checkSomeUnreadEmailsHours() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_HOURS_QUERY)))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(IMPORTANT_QUERY))).thenReturn(NO_MESSAGES);

    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_ONE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(0));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_TWO), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(1));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_THREE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(2));

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(
        THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.size(), gmailResponse.getUnreadEmailsHours());
  }

  @Test
  public void checkSomeUnreadImportantEmails() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_HOURS_QUERY)))
        .thenReturn(NO_MESSAGES);
    Mockito.when(gmailClient.listUserMessages(Mockito.eq(IMPORTANT_QUERY)))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME);

    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_ONE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(0));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_TWO), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(1));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_THREE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(2));

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(
        THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.size(), gmailResponse.getUnreadImportantEmails());
  }

  @Test
  public void checkSenderWithNamePresentInHeader() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME);

    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_ONE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(0));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_TWO), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(1));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_THREE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME.get(2));

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(SENDER_ONE_NAME, gmailResponse.getSender());
  }

  @Test
  public void checkSenderWithoutNamePresentInHeader() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITH_NAME);

    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_ONE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITHOUT_NAME.get(0));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_TWO), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITHOUT_NAME.get(1));
    Mockito.when(gmailClient.getUserMessage(Mockito.contains(MESSAGE_ID_THREE), Mockito.any()))
        .thenReturn(THREE_MESSAGES_MAJORITY_SENDER_WITHOUT_NAME.get(2));

    servlet.doGet(request, response);
    printWriter.flush();
    GmailResponse gmailResponse = gson.fromJson(stringWriter.toString(), GmailResponse.class);
    Assert.assertEquals(SENDER_TWO_EMAIL, gmailResponse.getSender());
  }
}
