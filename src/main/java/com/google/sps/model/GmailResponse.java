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
 * methods to generate these statistics. Properties only available through reflective access (i.e.
 * gson, etc)
 */
public class GmailResponse {
  private int nDays;
  private int mHours;
  private int unreadEmailsDays;
  private int unreadEmailsHours;
  private int unreadImportantEmails;
  private String sender;

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
