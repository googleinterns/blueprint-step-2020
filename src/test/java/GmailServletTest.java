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
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClient.MessageFormat;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailResponse;
import com.google.sps.model.GmailResponseHelper;
import com.google.sps.servlets.GmailServlet;
import java.util.Optional;
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
 * AuthenticatedHttpServlet and GmailResponseHelperImplTest is functioning properly (those tests
 * will fail otherwise).
 */
@RunWith(JUnit4.class)
public final class GmailServletTest extends AuthenticatedServletTestBase implements GmailTestBase {
  private GmailClient gmailClient;
  private GmailServlet servlet;
  private GmailResponseHelper gmailResponseHelper;

  private static final Gson gson = new Gson();

  private static final int EXPECTED_IMPORTANT_EMAIL_COUNT = 2;
  private static final int EXPECTED_EMAILS_M_HOURS_COUNT = 2;

  private static final GmailClient.MessageFormat messageFormat = MessageFormat.METADATA;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    GmailClientFactory gmailClientFactory = Mockito.mock(GmailClientFactory.class);
    gmailClient = Mockito.mock(GmailClient.class);
    gmailResponseHelper = Mockito.mock(GmailResponseHelper.class);
    servlet = new GmailServlet(authenticationVerifier, gmailClientFactory, gmailResponseHelper);
    Mockito.when(gmailClientFactory.getGmailClient(Mockito.any())).thenReturn(gmailClient);
  }

  /**
   * Auxiliary method to get a GmailResponse object from a servlet when using doGet
   *
   * @param request Mock HttpRequest
   * @param response Mock HttpResponse
   * @return GmailResponse object from doGet method
   */
  private GmailResponse getGmailResponse(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    servlet.doGet(request, response);
    return gson.fromJson(stringWriter.toString(), GmailResponse.class);
  }

  @Test
  public void noNDaysParameter() throws Exception {
    Mockito.when(request.getParameter("nDays")).thenReturn(null);
    Mockito.when(request.getParameter("mHours")).thenReturn(String.valueOf(DEFAULT_M_HOURS));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void noMHoursParameter() throws Exception {
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_M_HOURS));
    Mockito.when(request.getParameter("mHours")).thenReturn(null);

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void negativeNDaysParameter() throws Exception {
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(NEGATIVE_N_DAYS));
    Mockito.when(request.getParameter("mHours")).thenReturn(String.valueOf(DEFAULT_M_HOURS));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void negativeMHoursParameter() throws Exception {
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter("mHours")).thenReturn(String.valueOf(NEGATIVE_M_HOURS));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void zeroNDaysParameter() throws Exception {
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(0));
    Mockito.when(request.getParameter("mHours")).thenReturn(String.valueOf(0));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void zeroMHoursParameter() throws Exception {
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter("mHours")).thenReturn(String.valueOf(0));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void invalidMHoursParameter() throws Exception {
    // mHours must represent less time than nDays
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter("mHours")).thenReturn(String.valueOf(INVALID_M_HOURS));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void checkDefaultSenderInNullCase() throws Exception {
    // When mostFrequentSender is N/A (in case of no messages), response should be some
    // default value, not null.
    Mockito.when(request.getParameter(Mockito.eq("nDays")))
        .thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter(Mockito.eq("mHours")))
        .thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.getUnreadEmailsFromNDays(messageFormat, DEFAULT_N_DAYS))
        .thenReturn(NO_MESSAGES);

    Mockito.when(gmailResponseHelper.findMostFrequentSender(NO_MESSAGES))
        .thenReturn(Optional.empty());

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(DEFAULT_SENDER, gmailResponse.getSender());
  }

  @Test
  public void checkResponseParsing() throws Exception {
    // This does NOT check that the statistics are correctly calculated.
    // This only checks that the statistics from GmailResponseHelper
    // are correctly included in the response
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(request.getParameter("mHours")).thenReturn(String.valueOf(DEFAULT_M_HOURS));

    Mockito.when(gmailClient.getUnreadEmailsFromNDays(messageFormat, DEFAULT_N_DAYS))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS);

    Mockito.when(gmailResponseHelper.countEmailsFromNDays(SOME_MESSAGES_HALF_WITHIN_M_HOURS))
        .thenReturn(SOME_MESSAGES_HALF_WITHIN_M_HOURS.size());
    Mockito.when(
            gmailResponseHelper.countEmailsFromMHours(
                SOME_MESSAGES_HALF_WITHIN_M_HOURS, DEFAULT_M_HOURS))
        .thenReturn(EXPECTED_EMAILS_M_HOURS_COUNT);
    Mockito.when(gmailResponseHelper.countImportantEmails(SOME_MESSAGES_HALF_WITHIN_M_HOURS))
        .thenReturn(EXPECTED_IMPORTANT_EMAIL_COUNT);
    Mockito.when(gmailResponseHelper.findMostFrequentSender(SOME_MESSAGES_HALF_WITHIN_M_HOURS))
        .thenReturn(Optional.of(SENDER_ONE_NAME));

    GmailResponse gmailResponse = getGmailResponse(request, response);
    Assert.assertEquals(
        SOME_MESSAGES_HALF_WITHIN_M_HOURS.size(), gmailResponse.getUnreadEmailsDays());
    Assert.assertEquals(EXPECTED_EMAILS_M_HOURS_COUNT, gmailResponse.getUnreadEmailsHours());
    Assert.assertEquals(EXPECTED_IMPORTANT_EMAIL_COUNT, gmailResponse.getUnreadImportantEmails());
    Assert.assertEquals(SENDER_ONE_NAME, gmailResponse.getSender());
  }
}
