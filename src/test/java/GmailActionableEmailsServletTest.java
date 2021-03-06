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
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sps.model.ActionableMessage;
import com.google.sps.model.ActionableMessageHelper;
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
  private ActionableMessageHelper actionableMessageHelper;
  private GmailClientFactory gmailClientFactory;

  private static final Gson gson = new Gson();

  // Spaces within words are fine (and would be encoded/decoded by request.getParameter)
  private static final String SUBJECT_LINE_PHRASES_STRING = "Action Word One,ActionWordTwo";
  private static final List<String> SUBJECT_LINE_PHRASES_LIST =
      Arrays.asList("Action Word One", "ActionWordTwo");

  private static final List<String> METADATA_HEADERS =
      ImmutableList.of("Subject", "From", "To", "List-ID");

  private static final String MESSAGE_ID_OLD = "messageOne";
  private static final String MESSAGE_ID_NEW = "messageTwo";
  private static final String MESSAGE_ID_LOW_PRIORITY = "messageTwo";
  private static final String MESSAGE_ID_HIGH_PRIORITY = "messageTwo";
  private static final String SUBJECT_VALUE_ONE = "subjectValueOne";
  private static final String SUBJECT_VALUE_TWO = "subjectValueTwo";
  private static final String SENDER_EMAIL = "example@example.com";
  private static final MessagePartHeader subjectHeaderOne =
      new MessagePartHeader().setName("Subject").setValue(SUBJECT_VALUE_ONE);
  private static final MessagePartHeader subjectHeaderTwo =
      new MessagePartHeader().setName("Subject").setValue(SUBJECT_VALUE_TWO);
  private static final MessagePartHeader fromHeader =
      new MessagePartHeader().setName("From").setValue(String.format("<%s>", SENDER_EMAIL));

  private static final long INTERNAL_DATE_EARLY = 1;
  private static final long INTERNAL_DATE_LATE = 2;

  private static final int DEFAULT_N_DAYS = 7;
  private static final int NEGATIVE_N_DAYS = -1;
  private static final ActionableMessage.MessagePriority DEFAULT_PRIORITY =
      ActionableMessage.MessagePriority.LOW;

  private static final Message messageOld =
      new Message()
          .setId(MESSAGE_ID_OLD)
          .setPayload(new MessagePart().setHeaders(ImmutableList.of(subjectHeaderOne, fromHeader)))
          .setInternalDate(INTERNAL_DATE_EARLY);
  private static final Message messageNew =
      new Message()
          .setId(MESSAGE_ID_NEW)
          .setPayload(new MessagePart().setHeaders(ImmutableList.of(subjectHeaderTwo, fromHeader)))
          .setInternalDate(INTERNAL_DATE_LATE);
  private static final Message messageLowPriority =
      new Message()
          .setId(MESSAGE_ID_LOW_PRIORITY)
          .setPayload(new MessagePart().setHeaders(ImmutableList.of(subjectHeaderOne, fromHeader)))
          .setInternalDate(INTERNAL_DATE_LATE);
  private static final Message messageHighPriority =
      new Message()
          .setId(MESSAGE_ID_LOW_PRIORITY)
          .setPayload(new MessagePart().setHeaders(ImmutableList.of(subjectHeaderTwo, fromHeader)))
          .setInternalDate(INTERNAL_DATE_LATE);

  private static final List<Message> MESSAGES_OLDEST_TO_NEWEST =
      ImmutableList.of(messageOld, messageNew);
  private static final List<Message> MESSAGES_PRIORITY_LOWEST_TO_HIGHEST =
      ImmutableList.of(messageLowPriority, messageHighPriority);

  private static final List<ActionableMessage> actionableMessagesNewestToOldest =
      ImmutableList.of(
          new ActionableMessage(
              MESSAGE_ID_NEW,
              SUBJECT_VALUE_TWO,
              INTERNAL_DATE_LATE,
              DEFAULT_PRIORITY,
              SENDER_EMAIL),
          new ActionableMessage(
              MESSAGE_ID_OLD,
              SUBJECT_VALUE_ONE,
              INTERNAL_DATE_EARLY,
              DEFAULT_PRIORITY,
              SENDER_EMAIL));
  private static final List<ActionableMessage> actionableMessagesPriorityHighestToLowest =
      ImmutableList.of(
          new ActionableMessage(
              MESSAGE_ID_HIGH_PRIORITY,
              SUBJECT_VALUE_TWO,
              INTERNAL_DATE_LATE,
              ActionableMessage.MessagePriority.HIGH,
              SENDER_EMAIL),
          new ActionableMessage(
              MESSAGE_ID_LOW_PRIORITY,
              SUBJECT_VALUE_ONE,
              INTERNAL_DATE_LATE,
              ActionableMessage.MessagePriority.LOW,
              SENDER_EMAIL));

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    actionableMessageHelper = Mockito.mock(ActionableMessageHelper.class);
    gmailClientFactory = Mockito.mock(GmailClientFactory.class);
    gmailClient = Mockito.mock(GmailClient.class);
    servlet =
        new GmailActionableEmailsServlet(
            authenticationVerifier, gmailClientFactory, actionableMessageHelper);
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
  public void validResponseSortByDate() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases"))
        .thenReturn(SUBJECT_LINE_PHRASES_STRING);
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(
            actionableMessageHelper.assignMessagePriority(Mockito.any(), Mockito.eq(USER_EMAIL)))
        .thenReturn(DEFAULT_PRIORITY);
    Mockito.when(
            gmailClient.getActionableEmails(
                SUBJECT_LINE_PHRASES_LIST, true, DEFAULT_N_DAYS, METADATA_HEADERS))
        .thenReturn(MESSAGES_OLDEST_TO_NEWEST);

    servlet.doGet(request, response);
    Type type = new TypeToken<List<ActionableMessage>>() {}.getType();
    List<ActionableMessage> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(actionableMessagesNewestToOldest, actual);
  }

  @Test
  public void validResponseSortByPriority() throws Exception {
    Mockito.when(request.getParameter("subjectLinePhrases"))
        .thenReturn(SUBJECT_LINE_PHRASES_STRING);
    Mockito.when(request.getParameter("unreadOnly")).thenReturn(String.valueOf(true));
    Mockito.when(request.getParameter("nDays")).thenReturn(String.valueOf(DEFAULT_N_DAYS));
    Mockito.when(actionableMessageHelper.assignMessagePriority(messageHighPriority, USER_EMAIL))
        .thenReturn(ActionableMessage.MessagePriority.HIGH);
    Mockito.when(actionableMessageHelper.assignMessagePriority(messageLowPriority, USER_EMAIL))
        .thenReturn(ActionableMessage.MessagePriority.LOW);
    Mockito.when(
            gmailClient.getActionableEmails(
                SUBJECT_LINE_PHRASES_LIST, true, DEFAULT_N_DAYS, METADATA_HEADERS))
        .thenReturn(MESSAGES_PRIORITY_LOWEST_TO_HIGHEST);

    servlet.doGet(request, response);
    Type type = new TypeToken<List<ActionableMessage>>() {}.getType();
    List<ActionableMessage> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(actionableMessagesPriorityHighestToLowest, actual);
  }
}
