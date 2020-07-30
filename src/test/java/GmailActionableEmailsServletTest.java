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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.servlets.GmailActionableEmailsServlet;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Tests the GmailActionableEmailsServlet */
@RunWith(JUnit4.class)
public class GmailActionableEmailsServletTest extends AuthenticatedServletTestBase {
  private GmailClient gmailClient;
  private GmailActionableEmailsServlet servlet;

  private static final Gson gson = new Gson();

  // Spaces within words are fine (and would be encoded/decoded by request.getParameter)

  private static final String SUBJECT_LINE_PHRASES_STRING = "\"Action Word One\",\"ActionWordTwo\"";
  private static final List<String> SUBJECT_LINE_PHRASES_LIST =
      Arrays.asList("\"Action Word One\"", "\"ActionWordTwo\"");

  private static final GmailClient.MessageFormat messageFormat = GmailClient.MessageFormat.FULL;

  int DEFAULT_N_DAYS = 7;
  int NEGATIVE_N_DAYS = -1;

  List<Message> SOME_MESSAGES =
      ImmutableList.of(new Message().setId("messageFour"), new Message().setId("messageFive"));

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    GmailClientFactory gmailClientFactory = Mockito.mock(GmailClientFactory.class);
    gmailClient = Mockito.mock(GmailClient.class);
    servlet = new GmailActionableEmailsServlet(authenticationVerifier, gmailClientFactory);
    Mockito.when(gmailClientFactory.getGmailClient(Mockito.any())).thenReturn(gmailClient);
  }

  @Test
  public void subjectLinePhrasesNull() throws Exception {
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void nDaysNull() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases"))
        .thenReturn(SUBJECT_LINE_PHRASES_STRING);
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void subjectLinePhrasesEmpty() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases")).thenReturn("");
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void nDaysEmpty() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases"))
        .thenReturn(SUBJECT_LINE_PHRASES_STRING);
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn("");

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void nDaysNegative() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases"))
        .thenReturn(SUBJECT_LINE_PHRASES_STRING);
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(NEGATIVE_N_DAYS));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void nDaysZero() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases"))
        .thenReturn(SUBJECT_LINE_PHRASES_STRING);
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(0));

    servlet.doGet(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void validResponse() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases"))
        .thenReturn(SUBJECT_LINE_PHRASES_STRING);
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));

    Mockito.when(
            gmailClient.getActionableEmails(
                messageFormat, SUBJECT_LINE_PHRASES_LIST, true, DEFAULT_N_DAYS))
        .thenReturn(SOME_MESSAGES);

    servlet.doGet(request, response);
    Type type = new TypeToken<List<Message>>() {}.getType();
    List<Message> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(200, response.getStatus());
    // Assert.assertEquals(SOME_MESSAGES, actual);
  }
}
