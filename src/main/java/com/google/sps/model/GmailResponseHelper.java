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
import java.util.List;
import java.util.Optional;

/** Contains business logic to calculate statistics for the GmailResponse */
public interface GmailResponseHelper {
  /**
   * Given a list of unread emails from the last NDays, count how many emails are from the last
   * NDays. This method does NOT filter the list or check the timestamps of the messages.
   *
   * @param unreadEmailsFromLastNDays a list of unread emails from the last NDays
   * @return number of emails present in unreadEmailsFromLastNDays
   */
  int countEmailsFromNDays(List<Message> unreadEmailsFromLastNDays);

  /**
   * Get the amount of emails sent within the last mHours hours given a list of messages
   *
   * @param unreadEmailsFromLastNDays a list of Message objects from user's gmail account. Messages
   *     must be either FULL, METADATA, or MINIMAL MessageFormat
   * @return number of emails from last mHours hours
   */
  int countEmailsFromMHours(List<Message> unreadEmailsFromLastNDays, int mHours);

  /**
   * Get the amount of important emails given a list of user's emails. Emails without labelIds are
   * skipped (they may be of the correct format, but there are no label ids, which results in a null
   * return from google for labelIds)
   *
   * @param unreadEmails a list of Message objects from user's gmail account. Messages must be
   *     either FULL, METADATA, or MINIMAL MessageFormat
   * @return number of important emails given list of emails
   */
  int countImportantEmails(List<Message> unreadEmails);

  /**
   * Get the sender of the most unread emails from the last nDays days in a user's Gmail account
   *
   * @param unreadEmails a list of unread emails (with METADATA OR FULL MessageFormat)
   * @return sender of the most unread emails from the last nDays days.
   */
  Optional<String> findMostFrequentSender(List<Message> unreadEmails);
}
