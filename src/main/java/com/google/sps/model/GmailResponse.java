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

package com.google.sps.model;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.sps.exceptions.GmailMessageFormatException;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains the summary gmail information that should be passed to the client, as well as the
 * methods to generate these statistics.
 */
public class GmailResponse {
  private final int nDays;
  private final int mHours;

  // Stores # of unread emails from last n days
  private final int unreadEmailsDays;

  // Stores # of unread emails from last m hours
  private final int unreadEmailsHours;

  // Stores # of important unread emails from last n days
  private final int unreadImportantEmails;

  // Stores the name (or email address if name isn't available) of person who sent the most
  // unread emails in the last n days.
  private final String sender;

  /**
   * Create a GmailResponse instance
   *
   * @param nDays number of days relevant statistics are based on
   * @param mHours number of hours relevant statistics are based on
   * @param gmailClient GmailClient implementation with valid google credential. Will be used to
   *     compute other statistics for GmailResponse.
   */
  public GmailResponse(int nDays, int mHours, GmailClient gmailClient) throws IOException {
    this.nDays = nDays;
    this.mHours = mHours;

    // TODO: Perform these operations faster (use multithreading, batching, etc) (Issue #84)
    GmailClient.MessageFormat messageFormat = GmailClient.MessageFormat.METADATA;
    List<Message> unreadMessages = getUnreadEmailsFromNDays(gmailClient, messageFormat);
    this.unreadEmailsDays = unreadMessages.size();
    this.unreadEmailsHours = findUnreadEmailCountHours(unreadMessages);
    this.unreadImportantEmails = findImportantUnreadEmailCount(unreadMessages);
    this.sender = findMostFrequentSender(unreadMessages);
  }

  public int getNDays() {
    return nDays;
  }

  public int getMHours() {
    return mHours;
  }

  public int getUnreadEmailsDays() {
    return unreadEmailsDays;
  }

  public int getUnreadEmailsHours() {
    return unreadEmailsHours;
  }

  public int getUnreadImportantEmails() {
    return unreadImportantEmails;
  }

  public String getSender() {
    return sender;
  }

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
  private List<Message> getUnreadEmailsFromNDays(
      GmailClient gmailClient, GmailClient.MessageFormat messageFormat) throws IOException {
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
   * Get the amount of unread emails from the last m hours given a list of user's unread emails from
   * last nDays. (nDays >= mHours * 24)
   *
   * @param unreadEmails a list of Message objects from user's gmail account. Messages must be
   *     either FULL, METADATA, or MINIMAL MessageFormat
   * @return number of unread emails from last mHours hours
   * @throws GmailMessageFormatException if the Message objects do not contain an internalDate (and
   *     are thus the wrong format)
   */
  private int findUnreadEmailCountHours(List<Message> unreadEmails)
      throws GmailMessageFormatException {
    // This is the oldest an email can be (in milliseconds since epoch) to be within the last mHours
    long timeCutoffEpochMs = Instant.now().toEpochMilli() - mHours * 60 * 60 * 1000;
    return (int)
        unreadEmails.stream()
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
   * Get the amount of unread, important emails from the last nDays given a list of user's unread
   * emails. Emails without labelIds are skipped (they may be of the correct format, but there are
   * no label ids, which results in a null return from google for labelIds)
   *
   * @param unreadEmails a list of Message objects from user's gmail account. Messages must be
   *     either FULL, METADATA, or MINIMAL MessageFormat
   * @return number of unread, important emails from last nDays days
   */
  private int findImportantUnreadEmailCount(List<Message> unreadEmails) {
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
   * @return sender of the most unread emails from the last nDays days
   * @throws GmailMessageFormatException if the messages do not contain an internal date and/or
   *     headers (and are thus the wrong format)
   */
  private String findMostFrequentSender(List<Message> unreadEmails)
      throws GmailMessageFormatException {
    // This data structure is used in the hashmap, to associate two values (how many times
    // the sender sent emails in the last nDays, and when their most recent sent email was) with
    // the sender's email in the hashmap.
    class EmailFrequencyWithDate {
      private int timesSent;
      private long mostRecentEmailTimestamp;

      /**
       * Create a new EmailFrequencyWithDate instance
       *
       * @param timestamp timestamp of email sent in milliseconds since epoch
       */
      public EmailFrequencyWithDate(long timestamp) {
        timesSent = 1;
        this.mostRecentEmailTimestamp = timestamp;
      }

      public int getTimesSent() {
        return timesSent;
      }

      public long getMostRecentEmailTimestamp() {
        return mostRecentEmailTimestamp;
      }

      /**
       * Updates the mostRecentEmailTimestamp if the passed timestamp is later than the currently
       * stored one.
       *
       * @param timestamp timestamp of email sent in milliseconds since epoch
       */
      public void updateMostRecentEmailTimestamp(long timestamp) {
        if (timestamp > mostRecentEmailTimestamp) {
          this.mostRecentEmailTimestamp = timestamp;
        }
      }

      public void incrementTimesSent() {
        timesSent++;
      }
    }

    // If there are no emails, return "" (the default value for sender)
    if (unreadEmails.isEmpty()) {
      return "";
    }

    // Find out which sender sent the most unread emails.
    // This is extracted from the "From" headers within messages (which are always present in a
    // valid email)
    // Store the senders of emails in a hashmap paired with 1) how often they sent emails in the
    // last nDays and 2) what the timestamp was of their most recent email.
    HashMap<String, EmailFrequencyWithDate> senders = new HashMap<>();
    unreadEmails.forEach(
        (Message m) -> {
          long timestamp = m.getInternalDate();
          MessagePart messagePayload = m.getPayload();

          if (messagePayload == null || timestamp == 0) {
            throw new GmailMessageFormatException("Messages must be of format METADATA or FULL");
          }

          String sender =
              messagePayload.getHeaders().stream()
                  .filter((MessagePartHeader header) -> header.getName().equals("From"))
                  .findFirst()
                  .get()
                  .getValue();

          if (senders.get(sender) == null) {
            EmailFrequencyWithDate newEmailFrequencyWithDate =
                new EmailFrequencyWithDate(timestamp);
            senders.put(sender, newEmailFrequencyWithDate);
          } else {
            EmailFrequencyWithDate updatedEmailFrequencyWithDate = senders.get(sender);
            updatedEmailFrequencyWithDate.incrementTimesSent();
            updatedEmailFrequencyWithDate.updateMostRecentEmailTimestamp(timestamp);
            senders.replace(sender, updatedEmailFrequencyWithDate);
          }
        });

    // Senders cannot be empty, as all emails have a "From" header and
    // there is at least one email in unreadEmails.
    // Identify the sender who sent the most emails. In the case of a tie,
    // choose the sender who sent an email most recently. In case of another tie
    // (i.e. two senders with same send frequency and same timestamp),
    // return either sender (this case doesn't need to be handled)
    String headerValue =
        senders.entrySet().stream()
            .reduce(
                (a, b) -> {
                  if (a.getValue().getTimesSent() > b.getValue().getTimesSent()) {
                    return a;
                  } else if (b.getValue().getTimesSent() > a.getValue().getTimesSent()) {
                    return b;
                  } else {
                    if (a.getValue().getMostRecentEmailTimestamp()
                        > b.getValue().getMostRecentEmailTimestamp()) {
                      return a;
                    }
                    return b;
                  }
                })
            .get()
            .getKey();

    // "From" headers have two possible formats:
    // <sampleemail@sample.com> (if name is not available)
    // OR
    // Sample Sender <sampleemail@sample.com>
    // If a name is available, this is extracted. Otherwise, the email is extracted
    return headerValue.charAt(0) == '<'
        ? headerValue.substring(1, headerValue.length() - 1)
        : headerValue.split("<")[0].trim();
  }
}
