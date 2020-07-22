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
   * @param unreadEmailsDays # of unread emails from last n days
   * @param unreadEmailsHours # of unread emails from last m hours
   * @param unreadImportantEmails # of unread emails that are important from last n days
   * @param sender Stores the name (or email address if name isn't available) of person who sent the
   *     most unread emails in the last n days.
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
}
