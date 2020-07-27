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
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A common base for Gmail tests. Contains constants and auxillary methods required for testing
 * gmail functionality
 */
public interface GmailTestBase {
  String DEFAULT_SENDER = "";
  int DEFAULT_N_DAYS = 7;
  int DEFAULT_M_HOURS = 3;
  int INVALID_M_HOURS = DEFAULT_N_DAYS * 24 + 1;
  int NEGATIVE_N_DAYS = -1;
  int NEGATIVE_M_HOURS = -1;
  long N_DAYS_TIMESTAMP = Instant.now().toEpochMilli() - TimeUnit.DAYS.toMillis(DEFAULT_N_DAYS - 1);
  long M_HOURS_TIMESTAMP =
      Instant.now().toEpochMilli() - TimeUnit.HOURS.toMillis(DEFAULT_M_HOURS - 1);

  String SENDER_ONE_NAME = "Sender_1";
  String SENDER_ONE_EMAIL = "senderOne@sender.com";
  String SENDER_TWO_NAME = "Sender_2";
  String SENDER_TWO_EMAIL = "senderTwo@sender.com";

  MessagePart SENDER_ONE_WITH_CONTACT_NAME_PAYLOAD =
      generateMessagePayload(SENDER_ONE_EMAIL, SENDER_ONE_NAME);
  MessagePart SENDER_ONE_WITHOUT_CONTACT_NAME_PAYLOAD = generateMessagePayload(SENDER_ONE_EMAIL);
  MessagePart SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD =
      generateMessagePayload(SENDER_TWO_EMAIL, SENDER_TWO_NAME);
  MessagePart SENDER_TWO_WITHOUT_CONTACT_NAME_PAYLOAD = generateMessagePayload(SENDER_TWO_EMAIL);

  List<Message> NO_MESSAGES = ImmutableList.of();
  List<Message> SOME_IMPORTANT_MESSAGES_WITH_ONE_UNIMPORTANT =
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
  List<Message> SOME_MESSAGES_HALF_WITHIN_M_HOURS =
      ImmutableList.of(
          new Message()
              .setId("messageFour")
              .setInternalDate(N_DAYS_TIMESTAMP)
              .setPayload(SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD),
          new Message()
              .setId("messageFive")
              .setInternalDate(M_HOURS_TIMESTAMP)
              .setPayload(SENDER_TWO_WITH_CONTACT_NAME_PAYLOAD));
  List<Message> MESSAGES_MAJORITY_SENDER_ONE_WITH_CONTACT_NAME =
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
  List<Message> MESSAGES_MAJORITY_SENDER_ONE_WITHOUT_CONTACT_NAME =
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
  List<Message> MESSAGES_SPLIT_SENDERS_SENDER_ONE_WITH_CONTACT_NAME_MOST_RECENT =
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
}
