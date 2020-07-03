import com.google.sps.utility.GmailUtility;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Gmail Utility functions TODO: Mock the Gmail Service features (Issue #) */
@RunWith(JUnit4.class)
public final class GmailUtilityTest {

  // Units of time for email query string
  private final String daysUnit = "d";
  private final String hoursUnit = "h";
  private final String defaultUnit = "";
  private final String invalidUnit = "z";
  private final int oneUnitOfTime = 1;
  private final int defaultUnitOfTime = 0;
  private final int invalidUnitOfTime = -100;

  // Values to toggle filtering unread vs all emails in email query string
  private final Boolean returnUnreadOnly = true;
  private final Boolean defaultUnreadFilter = false;

  // Sample emails for email query string
  private final String sampleEmail = "example@example.com";
  private final String defaultEmail = "";

  // Expected email query strings
  private final String emptyQuery = "";
  private final String oneDayQuery = String.format("newer_than: %d%s ", oneUnitOfTime, daysUnit);
  private final String oneHourQuery = String.format("newer_than: %d%s ", oneUnitOfTime, hoursUnit);
  private final String unreadEmailsQuery = "is:unread ";
  private final String fromEmailQuery = String.format("from: %s ", sampleEmail);

  @Test
  public void getQueryStringDays() {
    // Inputting a query for 1 day should result in a oneDayQuery
    String daysQuery = GmailUtility.emailAgeQuery(oneUnitOfTime, daysUnit);

    Assert.assertEquals(daysQuery, oneDayQuery);
  }

  @Test
  public void getQueryStringHours() {
    // Inputting a query for 1 hour should result in a oneHourQuery
    String hoursQuery = GmailUtility.emailAgeQuery(oneUnitOfTime, hoursUnit);

    Assert.assertEquals(hoursQuery, oneHourQuery);
  }

  @Test
  public void getQueryStringInvalidUnit() {
    // Inputting an invalid unit should have a null result
    String invalidQuery = GmailUtility.emailAgeQuery(defaultUnitOfTime, invalidUnit);

    Assert.assertNull(invalidQuery);
  }

  @Test
  public void getQueryStringInvalidUnitOfTime() {
    // Inputting an invalid amount of time should have a null result
    String invalidQuery = GmailUtility.emailAgeQuery(invalidUnitOfTime, defaultUnit);

    Assert.assertNull(invalidQuery);
  }

  @Test
  public void getQueryStringIgnoreUnitOfTime() {
    // Inputting the default units for time and amount of time should result in an empty query
    String ignoreTimeQuery = GmailUtility.emailAgeQuery(defaultUnitOfTime, defaultUnit);

    Assert.assertEquals(ignoreTimeQuery, emptyQuery);
  }

  @Test
  public void getQueryStringUnreadEmail() {
    // Specifying unreadEmails should only be returned should result in a matching query
    String unreadQuery = GmailUtility.unreadEmailQuery(returnUnreadOnly);

    Assert.assertEquals(unreadQuery, unreadEmailsQuery);
  }

  @Test
  public void getQueryStringAnyEmail() {
    // Specifying any email can be returned should result in an empty query
    String anyEmailQuery = GmailUtility.unreadEmailQuery(defaultUnreadFilter);

    Assert.assertEquals(anyEmailQuery, emptyQuery);
  }

  @Test
  public void getQueryStringFromSpecificSender() {
    // Specifying that emails must be send from a specific email should result in a matching query
    String fromSenderQuery = GmailUtility.fromEmailQuery(sampleEmail);

    Assert.assertEquals(fromSenderQuery, fromEmailQuery);
  }

  @Test
  public void getQueryStringFromAnySender() {
    // Specifying that emails can be sent from any email (with default parameter as argument)
    // should result in a matching query
    String fromAnySenderQuery = GmailUtility.fromEmailQuery(defaultEmail);

    Assert.assertEquals(fromAnySenderQuery, emptyQuery);
  }

  @Test
  public void getQueryStringCombined() {
    // Should return query matching all specified rules
    String multipleFilterQuery =
        GmailUtility.emailQueryString(oneUnitOfTime, daysUnit, returnUnreadOnly, sampleEmail);

    Assert.assertTrue(multipleFilterQuery.contains(oneDayQuery));
    Assert.assertTrue(multipleFilterQuery.contains(unreadEmailsQuery));
    Assert.assertTrue(multipleFilterQuery.contains(fromEmailQuery));
  }
}
