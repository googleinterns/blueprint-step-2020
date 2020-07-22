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

package com.google.sps.utility;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.sps.exceptions.GmailMessageFormatException;
import com.google.sps.model.GmailClient;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** Contains business logic to calculate statistics for the GmailResponse */
public final class GmailResponseUtility {
  private GmailResponseUtility() {}

  /**
   * Get list of unread emails from last nDays days from user's Gmail account
   *
   * @param gmailClient GmailClient implementation with valid google credential
   * @param messageFormat GmailClient.MessageFormat setting to control how much of each message is
   *     returned
   * @return List of unread messages from last nDays from user's Gmail account with requested level
   *     of information
   * @throws IOException if an issue occurs with the Gmail service
   */
  public static List<Message> getUnreadEmailsFromNDays(
      GmailClient gmailClient, GmailClient.MessageFormat messageFormat, int nDays)
      throws IOException {
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, false, "");
    return gmailClient.listUserMessages(searchQuery).stream()
        .map(
            (message) -> {
              try {
                return gmailClient.getUserMessage(message.getId(), messageFormat);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * Get the amount of emails sent within the last mHours hours given a list of messages
   *
   * @param unreadEmailsFromLastNDays a list of Message objects from user's gmail account. Messages
   *     must be either FULL, METADATA, or MINIMAL MessageFormat
   * @return number of emails from last mHours hours
   * @throws GmailMessageFormatException if the Message objects do not contain an internalDate (and
   *     are thus the wrong format)
   */
  public static int countEmailsFromMHours(List<Message> unreadEmailsFromLastNDays, int mHours) {
    // This is the oldest an email can be (in milliseconds since epoch) to be within the last mHours
    long timeCutoffEpochMs = Instant.now().toEpochMilli() - TimeUnit.HOURS.toMillis(mHours);
    return (int)
        unreadEmailsFromLastNDays.stream()
            .filter(
                (message) -> {
                  if (message.getInternalDate() != 0) {
                    return message.getInternalDate() >= timeCutoffEpochMs;
                  }
                  throw new GmailMessageFormatException(
                      "Messages must be of format MINIMAL, METADATA, or FULL");
                })
            .count();
  }

  /**
   * Get the amount of important emails given a list of user's emails. Emails without labelIds are
   * skipped (they may be of the correct format, but there are no label ids, which results in a null
   * return from google for labelIds)
   *
   * @param unreadEmails a list of Message objects from user's gmail account. Messages must be
   *     either FULL, METADATA, or MINIMAL MessageFormat
   * @return number of important emails given list of emails
   */
  public static int countImportantEmails(List<Message> unreadEmails) {
    return (int)
        unreadEmails.stream()
            .filter(
                (message) ->
                    message.getLabelIds() != null && message.getLabelIds().contains("IMPORTANT"))
            .count();
  }

  /**
   * Get the sender of the most unread emails from the last nDays days in a user's Gmail account
   *
   * @param unreadEmails a list of unread emails (with METADATA OR FULL MessageFormat)
   * @return sender of the most unread emails from the last nDays days. Null if no unread emails.
   * @throws GmailMessageFormatException if the messages do not contain an internal date and/or
   *     headers (and are thus the wrong format)
   */
  public static Optional<String> findMostFrequentSender(List<Message> unreadEmails) {
    if (unreadEmails.isEmpty()) {
      return Optional.empty();
    }

    HashMap<String, Integer> sendersToEmailFrequency = mapSendersToFrequencies(unreadEmails);
    SortByFrequencyThenTimestamp senderComparator =
        new SortByFrequencyThenTimestamp(mapSendersToMostRecentEmail(unreadEmails));

    String mostFrequentSender =
        sendersToEmailFrequency.entrySet().stream()
            .max(senderComparator)
            .map(Map.Entry::getKey)
            .get();

    return Optional.of(parseNameInFromHeader(mostFrequentSender));
  }

  /**
   * Gets a sender's contact name or email address.
   *
   * <p>"From" header values have two possible formats: "<sampleemail@sample.com>" (if name is not
   * available) OR "Sample Name <sampleemail@sample.com>" If a name is available, this is extracted.
   * Otherwise, the email is extracted
   *
   * @param fromHeaderValue the value of a "From" header from which a contact name / email should be
   *     extracted
   * @return A contact name if available, or an email if it is not
   */
  private static String parseNameInFromHeader(String fromHeaderValue) {
    return fromHeaderValue.charAt(0) == '<'
        ? fromHeaderValue.substring(1, fromHeaderValue.length() - 1)
        : fromHeaderValue.split("<")[0].trim();
  }

  /**
   * Associate the senders of emails with how often they sent emails within a given list.
   *
   * @param messages a list of Gmail Messages objects. Must be METADATA or FULL format
   * @return a HashMap where the keys are the values of the "From" header and the values are the
   *     number of times that sender sent an email (from the list of messages)
   */
  private static HashMap<String, Integer> mapSendersToFrequencies(List<Message> messages) {
    HashMap<String, Integer> sendersToFrequencies = new HashMap<>();

    messages.stream()
        .map(GmailResponseUtility::extractFromHeader)
        .forEach(
            (fromHeader) -> {
              String sender = fromHeader.getValue();
              Integer frequency = sendersToFrequencies.getOrDefault(sender, 0);

              sendersToFrequencies.put(sender, frequency + 1);
            });

    return sendersToFrequencies;
  }

  /**
   * Associate the senders of emails with the timestamp of their most recent email within a given
   * list
   *
   * @param messages a list of Gmail Messages objects. Must be MINIMAL, METADATA or FULL format
   * @return a HashMap where the keys are the values of the "From" header and the values are
   *     timestamps of that sender's most recent email
   */
  private static HashMap<String, Long> mapSendersToMostRecentEmail(List<Message> messages) {
    HashMap<String, Long> sendersToTimestamp = new HashMap<>();

    messages.forEach(
        (message) -> {
          MessagePartHeader fromHeader = extractFromHeader(message);

          String sender = fromHeader.getValue();
          long newEmailTimestamp = message.getInternalDate();
          long latestEmailFromSenderTimestamp = sendersToTimestamp.getOrDefault(sender, (long) 0);

          if (newEmailTimestamp == 0) {
            throw new GmailMessageFormatException(
                "Messages must be MINIMAL, METADATA, or FULL format");
          }

          if (newEmailTimestamp > latestEmailFromSenderTimestamp) {
            sendersToTimestamp.put(sender, newEmailTimestamp);
          }
        });

    return sendersToTimestamp;
  }

  /**
   * Given a list of message headers, extract the "From" header
   *
   * @param message a Gmail message object. Must be METADATA or FULL format
   * @return "From" header
   * @throws GmailMessageFormatException if the messages do not contain a from header (and are thus
   *     the wrong format)
   */
  private static MessagePartHeader extractFromHeader(Message message) {
    List<MessagePartHeader> headers = message.getPayload().getHeaders();

    return headers.stream()
        .filter((header) -> header.getName().equals("From"))
        .findFirst()
        .orElseThrow(
            () -> new GmailMessageFormatException("Messages must be METADATA or FULL format"));
  }

  /** Private comparator class to compare entries of sender hashmaps */
  private static class SortByFrequencyThenTimestamp
      implements Comparator<Map.Entry<String, Integer>> {
    private final HashMap<String, Long> sendersToMostRecentEmailTimestamp;

    public SortByFrequencyThenTimestamp(HashMap<String, Long> sendersToMostRecentEmailTimestamp) {
      this.sendersToMostRecentEmailTimestamp = sendersToMostRecentEmailTimestamp;
    }

    /**
     * Compares sender frequency hashmaps based on 1) how many emails sent and then, as a
     * tiebreaker, 2) which sender sent an email most recently (based on map passed to class on
     * initialization)
     *
     * @param entryOne the first entry object
     * @param entryTwo the second entry object
     * @return 1 if entryOne sent the most emails or (if a tie) sent an email most recently. -1 in
     *     opposite case. 0 if both entires have the same number of sent emails and same timestamp
     *     of most recent email (rare - timestamps are in milliseconds)
     */
    @Override
    public int compare(Map.Entry<String, Integer> entryOne, Map.Entry<String, Integer> entryTwo) {
      if (entryOne.getValue().equals(entryTwo.getValue())) {
        long currentRecordTimestamp = sendersToMostRecentEmailTimestamp.get(entryOne.getKey());
        long newEntryTimestamp = sendersToMostRecentEmailTimestamp.get(entryTwo.getKey());

        return Long.compare(currentRecordTimestamp, newEntryTimestamp);
      }

      return Integer.compare(entryOne.getValue(), entryTwo.getValue());
    }
  }
}
