package com.google.sps.utility;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.sps.model.MessageFormat;
import java.io.IOException;
import java.util.List;

/**
 * Contains logic that handles GET & POST requests to the Gmail API and transforms those responses
 * into easily usable Java types
 */
public class GmailUtility {
  // Make constructor private so no instances of this class can be made
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
   * Creates Gmail query for age of emails
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
      return String.format("newer_than: %d%s ", emailAge, emailAgeUnits);
    } else if (emailAge == 0 && emailAgeUnits.isEmpty()) {
      return "";
    } else {
      return null;
    }
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
    return !from.equals("") ? String.format("from: %s ", from) : "";
  }

  // Get a gmail service instance given a credential
  public static Gmail getGmailService(Credential credential) {
    JsonFactory jsonFactory = AuthenticationUtility.getJsonFactory();
    HttpTransport transport = AuthenticationUtility.getAppEngineTransport();

    return new Gmail.Builder(transport, jsonFactory, credential).build();
  }

  public static List<Message> listUserMessages(Gmail gmailService, String query)
      throws IOException {
    List<Message> messages =
        gmailService.users().messages().list("me").setQ(query).execute().getMessages();

    return messages;
  }

  public static Message getMessage(Gmail gmailService, String messageId, MessageFormat format)
      throws IOException {
    Message message =
        gmailService
            .users()
            .messages()
            .get("me", messageId)
            .setFormat(format.formatValue)
            .execute();

    return message;
  }
}
