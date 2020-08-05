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
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/** Contract for handling/making GET requests to the Gmail API */
public interface GmailClient {
  /**
   * Get all of the message IDs from a user's Gmail account
   *
   * @param query search query to filter which results are returned (see:
   *     https://support.google.com/mail/answer/7190?hl=en)
   * @return a list of messages with IDs and Thread IDs
   * @throws IOException if an issue occurs with the Gmail Service
   */
  List<Message> listUserMessages(String query) throws IOException;

  /**
   * Get a message from a user's Gmail account
   *
   * @param messageId the messageID (retrieved from listUserMessages) of the desired Message
   * @param format MessageFormat enum that defines how much of the Message object is populated
   * @return a Message object with the requested information
   * @throws IOException if an issue occurs with the Gmail service
   */
  Message getUserMessage(String messageId, MessageFormat format) throws IOException;

  // /**
  //  * Get the number of words in a message body
  //  *
  //  * @param message the message we want to get the size from
  //  * @return the number of words in the body of the message
  //  * @throws IOException if an issue occurs with the Gmail service
  //  */
  // int getMessageSize(Message message) throws IOException, MessagingException;

  // String getTextFromMultiPartAlternative(Multipart multipart) throws IOException,
  // MessagingException;

  /**
   * Gets a message from a user's Gmail account. Format assumed to be METADATA. Returned message
   * will only include email message ID, labels, and specified headers
   *
   * @param messageId messageID (retrieved from listUserMessages) of the desired Message
   * @param metadataHeaders list of names of headers (e.g. "From") that should be included
   * @return a Message object with the requested information
   * @throws IOException if an issue occurs with the Gmail service
   */
  Message getUserMessageWithMetadataHeaders(String messageId, List<String> metadataHeaders)
      throws IOException;

  /**
   * Encapsulates possible values for the "format" query parameter in the Gmail GET message method
   */
  enum MessageFormat {
    /** Returns full email message data */
    FULL("full"),

    /** Returns only email message ID, labels, and email headers */
    METADATA("metadata"),

    /**
     * Returns only email message ID and labels; does not return the email headers, body, or
     * payload.
     */
    MINIMAL("minimal"),

    /**
     * Returns the full email message data with body content in the raw field as a base64url encoded
     * string
     */
    RAW("raw");

    public final String formatValue;

    MessageFormat(String formatValue) {
      this.formatValue = formatValue;
    }
  }

  /**
   * Given some search queries, combine them into a larger query. Will not remove
   * duplicates/contradictions. Will behave as if all search queries are concatenated with AND
   * operators. TODO: Refactor query creation using the Builder pattern (Issue #100)
   *
   * @param queries gmail search queries
   * @return String with queries space separated for use in Gmail
   */
  static String combineSearchQueries(String... queries) {
    return String.join(" ", queries);
  }

  /**
   * Get list of unread emails from last nDays days from user's Gmail account
   *
   * @param messageFormat GmailClient.MessageFormat setting to control how much of each message is
   *     returned
   * @param nDays number of days of emails to return
   * @return List of unread messages from last nDays from user's Gmail account with requested level
   *     of information
   * @throws IOException if an issue occurs with the Gmail service
   */
  List<Message> getUnreadEmailsFromNDays(GmailClient.MessageFormat messageFormat, int nDays)
      throws IOException;

  /**
   * Get list of actionable emails that meet specified criteria. Format assumed to be METADATA,
   * Returned message will only include email message ID, labels, and specified headers
   *
   * @param subjectLinePhrases list of words that gmail should look for in the subject line. Emails
   *     will be returned as long as one of the passed phrases are present.
   * @param unreadOnly true if emails must be unread, false otherwise
   * @param nDays emails from the last nDays days will be returned. (Goes by time, not date. E.g. if
   *     nDays is 1, emails from last 24 hours will be returned)
   * @param metadataHeaders list of names of headers (e.g. "From") that should be included
   * @return List of messages that match above criteria
   * @throws IOException if an issue occurs with the Gmail service
   */
  List<Message> getActionableEmails(
      List<String> subjectLinePhrases, boolean unreadOnly, int nDays, List<String> metadataHeaders)
      throws IOException;

  /**
   * Creates Gmail query for age of emails. Use of months and years not supported as they are out of
   * scope for the application's current purpose (there is no need for it yet)
   *
   * @param emailAge emails from the last emailAge [emailAgeUnits] will be returned. Set to 0 to
   *     ignore filter
   * @param emailAgeUnits "d" for days, "h" for hours, "" for ignore email
   * @return string to use in Gmail (either client or API) to find emails that match these criteria
   *     or null if either one of the arguments are invalid
   */
  static String emailAgeQuery(int emailAge, String emailAgeUnits) {
    // newer_than:#d where # is an integer will specify to only return emails from last # days
    if (emailAge > 0 && (emailAgeUnits.equals("h") || emailAgeUnits.equals("d"))) {
      return String.format("newer_than:%d%s", emailAge, emailAgeUnits);
    } else if (emailAge == 0 && emailAgeUnits.isEmpty()) {
      return "";
    }

    // Input invalid
    return null;
  }

  /**
   * Creates Gmail query for unread emails
   *
   * @param unreadOnly true if only unread emails, false otherwise
   * @return string to use in gmail (either client or API) to find emails that match these criteria
   */
  static String unreadEmailQuery(boolean unreadOnly) {
    // is:unread will return only unread emails
    return unreadOnly ? "is:unread" : "";
  }

  /**
   * Creates Gmail query to find emails from specific sender
   *
   * @param from email address of the sender. "" if not specified
   * @return string to use in Gmail (either client or API) to find emails that match these criteria
   */
  static String fromEmailQuery(String from) {
    // from: <emailAddress> will return only emails from that sender
    return !from.equals("") ? String.format("from:%s", from) : "";
  }

  /**
   * Creates Gmail query to find emails marked as important
   *
   * @param isImportant true if filtering for important emails, false otherwise
   * @return string to use in gmail (either client or APi) to find emails that match these criteria
   */
  static String isImportantQuery(boolean isImportant) {
    return isImportant ? "is:important" : "";
  }

  /**
   * Creates Gmail query to find emails with at least one of the passed phrases in the subject line.
   * Phrases will automatically be surrounded with double quotes, if they are not already present.
   *
   * @param phrases phrases to check subject line for.
   * @return query string to use in gmail to find emails that contain at least one of the passed
   *     phrases.
   */
  static String oneOfPhrasesInSubjectLineQuery(List<String> phrases) {
    if (phrases.isEmpty()) {
      return "";
    }

    phrases =
        phrases.stream()
            .map((phrase) -> String.format("\"%s\"", StringUtils.strip(phrase, "\"")))
            .collect(Collectors.toList());

    return String.format("subject:(%s)", String.join(" OR ", phrases));
  }
}
