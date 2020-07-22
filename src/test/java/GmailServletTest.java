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
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
  private HttpServletResponseFake response;

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
  private static final int INVALID_M_HOURS = DEFAULT_N_DAYS * 24 + 1;
  private static final int NEGATIVE_N_DAYS = -1;
  private static final int NEGATIVE_M_HOURS = -1;
  private static final long N_DAYS_TIMESTAMP =
      Instant.now().toEpochMilli() - TimeUnit.DAYS.toMillis(DEFAULT_N_DAYS - 1);
  private static final long M_HOURS_TIMESTAMP =
      Instant.now().toEpochMilli() - TimeUnit.HOURS.toMillis(DEFAULT_M_HOURS - 1);

  private static final String UNREAD_EMAIL_DAYS_QUERY =
      GmailClient.emailQueryString(DEFAULT_N_DAYS, "d", true, false, "");

  private static final String SENDER_ONE_NAME = "Sender_1";
  private static final String SENDER_ONE_EMAIL = "senderOne@sender.com";
  private static final String SENDER_TWO_NAME = "Sender_2";
  private static final String SENDER_TWO_EMAIL = "senderTwo@sender.com";

  private static final MessagePart SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD =
      generateMessagePayload(SENDER_ONE_EMAIL, SENDER_ONE_NAME);
  private static final MessagePart SENDER_ONE_WITHOUT_CONTACT_NAME_PAYLOAD =
      generateMessagePayload(SENDER_ONE_EMAIL);
  private static final MessagePart SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD =
      generateMessagePayload(SENDER_TWO_EMAIL, SENDER_TWO_NAME);
  private static final MessagePart SENDER_TWO_WITHOUT_CONTACT_NAME_PAYLOAD =
      generateMessagePayload(SENDER_TWO_EMAIL);

  private static final List<Message> NO_MESSAGES = ImmutableList.of();
  private static final List<Message> SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT =
      ImmutableList.of(
          new Message()
              .setId("messageOne")
              .setInternalDate(N_DAYS_TIMESTAMP)
              .setLabelIds(Collections.singletonList("IMPORTANT"))
              .setPayload(SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageTwo")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setLabelIds(Collections.singletonList("IMPORTANT"))
              .setPayload(SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageThree")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setPayload(SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD));
  private static final List<Message> SOME_MESSAGES_HALF_WITHIN_M_HOURS =
      ImmutableList.of(
          new Message()
              .setId("messageFour")
              .setInternalDate(N_DAYS_TIMESTAMP)
              .setPayload(SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageFive")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setPayload(SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD));
  private static final List<Message> MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME =
      ImmutableList.of(
          new Message()
              .setId("messageSix")
              .setInternalDate(N_DAYS_TIMESTAMP)
              .setPayload(SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageSeven")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setPayload(SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageEight")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setPayload(SENDER_TWO_WITHOUT_CONTACT_NAME_PAYLOAD));
  private static final List<Message> MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME =
      ImmutableList.of(
          new Message()
              .setId("messageNine")
              .setInternalDate(N_DAYS_TIMESTAMP)
              .setPayload(SENDER_ONE_WITHOUT_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageTen")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setPayload(SENDER_ONE_WITHOUT_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageEleven")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setPayload(SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD));
  private static final List<Message>
      MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT =
          ImmutableList.of(
              new Message()
                  .setId("messageTwelve")
                  .setInternalDate(N_DAYS_TIMESTAMP)
                  .setPayload(SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD),
              new Message()
                  .setId("messageThirteen")
                  .setInternalDate(M_HOURS_TIMESTAMP)
                  .setPayload(SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD),
              new Message()
                  .setId("messageFourteen")
                  .setInternalDate(N_DAYS_TIMESTAMP)
                  .setPayload(SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD),
              new Message()
                  .setId("messageFifteen")
                  .setInternalDate(N_DAYS_TIMESTAMP)
                  .setPayload(SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD));

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
    response = new HttpServletResponseFake();
    Mockito.when(request.getCookies()).thenReturn(validCookies);
  }

  /**
   * Auxiliary method to get a Message payload (with a "From" header) given a sender's email. From
   * header in the form of: "From": "<email@email.com>"
   *
   * @param email the sender's email
   * @return a MessagePart instance that can be used as the payload of a Message
   */
  private static MessagePart generateMessagePayload(String email) {
    return new MessagePart()
        .setHeaders(
            Collections.singletonList(
                new MessagePartHeader().setName("From").setValue(String.format("<%s>", email))));
  }

  /**
   * Auxiliary method to get a Message payload (with a "From" header) given a sender's email. From
   * header in the form of: "From": "SenderName <email@email.com>"
   *
   * @param email the sender's email
   * @param contactName the name of the sender
   * @return a MessagePart instance that can be used as the payload of a Message
   */
  private static MessagePart generateMessagePayload(String email, String contactName) {
    return new MessagePart()
        .setHeaders(
            Collections.singletonList(
                new MessagePartHeader()
                    .setName("From")
                    .setValue(String.format("%s <%s>", contactName, email))));
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
  private GmailResponse getGmailResponse(
      HttpServletRequest request, HttpServletResponseFake response)
      throws IOException, ServletException {
    servlet.doGet(request, response);
    return gson.fromJson(response.getStringWriter().toString(), GmailResponse.class);
  }

  @Test
  public void noNDaysParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays"))).thenReturn(null);
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void noMHoursParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));
    Mockito.when(request.getParameter(Mockito.eq("mHours"))).thenReturn(null);

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void negativeNDaysParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(NEGATIVE_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void negativeMHoursParameter() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(NEGATIVE_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void invalidMHoursParameter() throws IOException, ServletException {
    // mHours must represent less time than nDays
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(INVALID_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
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
    // For all queries, return no messages. Sender should be the default value.
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_SENDER, gmailResponse.getSender());
  }

  @Test
  public void calculateUnreadEmailsNDays() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(0).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(1).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(1));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(
        SOME_MESSAGES_HALF_WITHIN_M_HOURS.size(), gmailResponse.getUnreadEmailsDays());
  }

  @Test
  public void calculateUnreadEmailsMHours() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(0).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(1).getId()), Mockito.any()))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS.get(1));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(
        SOME_MESSAGES_HALF_WITHIN_M_HOURS.size() / 2, gmailResponse.getUnreadEmailsHours());
  }

  @Test
  public void calculateUnreadImportantEmailsFromNDays() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.eq(UNREAD_EMAIL_DAYS_QUERY)))
        .thenReturn(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.get(0).getId()),
                Mockito.any()))
        .thenReturn(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.get(1).getId()),
                Mockito.any()))
        .thenReturn(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.get(1));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.get(2).getId()),
                Mockito.any()))
        .thenReturn(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.get(2));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(
        SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.size() - 1,
        gmailResponse.getUnreadImportantEmails());
  }

  @Test
  public void getMostFrequentSenderWhenContactNameIsPresent() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME.get(0).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME.get(1).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME.get(1));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME.get(2).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME.get(2));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(SENDER_ONE_NAME, gmailResponse.getSender());
  }

  @Test
  public void getMostFrequentSenderWhenContactNameIsNotPresent()
      throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME.get(0).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME.get(1).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME.get(1));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME.get(2).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME.get(2));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(SENDER_ONE_EMAIL, gmailResponse.getSender());
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
        .thenReturn(MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT);

    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(0).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(0));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(1).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(1));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(2).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(2));
    Mockito.when(
            gmailClient.getUserMessage(
                Mockito.contains(
                    MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(3).getId()),
                Mockito.any()))
        .thenReturn(MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT.get(3));

    // Most recent sender does not have a contact name
    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(SENDER_ONE_NAME, gmailResponse.getSender());
  }
}
