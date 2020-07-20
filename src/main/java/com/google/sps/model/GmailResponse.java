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
import com.google.api.services.gmail.model.MessagePartHeader;
import java.io.IOException;
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
  private final int unreadEmailsDays;
  private final int unreadEmailsHours;
  private final int unreadImportantEmails;
  private final String sender;

  /**
   * Create a GmailResponse instance
   *
   * @param nDays number of days relevant statistics are based on
   * @param mHours number of hours relevant statistics are based on
   * @param unreadEmailsDays how many unread emails user has from last nDays days
   * @param unreadEmailsHours how many unread emails user has from last mHours hours
   * @param unreadImportantEmails how many unread, important emails user has from last nDays days
   * @param sender who sent the most unread emails from last nDays. Either name (if available) or
   *     email address (if name not available)
   */
  public GmailResponse(
      int nDays,
      int mHours,
      int unreadEmailsDays,
      int unreadEmailsHours,
      int unreadImportantEmails,
      String sender) {
    this.nDays = nDays;
    this.mHours = mHours;
    this.unreadEmailsDays = unreadEmailsDays;
    this.unreadEmailsHours = unreadEmailsHours;
    this.unreadImportantEmails = unreadImportantEmails;
    this.sender = sender;
  }

  public GmailResponse(int nDays, int mHours, GmailClient gmailClient) throws IOException {
    this.nDays = nDays;
    this.mHours = mHours;

    // TODO: Perform these operations faster (use multithreading, batching, etc) (Issue #84)
    this.unreadEmailsDays = findUnreadEmailCountDays(gmailClient);
    this.unreadEmailsHours = findUnreadEmailCountHours(gmailClient);
    this.unreadImportantEmails = findImportantUnreadEmailCount(gmailClient);
    this.sender = findMostFrequentSender(gmailClient);
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
   * Get the amount of unread emails from the last nDays days in a user's Gmail account
   *
   * @param gmailClient Gmail service with valid google credential
   * @return number of unread emails from last nDays days
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private int findUnreadEmailCountDays(GmailClient gmailClient) throws IOException {
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, false, "");
    return gmailClient.listUserMessages(searchQuery).size();
  }

  /**
   * Get the amount of unread emails from the last mHours hours in a user's Gmail account
   *
   * @param gmailClient Gmail service with valid google credential
   * @return number of unread emails from last mHours hours
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private int findUnreadEmailCountHours(GmailClient gmailClient) throws IOException {
    String searchQuery = GmailClient.emailQueryString(mHours, "h", true, false, "");
    return gmailClient.listUserMessages(searchQuery).size();
  }

  /**
   * Get the amount of unread, important emails from the last nDays days in a user's Gmail account
   *
   * @param gmailClient Gmail service with valid google credential
   * @return number of unread, important emails from last nDays days
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private int findImportantUnreadEmailCount(GmailClient gmailClient) throws IOException {
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, true, "");
    return gmailClient.listUserMessages(searchQuery).size();
  }

  /**
   * Get the sender of the most unread emails from the last nDays days in a user's Gmail account
   *
   * @param gmailClient Gmail service with valid google credential
   * @return sender of the most unread emails from the last nDays days
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private String findMostFrequentSender(GmailClient gmailClient) throws IOException {
    class EmailFrequencyWithDate {
      private int timesSent;
      private long mostRecentEmailTimestamp;

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

      public void updateMostRecentEmailTimestamp(long timestamp) {
        if (timestamp > mostRecentEmailTimestamp) {
          this.mostRecentEmailTimestamp = timestamp;
        }
      }

      public void incrementTimesSent() {
        timesSent++;
      }
    }

    // Get user's messages - if none present, return "" as there is no sender to extract
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, false, "");
    GmailClient.MessageFormat messageFormat = GmailClient.MessageFormat.METADATA;
    List<Message> unreadEmails;
    unreadEmails =
        gmailClient.listUserMessages(searchQuery).stream()
            .map(
                (Message m) -> {
                  try {
                    return gmailClient.getUserMessage(m.getId(), messageFormat);
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toList());
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

          List<MessagePartHeader> senderList = m.getPayload().getHeaders();
          String sender =
              senderList.stream()
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
    // identify the sender who sent an email most recently. In case of another tie
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
