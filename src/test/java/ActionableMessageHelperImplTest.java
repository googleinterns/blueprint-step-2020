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
import com.google.sps.exceptions.GmailMessageFormatException;
import com.google.sps.model.ActionableMessage;
import com.google.sps.model.ActionableMessageHelper;
import com.google.sps.model.ActionableMessageHelperImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.List;

/**
 * Tests the ActionableMessageHelperImpl and its helper methods.
 */
@RunWith(JUnit4.class)
public class ActionableMessageHelperImplTest {
  private static final ActionableMessageHelper actionableMessageHelperImpl = new ActionableMessageHelperImpl();

  private static final List<String> LABELS_STARRED = ImmutableList.of("STARRED");
  private static final List<String> LABELS_IMPORTANT = ImmutableList.of("IMPORTANT");
  private static final String USER_EMAIL = "example@example.com";
  private static final String NOT_USER_EMAIL = "fake@fake.com";
  private static final String TO_HEADER_VALUE_USER_EMAIL = String.format("<%s>", USER_EMAIL);
  private static final String TO_HEADER_VALUE_NOT_USER_EMAIL = String.format("<%s>", NOT_USER_EMAIL);
  private static final String TO_HEADER_VALUE_UNDISCLOSED_RECIPIENTS = ActionableMessageHelperImpl.UNDISCLOSED_RECIPIENTS_TO_HEADER;
  private static final String TO_HEADER_VALUE_TOO_MANY_RECIPIENTS = createHeaderWithNRecipients(ActionableMessageHelperImpl.MAILING_LIST_LOWER_BOUND, TO_HEADER_VALUE_USER_EMAIL, ", ");
  private static final MessagePartHeader toHeaderUserEmail = new MessagePartHeader().setName("To").setValue(TO_HEADER_VALUE_USER_EMAIL);
  private static final MessagePartHeader toHeaderNotUserEmail = new MessagePartHeader().setName("To").setValue(TO_HEADER_VALUE_NOT_USER_EMAIL);
  private static final MessagePartHeader toHeaderUndisclosedRecipients = new MessagePartHeader().setName("To").setValue(TO_HEADER_VALUE_UNDISCLOSED_RECIPIENTS);
  private static final MessagePartHeader toHeaderTooManyRecipients = new MessagePartHeader().setName("To").setValue(TO_HEADER_VALUE_TOO_MANY_RECIPIENTS);

  private static final Message starredMessage =
      new Message()
          .setPayload(
              new MessagePart()
                  .setHeaders(
                      Collections.singletonList(toHeaderNotUserEmail)))
          .setLabelIds(LABELS_STARRED);
  private static final Message importantMessage =
      new Message()
          .setPayload(
              new MessagePart()
                  .setHeaders(
                      Collections.singletonList(toHeaderNotUserEmail)))
          .setLabelIds(LABELS_IMPORTANT);
  private static final Message undisclosedRecipientsMessage =
      new Message()
          .setPayload(
              new MessagePart()
                  .setHeaders(
                      Collections.singletonList(toHeaderUndisclosedRecipients)));
  private static final Message tooManyRecipientsMessage =
      new Message()
          .setPayload(
              new MessagePart()
                  .setHeaders(
                      Collections.singletonList(toHeaderTooManyRecipients)));
  private static final Message notUserEmailMessage =
      new Message()
          .setPayload(
              new MessagePart()
                  .setHeaders(
                      Collections.singletonList(toHeaderNotUserEmail)));
  private static final Message userEmailMessage =
      new Message()
          .setPayload(
              new MessagePart()
                  .setHeaders(
                      Collections.singletonList(toHeaderUserEmail)));
  private static final Message messageWithoutToHeader = new Message();

  private static String createHeaderWithNRecipients(int n, String headerValue, String delimiter) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i<n; i++) {
      stringBuilder.append(headerValue).append(delimiter);
    }

    return stringBuilder.toString();
  }

  @Test(expected = GmailMessageFormatException.class)
  public void getPriorityNoHeaders() {
    actionableMessageHelperImpl.assignMessagePriority(messageWithoutToHeader, USER_EMAIL);
  }

  @Test
  public void getPriorityStarredMessage() {
    ActionableMessage.MessagePriority actualPriority =
        actionableMessageHelperImpl.assignMessagePriority(starredMessage, USER_EMAIL);
    Assert.assertEquals(ActionableMessage.MessagePriority.HIGH, actualPriority);
  }

  @Test
  public void getPriorityImportantMessage() {
    ActionableMessage.MessagePriority actualPriority =
        actionableMessageHelperImpl.assignMessagePriority(importantMessage, USER_EMAIL);
    Assert.assertEquals(ActionableMessage.MessagePriority.MEDIUM, actualPriority);
  }

  @Test
  public void getPrioritySentToUserMessage() {
    ActionableMessage.MessagePriority actualPriority =
        actionableMessageHelperImpl.assignMessagePriority(userEmailMessage, USER_EMAIL);
    Assert.assertEquals(ActionableMessage.MessagePriority.MEDIUM, actualPriority);
  }

  @Test
  public void getPriorityNotSentToUserMessage() {
    ActionableMessage.MessagePriority actualPriority =
        actionableMessageHelperImpl.assignMessagePriority(notUserEmailMessage, USER_EMAIL);
    Assert.assertEquals(ActionableMessage.MessagePriority.LOW, actualPriority);
  }

  @Test
  public void getPriorityUndisclosedRecipientsMessage() {
    ActionableMessage.MessagePriority actualPriority =
        actionableMessageHelperImpl.assignMessagePriority(undisclosedRecipientsMessage, USER_EMAIL);
    Assert.assertEquals(ActionableMessage.MessagePriority.LOW, actualPriority);
  }

  @Test
  public void getPrioritySentToTooManyUsersMessage() {
    ActionableMessage.MessagePriority actualPriority =
        actionableMessageHelperImpl.assignMessagePriority(tooManyRecipientsMessage, USER_EMAIL);
    Assert.assertEquals(ActionableMessage.MessagePriority.LOW, actualPriority);
  }
}
