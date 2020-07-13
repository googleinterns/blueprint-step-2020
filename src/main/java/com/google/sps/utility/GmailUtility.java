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

/** Contains logic that transforms and creates Gmail data structures (no network requests) */
public final class GmailUtility {
  private GmailUtility() {}

  /**
   * Creates a query string for Gmail. Use in search to return emails that fit certain restrictions
   *
   * @param emailAge emails from the last emailAge [emailAgeUnits] will be returned. Set to 0 to
   *     ignore filter
   * @param emailAgeUnits "d" for days, "h" for hours, "" for ignore email
   * @param unreadOnly true if only returning unread emails, false otherwise
   * @param from email address of the sender. "" if not specified
   * @return string to use in gmail (either client or API) to find emails that match criteria
   */
  public static String emailQueryString(
      int emailAge, String emailAgeUnits, boolean unreadOnly, String from) {
    String queryString = "";

    // Add query components
    queryString += emailAgeQuery(emailAge, emailAgeUnits);
    queryString += unreadEmailQuery(unreadOnly);
    queryString += fromEmailQuery(from);

    // Return multi-part query
    return queryString;
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
  public static String emailAgeQuery(int emailAge, String emailAgeUnits) {
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
  public static String unreadEmailQuery(boolean unreadOnly) {
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
  public static String fromEmailQuery(String from) {
    // from: <emailAddress> will return only emails from that sender
    return !from.equals("") ? String.format("from:%s ", from) : "";
  }
}
