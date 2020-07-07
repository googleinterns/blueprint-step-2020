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

import com.google.sps.utility.GmailUtility;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Gmail Utility functions TODO: Mock the Gmail Service features (Issue #5) */
@RunWith(JUnit4.class)
public final class GmailUtilityTest {

  // Units of time for email query string
  private static final String DAYS_UNIT = "d";
  private static final String HOURS_UNIT = "h";
  private static final String DEFAULT_UNIT = "";
  private static final String INVALID_UNIT = "z";
  private static final int ONE_UNIT_OF_TIME = 1;
  private static final int DEFAULT_UNIT_OF_TIME = 0;
  private static final int INVALID_UNIT_OF_TIME = -100;

  // Values to toggle filtering unread vs all emails in email query string
  private static final Boolean RETURN_UNREAD_ONLY = true;
  private static final Boolean DEFAULT_UNREAD_FILTER = false;

  // Sample emails for email query string
  private static final String SAMPLE_EMAIL = "example@example.com";
  private static final String DEFAULT_EMAIL = "";

  // Expected email query strings
  private static final String EMPTY_QUERY = "";
  private static final String ONE_DAY_QUERY =
      String.format("newer_than: %d%s ", ONE_UNIT_OF_TIME, DAYS_UNIT);
  private static final String ONE_HOUR_QUERY =
      String.format("newer_than: %d%s ", ONE_UNIT_OF_TIME, HOURS_UNIT);
  private static final String UNREAD_EMAILS_QUERY = "is:unread ";
  private static final String FROM_EMAIL_QUERY = String.format("from: %s ", SAMPLE_EMAIL);

  @Test
  public void getQueryStringDays() {
    // Inputting a query for 1 day should result in a oneDayQuery
    String daysQuery = GmailUtility.emailAgeQuery(ONE_UNIT_OF_TIME, DAYS_UNIT);

    Assert.assertEquals(daysQuery, ONE_DAY_QUERY);
  }

  @Test
  public void getQueryStringHours() {
    // Inputting a query for 1 hour should result in a oneHourQuery
    String hoursQuery = GmailUtility.emailAgeQuery(ONE_UNIT_OF_TIME, HOURS_UNIT);

    Assert.assertEquals(hoursQuery, ONE_HOUR_QUERY);
  }

  @Test
  public void getQueryStringInvalidUnit() {
    // Inputting an invalid unit should have a null result
    String invalidQuery = GmailUtility.emailAgeQuery(DEFAULT_UNIT_OF_TIME, INVALID_UNIT);

    Assert.assertNull(invalidQuery);
  }

  @Test
  public void getQueryStringInvalidUnitOfTime() {
    // Inputting an invalid amount of time should have a null result
    String invalidQuery = GmailUtility.emailAgeQuery(INVALID_UNIT_OF_TIME, DEFAULT_UNIT);

    Assert.assertNull(invalidQuery);
  }

  @Test
  public void getQueryStringIgnoreUnitOfTime() {
    // Inputting the default units for time and amount of time should result in an empty query
    String ignoreTimeQuery = GmailUtility.emailAgeQuery(DEFAULT_UNIT_OF_TIME, DEFAULT_UNIT);

    Assert.assertEquals(ignoreTimeQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringUnreadEmail() {
    // Specifying unreadEmails should only be returned should result in a matching query
    String unreadQuery = GmailUtility.unreadEmailQuery(RETURN_UNREAD_ONLY);

    Assert.assertEquals(unreadQuery, UNREAD_EMAILS_QUERY);
  }

  @Test
  public void getQueryStringAnyEmail() {
    // Specifying any email can be returned should result in an empty query
    String anyEmailQuery = GmailUtility.unreadEmailQuery(DEFAULT_UNREAD_FILTER);

    Assert.assertEquals(anyEmailQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringFromSpecificSender() {
    // Specifying that emails must be send from a specific email should result in a matching query
    String fromSenderQuery = GmailUtility.fromEmailQuery(SAMPLE_EMAIL);

    Assert.assertEquals(fromSenderQuery, FROM_EMAIL_QUERY);
  }

  @Test
  public void getQueryStringFromAnySender() {
    // Specifying that emails can be sent from any email (with default parameter as argument)
    // should result in a matching query
    String fromAnySenderQuery = GmailUtility.fromEmailQuery(DEFAULT_EMAIL);

    Assert.assertEquals(fromAnySenderQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringCombined() {
    // Should return query matching all specified rules
    String multipleFilterQuery =
        GmailUtility.emailQueryString(
            ONE_UNIT_OF_TIME, DAYS_UNIT, RETURN_UNREAD_ONLY, SAMPLE_EMAIL);

    Assert.assertTrue(multipleFilterQuery.contains(ONE_DAY_QUERY));
    Assert.assertTrue(multipleFilterQuery.contains(UNREAD_EMAILS_QUERY));
    Assert.assertTrue(multipleFilterQuery.contains(FROM_EMAIL_QUERY));
  }
}