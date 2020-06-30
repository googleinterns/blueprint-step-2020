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
   * Creates a query string for Gmail. Used in search to return emails that fit certain restrictions
   *
   * @param emailAgeinDays emails from the last emailAgeInDays days will be returned. 0 for
   *     not-specified
   * @param unreadOnly true if only returning unread emails, false otherwise
   * @param from should be the email address of the recipient. "" if not specified
   * @return string to use in gmail (either client or API) to find emails that match criteria
   */
  public static String emailQueryString(int emailAgeinDays, boolean unreadOnly, String from) {
    String queryString = "";

    // newer_than:#d where # is an integer will specify to only return emails from last # days
    if (emailAgeinDays > 0) {
      queryString += String.format("newer_than: %dd ", emailAgeinDays);
    }

    if (unreadOnly) {
      queryString += "is:unread ";
    }

    if (!from.equals("")) {
      queryString += String.format("from: %s", from);
    }

    return queryString;
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
