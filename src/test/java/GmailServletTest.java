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

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailResponse;
import com.google.sps.servlets.GmailServlet;
import com.google.sps.utility.GmailResponseUtility;
import java.io.IOException;
import java.security.GeneralSecurityException;
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
 * AuthenticatedHttpServlet and GmailResponseUtilityTest is functioning properly (those tests will
 * fail otherwise).
 */
@RunWith(JUnit4.class)
public final class GmailServletTest extends GmailTestBase {
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
  public void checkNDaysInResponse() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_N_DAYS, gmailResponse.getNDays());
  }

  @Test
  public void checkMHoursInResponse() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_M_HOURS, gmailResponse.getMHours());
  }

  @Test
  public void checkDefaultUnreadEmailsNDaysInResponse() throws IOException, ServletException {
    // no messages returned - unread email count (nDays) should be 0
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(0, gmailResponse.getUnreadEmailsDays());
  }

  @Test
  public void checkDefaultSenderInResponse() throws IOException, ServletException {
    // For all queries, return no messages. Sender should be the default value in response (not
    // null)
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_SENDER, gmailResponse.getSender());
  }

  @Test
  public void checkUnreadEmailsNDaysInResponse() throws Exception {
    // some messages returned - unread email count (nDays) should be message list length
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
  public void checkUnreadEmailsFromMHoursCountInResponse() throws IOException, ServletException {
    // This does NOT check that the statistic is correctly calculated.
    // This only checks that the unreadEmailsFromMHours generated from GmailResponseUtility is
    // correctly included in the response
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
    int expected =
        GmailResponseUtility.countEmailsFromMHours(
            SOME_MESSAGES_HALF_WITHIN_M_HOURS, DEFAULT_M_HOURS);
    Assert.assertEquals(expected, gmailResponse.getUnreadEmailsHours());
  }

  @Test
  public void checkUnreadImportantEmailsCountInResponse() throws IOException, ServletException {
    // This does NOT check that the statistic is correctly calculated.
    // This only checks that the unreadImportantEmails generated from GmailResponseUtility is
    // correctly included in the response
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
    int expected =
        GmailResponseUtility.countImportantEmails(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT);
    Assert.assertEquals(expected, gmailResponse.getUnreadImportantEmails());
  }

  @Test
  public void checkMostFrequentSenderInResponse() throws IOException, ServletException {
    // This does NOT check that the sender is correct.
    // This only checks that the sender generated from GmailResponseUtility is
    // correctly included in the response
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
    String expected =
        GmailResponseUtility.findMostFrequentSender(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME)
            .get();
    Assert.assertEquals(expected, gmailResponse.getSender());
  }
}
