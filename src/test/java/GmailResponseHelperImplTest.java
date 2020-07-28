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
import com.google.sps.model.GmailResponseHelper;
import com.google.sps.model.GmailResponseHelperImpl;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the GmailResponseHelperImpl and ensures that statistics for the GmailResponse are
 * calculated correctly
 */
public class GmailResponseHelperImplTest {
  private static final GmailResponseHelper gmailResponseHelper = new GmailResponseHelperImpl();

  private static final int DEFAULT_N_DAYS = 7;
  private static final int DEFAULT_M_HOURS = 3;
  private static final long N_DAYS_TIMESTAMP =
      Instant.now().toEpochMilli() - TimeUnit.DAYS.toMillis(DEFAULT_N_DAYS - 1);
  private static final long M_HOURS_TIMESTAMP =
      Instant.now().toEpochMilli() - TimeUnit.HOURS.toMillis(DEFAULT_M_HOURS - 1);

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

  /**
   * Auxiliary method to get a Message payload (with a "From" header) given a sender's email. From
   * header in the form of: "From": "<email@email.com>"
   *
   * @param email the sender's email
   * @return a MessagePart instance that can be used as the payload of a Message
   */
  static MessagePart generateMessagePayload(String email) {
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
  static MessagePart generateMessagePayload(String email, String contactName) {
    return new MessagePart()
        .setHeaders(
            Collections.singletonList(
                new MessagePartHeader()
                    .setName("From")
                    .setValue(String.format("%s <%s>", contactName, email))));
  }

  @Test
  public void emptyListUnreadEmailsNDays() {
    int unreadEmailsNDays = gmailResponseHelper.countEmailsFromNDays(NO_MESSAGES);
    Assert.assertEquals(0, unreadEmailsNDays);
  }

  @Test
  public void emptyListUnreadEmailsMHours() {
    int unreadEmailsMHours =
        gmailResponseHelper.countEmailsFromMHours(NO_MESSAGES, DEFAULT_M_HOURS);
    Assert.assertEquals(0, unreadEmailsMHours);
  }

  @Test
  public void emptyListUnreadImportantEmailsFromNDays() {
    int unreadImportantEmails = gmailResponseHelper.countImportantEmails(NO_MESSAGES);
    Assert.assertEquals(0, unreadImportantEmails);
  }

  @Test
  public void emptyListFindMostFrequentSender() {
    Optional<String> sender = gmailResponseHelper.findMostFrequentSender(NO_MESSAGES);
    Assert.assertFalse(sender.isPresent());
  }

  @Test
  public void calculateUnreadEmailsNDays() {
    int unreadEmailsNDays =
        gmailResponseHelper.countEmailsFromNDays(SOME_MESSAGES_HALF_WITHIN_M_HOURS);
    Assert.assertEquals(SOME_MESSAGES_HALF_WITHIN_M_HOURS.size(), unreadEmailsNDays);
  }

  @Test
  public void calculateUnreadEmailsMHours() {
    int unreadEmailsMHours =
        gmailResponseHelper.countEmailsFromMHours(
            SOME_MESSAGES_HALF_WITHIN_M_HOURS, DEFAULT_M_HOURS);
    Assert.assertEquals(SOME_MESSAGES_HALF_WITHIN_M_HOURS.size(), unreadEmailsMHours * 2);
  }

  @Test
  public void calculateUnreadImportantEmailsFromNDays() {
    int numberOfImportantEmails =
        gmailResponseHelper.countImportantEmails(SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT);
    Assert.assertEquals(
        SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT.size() - 1, numberOfImportantEmails);
  }

  @Test
  public void getMostFrequentSenderWhenContactNameIsPresent() {
    String senderName =
        gmailResponseHelper
            .findMostFrequentSender(MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME)
            .get();
    Assert.assertEquals(SENDER_ONE_NAME, senderName);
  }

  @Test
  public void getMostFrequentSenderWhenOnlyEmailIsPresent() {
    String senderName =
        gmailResponseHelper
            .findMostFrequentSender(MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME)
            .get();
    Assert.assertEquals(SENDER_ONE_EMAIL, senderName);
  }

  @Test
  public void checkSenderEqualFrequenciesPreferMostRecentSender() {
    // This is the case when there is an equal amount of sent messages from two senders
    // (i.e. 2 from sender A, 2 from sender B).
    // The sender who sent the latest/most recent contact should be returned.
    // NOTE: in the case that the frequencies are tied, and they both sent emails at the exact same
    // time
    // (which is absurdly rare), the system can return either of the senders (it won't matter to the
    // user).
    // Thus, that circumstance will not be tested.

    String senderName =
        gmailResponseHelper
            .findMostFrequentSender(MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT)
            .get();
    Assert.assertEquals(SENDER_ONE_NAME, senderName);
  }
}
