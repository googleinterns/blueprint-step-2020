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
   * Creates a query string for Gmail. Use in search to return emails that fit certain restrictions
   *
   * @param emailAge emails from the last emailAge [emailAgeUnits] will be returned. Set to 0 to
   *     ignore filter
   * @param emailAgeUnits "d" for days, "h" for hours, "" for ignore email
   * @param unreadOnly true if only returning unread emails, false otherwise
   * @param isImportant true if only returning important emails, false otherwise
   * @param from email address of the sender. "" if not specified
   * @return string to use in gmail (either client or API) to find emails that match criteria
   */
  static String emailQueryString(
      int emailAge, String emailAgeUnits, boolean unreadOnly, boolean isImportant, String from) {
    String queryString = "";

    // Add query components
    queryString += emailAgeQuery(emailAge, emailAgeUnits);
    queryString += unreadEmailQuery(unreadOnly);
    queryString += isImportantQuery(isImportant);
    queryString += fromEmailQuery(from);

    // Return multi-part query
    return queryString.trim();
  }

  /**
   * Creates Gmail query for age of emails. Use of months and years not supported as they are out of
   * scope for the application's current purpose (there is no need for it yet)
   *
   * @param emailAge emails from the last emailAge [emailAgeUnits] will be returned. Set to 0 to
   *     ignore filter
   * @param emailAgeUnits "d" for days, "h" for hours, "" for ignore email
   * @return string to use in Gmail (either client or API) to find emails that match these criteria
   *     or null if either one of the arguments are invalid Trailing space added to properties so
   *     multiple queries can be concatenated
   */
  static String emailAgeQuery(int emailAge, String emailAgeUnits) {
    // newer_than:#d where # is an integer will specify to only return emails from last # days
    if (emailAge > 0 && (emailAgeUnits.equals("h") || emailAgeUnits.equals("d"))) {
      return String.format("newer_than:%d%s ", emailAge, emailAgeUnits);
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
   *     Trailing space added to properties so multiple queries can be concatenated
   */
  static String unreadEmailQuery(boolean unreadOnly) {
    // is:unread will return only unread emails
    return unreadOnly ? "is:unread " : "";
  }

  /**
   * Creates Gmail query to find emails from specific sender
   *
   * @param from email address of the sender. "" if not specified
   * @return string to use in Gmail (either client or API) to find emails that match these criteria
   *     Trailing space added to properties so multiple queries can be concatenated
   */
  static String fromEmailQuery(String from) {
    // from: <emailAddress> will return only emails from that sender
    return !from.equals("") ? String.format("from:%s ", from) : "";
  }

  /**
   * Creates Gmail query to find emails marked as important
   *
   * @param isImportant true if filtering for important emails, false otherwise
   * @return string to use in gmail (either client or APi) to find emails that match these criteria
   *     Trailing space added to string so multiple queries can be concatenated
   */
  static String isImportantQuery(boolean isImportant) {
    return isImportant ? "is:important " : "";
  }
}
