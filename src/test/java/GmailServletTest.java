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
import com.google.common.collect.ImmutableList;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.servlets.GmailServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  private static final String MESSAGE_ID_ONE = "messageIdOne";
  private static final String MESSAGE_ID_TWO = "messageIdTwo";
  private static final String MESSAGE_ID_THREE = "messageIdThree";
  private static final List<Message> NO_MESSAGES = ImmutableList.of();
  private static final List<Message> THREE_MESSAGES =
      ImmutableList.of(
          new Message().setId(MESSAGE_ID_ONE),
          new Message().setId(MESSAGE_ID_TWO),
          new Message().setId(MESSAGE_ID_THREE));
  private static final String THREE_MESSAGES_JSON =
      String.format(
          "[{\"id\":\"%s\"},{\"id\":\"%s\"},{\"id\":\"%s\"}]",
          MESSAGE_ID_ONE, MESSAGE_ID_TWO, MESSAGE_ID_THREE);
  private static final String NO_MESSAGES_JSON = "[]";

  @BeforeClass
  public static void classInit() throws GeneralSecurityException, IOException {}

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

  @Test
  public void noMessagesPresent() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(NO_MESSAGES);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(NO_MESSAGES_JSON));
  }

  @Test
  public void someMessagesPresent() throws IOException, ServletException {
    Mockito.when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(THREE_MESSAGES);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(THREE_MESSAGES_JSON));
  }
}
