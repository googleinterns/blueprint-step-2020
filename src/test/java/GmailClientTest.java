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

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.common.collect.ImmutableList;
import com.google.sps.exceptions.GmailMessageFormatException;
import com.google.sps.model.GmailClient;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test GmailClient static functions */
@RunWith(JUnit4.class)
public final class GmailClientTest {
  // Units of time for email query string
  private static final String DAYS_UNIT = "d";
  private static final String HOURS_UNIT = "h";
  private static final String DEFAULT_UNIT = "";
  private static final String INVALID_UNIT = "z";
  private static final int ONE_UNIT_OF_TIME = 1;
  private static final int DEFAULT_UNIT_OF_TIME = 0;
  private static final int INVALID_UNIT_OF_TIME = -100;

  // Sample emails for email query string
  private static final String SAMPLE_EMAIL = "example@example.com";
  private static final String DEFAULT_EMAIL = "";

  // Expected email query strings
  private static final String EMPTY_QUERY = "";
  private static final String ONE_DAY_QUERY =
      String.format("newer_than:%d%s", ONE_UNIT_OF_TIME, DAYS_UNIT);
  private static final String ONE_HOUR_QUERY =
      String.format("newer_than:%d%s", ONE_UNIT_OF_TIME, HOURS_UNIT);
  private static final String UNREAD_EMAILS_QUERY = "is:unread";
  private static final String IMPORTANT_EMAILS_QUERY = "is:important";
  private static final String FROM_EMAIL_QUERY = String.format("from:%s", SAMPLE_EMAIL);
  private static final String SUBJECT_LINE_QUERY_PREFIX = "subject:";

  private static final List<String> EMPTY_SUBJECT_LINE_PHRASES = ImmutableList.of();
  private static final List<String> SUBJECT_LINE_PHRASES_WITH_QUOTES =
      ImmutableList.of("\"phrase one\"", "\"phrase two\"");
  private static final List<String> SUBJECT_LINE_PHRASES_WITHOUT_QUOTES =
      ImmutableList.of("phrase one", "phrase two");
  private static final String SUBJECT_LINE_PHRASES_QUERY =
      "subject:(\"phrase one\" OR \"phrase two\")";

  private static final String HEADER_NAME = "SampleName";
  private static final String HEADER_VALUE = "SampleName";
  private static final MessagePartHeader headerWithHeaderValue =
      new MessagePartHeader().setName(HEADER_NAME).setValue(HEADER_VALUE);
  private static final List<MessagePartHeader> HEADERS_WITH_HEADER_NAME =
      ImmutableList.of(
          headerWithHeaderValue,
          new MessagePartHeader().setName("junk_header_one").setValue("junk_value_one"),
          new MessagePartHeader().setName("junk_header_two").setValue("junk_value_two"));
  private static final List<MessagePartHeader> HEADERS_WITHOUT_HEADER_NAME =
      ImmutableList.of(
          new MessagePartHeader().setName("junk_header_one").setValue("junk_value_one"),
          new MessagePartHeader().setName("junk_header_two").setValue("junk_value_two"));
  private static final Message messageNoHeaders = new Message();
  private static final Message messageWithHeaderName =
      new Message().setPayload(new MessagePart().setHeaders(HEADERS_WITH_HEADER_NAME));
  private static final Message messageWithoutHeaderName =
      new Message().setPayload(new MessagePart().setHeaders(HEADERS_WITHOUT_HEADER_NAME));

  @Test
  public void getQueryStringDays() {
    String daysQuery = GmailClient.emailAgeQuery(ONE_UNIT_OF_TIME, DAYS_UNIT);

    Assert.assertEquals(daysQuery, ONE_DAY_QUERY);
  }

  @Test
  public void getQueryStringHours() {
    String hoursQuery = GmailClient.emailAgeQuery(ONE_UNIT_OF_TIME, HOURS_UNIT);

    Assert.assertEquals(hoursQuery, ONE_HOUR_QUERY);
  }

  @Test
  public void getQueryStringInvalidUnit() {
    // Inputting an invalid unit should have a null result
    String invalidQuery = GmailClient.emailAgeQuery(DEFAULT_UNIT_OF_TIME, INVALID_UNIT);

    Assert.assertNull(invalidQuery);
  }

  @Test
  public void getQueryStringInvalidUnitOfTime() {
    // Inputting an invalid amount of time should have a null result
    String invalidQuery = GmailClient.emailAgeQuery(INVALID_UNIT_OF_TIME, DEFAULT_UNIT);

    Assert.assertNull(invalidQuery);
  }

  @Test
  public void getQueryStringIgnoreUnitOfTime() {
    // Inputting the default units for time and amount of time should result in an empty query
    String ignoreTimeQuery = GmailClient.emailAgeQuery(DEFAULT_UNIT_OF_TIME, DEFAULT_UNIT);

    Assert.assertEquals(ignoreTimeQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringUnreadEmail() {
    // Specifying unreadEmails should only be returned should result in a matching query
    String unreadQuery = GmailClient.unreadEmailQuery(true);

    Assert.assertEquals(unreadQuery, UNREAD_EMAILS_QUERY);
  }

  @Test
  public void getQueryStringAnyEmail() {
    // Specifying any email can be returned should result in an empty query
    String anyEmailQuery = GmailClient.unreadEmailQuery(false);

    Assert.assertEquals(anyEmailQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringFromSpecificSender() {
    // Specifying that emails must be send from a specific email should result in a matching query
    String fromSenderQuery = GmailClient.fromEmailQuery(SAMPLE_EMAIL);

    Assert.assertEquals(fromSenderQuery, FROM_EMAIL_QUERY);
  }

  @Test
  public void getQueryStringFromAnySender() {
    // Specifying that emails can be sent from any email (with default parameter as argument)
    // should result in a matching query
    String fromAnySenderQuery = GmailClient.fromEmailQuery(DEFAULT_EMAIL);

    Assert.assertEquals(fromAnySenderQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringImportantEmailsOnly() {
    String importantQuery = GmailClient.isImportantQuery(true);

    Assert.assertEquals(importantQuery, IMPORTANT_EMAILS_QUERY);
  }

  @Test
  public void getQueryStringDontFilterForImportantTag() {
    String noImportantFilterQuery = GmailClient.isImportantQuery(false);

    Assert.assertEquals(noImportantFilterQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringEmptySubjectPhrases() {
    String subjectLineQuery =
        GmailClient.oneOfPhrasesInSubjectLineQuery(EMPTY_SUBJECT_LINE_PHRASES);

    Assert.assertEquals(subjectLineQuery, EMPTY_QUERY);
  }

  @Test
  public void getQueryStringSomeSubjectWordsWithoutQuotes() {
    // quotes should be added to each word in the query
    // assumes that the order of the passed queries is preserved (will fail otherwise)
    String subjectLineQuery =
        GmailClient.oneOfPhrasesInSubjectLineQuery(SUBJECT_LINE_PHRASES_WITHOUT_QUOTES);

    Assert.assertEquals(SUBJECT_LINE_PHRASES_QUERY, subjectLineQuery);
  }

  @Test
  public void getQueryStringSomeSubjectWordsWithQuotes() {
    // quotes should NOT be added to each word in the query, since they are already present.
    // assumes that the order of the passed queries is preserved (will fail otherwise)
    String subjectLineQuery =
        GmailClient.oneOfPhrasesInSubjectLineQuery(SUBJECT_LINE_PHRASES_WITH_QUOTES);

    Assert.assertEquals(SUBJECT_LINE_PHRASES_QUERY, subjectLineQuery);
  }

  @Test
  public void combineQueriesNonePresent() {
    String combinedQuery = GmailClient.combineSearchQueries();

    Assert.assertEquals(EMPTY_QUERY, combinedQuery);
  }

  @Test
  public void combineQueriesSomePresent() {
    String combinedQuery =
        GmailClient.combineSearchQueries(
            ONE_DAY_QUERY, UNREAD_EMAILS_QUERY, IMPORTANT_EMAILS_QUERY);
    List<String> queries = Arrays.asList(combinedQuery.split(" "));

    Assert.assertTrue(queries.contains(ONE_DAY_QUERY));
    Assert.assertTrue(queries.contains(UNREAD_EMAILS_QUERY));
    Assert.assertTrue(queries.contains(IMPORTANT_EMAILS_QUERY));
  }

  @Test(expected = GmailMessageFormatException.class)
  public void extractHeaderFromMessageWithNoHeaders() {
    GmailClient.extractHeader(messageNoHeaders, HEADER_NAME);
  }

  @Test(expected = GmailMessageFormatException.class)
  public void extractHeaderFromMessageWithoutDesiredHeader() {
    GmailClient.extractHeader(messageWithoutHeaderName, HEADER_NAME);
  }

  @Test()
  public void extractHeaderFromMessageWithDesiredHeader() {
    MessagePartHeader actualHeader = GmailClient.extractHeader(messageWithHeaderName, HEADER_NAME);

    Assert.assertEquals(headerWithHeaderValue, actualHeader);
  }
}
