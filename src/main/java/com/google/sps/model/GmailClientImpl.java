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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.sps.utility.ServletUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Handles GET requests from Gmail API */
public class GmailClientImpl implements GmailClient {
  private Gmail gmailService;
  private static final int BATCH_REQUEST_CALL_LIMIT = 100;

  private GmailClientImpl(Credential credential) {
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpTransport transport = UrlFetchTransport.getDefaultInstance();
    String applicationName = ServletUtility.APPLICATION_NAME;

    gmailService =
        new Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName(applicationName)
            .build();
  }

  /**
   * List the messages in a user's Gmail account that match the passed query. Results are returned
   * in batches of 100 from the Gmail API, though a next page token is included if another batch
   * still exists. This method will use these tokens to get all of the emails that match the query,
   * even if that is in excess of 100.
   *
   * @param query search query to filter which results are returned (see:
   *     https://support.google.com/mail/answer/7190?hl=en)
   * @return list of message objects that have an ID and thread ID
   * @throws IOException if an issue occurs with the gmail service
   */
  @Override
  public List<Message> listUserMessages(String query) throws IOException {
    List<Message> userMessages = new ArrayList<>();
    List<Message> newBatchUserMessages;
    String nextPageToken = null;

    do {
      ListMessagesResponse response =
          gmailService
              .users()
              .messages()
              .list("me")
              .setQ(query)
              .setPageToken(nextPageToken)
              .execute();
      newBatchUserMessages = response.getMessages();
      nextPageToken = response.getNextPageToken();
      userMessages.addAll(newBatchUserMessages);
    } while (nextPageToken != null);

    return userMessages;
  }

  @Override
  public Message getUserMessage(String messageId, MessageFormat format) throws IOException {
    Message message =
        gmailService
            .users()
            .messages()
            .get("me", messageId)
            .setFormat(format.formatValue)
            .execute();

    return message;
  }

  @Override
  public Message getUserMessageWithMetadataHeaders(String messageId, List<String> metadataHeaders)
      throws IOException {
    Message message =
        gmailService
            .users()
            .messages()
            .get("me", messageId)
            .setFormat(MessageFormat.METADATA.formatValue)
            .setMetadataHeaders(metadataHeaders)
            .execute();

    return message;
  }

  @Override
  public List<Message> getUnreadEmailsFromNDays(GmailClient.MessageFormat messageFormat, int nDays)
      throws IOException {
    String ageQuery = GmailClient.emailAgeQuery(nDays, "d");
    String unreadQuery = GmailClient.unreadEmailQuery(true);
    String searchQuery = GmailClient.combineSearchQueries(ageQuery, unreadQuery);

    return listUserMessagesWithFormat(messageFormat, searchQuery);
  }

  @Override
  public List<Message> getActionableEmails(
      List<String> subjectLinePhrases, boolean unreadOnly, int nDays, List<String> metadataHeaders)
      throws IOException {
    String ageQuery = GmailClient.emailAgeQuery(nDays, "d");
    String unreadQuery = GmailClient.unreadEmailQuery(unreadOnly);
    String subjectLineQuery = GmailClient.oneOfPhrasesInSubjectLineQuery(subjectLinePhrases);
    String searchQuery = GmailClient.combineSearchQueries(ageQuery, unreadQuery, subjectLineQuery);

    return listUserMessagesWithMetadataHeaders(searchQuery, metadataHeaders);
  }

  /**
   * Lists out messages, but maps each user message to a specific message format Uses batching,
   * where there is a limit of 100 calls per batch request.
   *
   * @param messageFormat GmailClient.MessageFormat setting that specifies how much information from
   *     each email to retrieve
   * @param searchQuery search query to filter which results are returned (see:
   *     https://support.google.com/mail/answer/7190?hl=en)
   * @return list of messages with requested information
   * @throws IOException if there is an issue with the GmailService
   */
  private List<Message> listUserMessagesWithFormat(
      GmailClient.MessageFormat messageFormat, String searchQuery) throws IOException {
    List<Message> userMessagesWithoutFormat = listUserMessages(searchQuery);
    List<Message> userMessagesWithFormat = new ArrayList<>();

    // When each message is retrieved, add them to the userMessagesWithFormat list
    JsonBatchCallback<Message> batchCallback = addToListMessageCallback(userMessagesWithFormat);
    System.out.println(userMessagesWithoutFormat.size());

    // Add messages to a batch request, BATCH_REQUEST_CALL_LIMIT messages at a time
    // At time of writing, the limit is 100 messages, so it will add 100 messages per request
    int messageIndex = 0;
    while (messageIndex < userMessagesWithoutFormat.size()) {
      BatchRequest batchRequest = gmailService.batch();

      while (messageIndex < userMessagesWithoutFormat.size()
          && batchRequest.size() < BATCH_REQUEST_CALL_LIMIT) {
        Message currentMessage = userMessagesWithoutFormat.get(messageIndex);
        gmailService
            .users()
            .messages()
            .get("me", currentMessage.getId())
            .setFormat(messageFormat.formatValue)
            .queue(batchRequest, batchCallback);
        messageIndex++;
      }

      batchRequest.execute();
    }

    return userMessagesWithFormat;
  }

  private JsonBatchCallback<Message> addToListMessageCallback(List<Message> listToAddTo) {
    return new JsonBatchCallback<Message>() {
      @Override
      public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
        throw new RuntimeException(googleJsonError.getMessage());
      }

      @Override
      public void onSuccess(Message message, HttpHeaders httpHeaders) {
        listToAddTo.add(message);
      }
    };
  }

  /**
   * Lists out messages, but maps each user message to the METADATA format with only the specified
   * headers. Uses batching, where there is a limit of 100 calls per batch request.
   *
   * @param searchQuery search query to filter which results are returned (see:
   *     https://support.google.com/mail/answer/7190?hl=en)
   * @param metadataHeaders list of names of headers (e.g. "From") that should be included
   * @return list of messages with requested information
   * @throws IOException if there is an issue with the GmailService
   */
  private List<Message> listUserMessagesWithMetadataHeaders(
      String searchQuery, List<String> metadataHeaders) throws IOException {
    List<Message> userMessagesWithoutFormat = listUserMessages(searchQuery);
    List<Message> userMessagesWithFormat = new ArrayList<>();

    // When each message is retrieved, add them to the userMessagesWithFormat list
    JsonBatchCallback<Message> batchCallback = addToListMessageCallback(userMessagesWithFormat);

    // Add messages to a batch request, BATCH_REQUEST_CALL_LIMIT messages at a time
    // At time of writing, the limit is 100 messages, so it will add 100 messages per request
    int messageIndex = 0;
    while (messageIndex < userMessagesWithoutFormat.size()) {
      BatchRequest batchRequest = gmailService.batch();

      while (messageIndex < userMessagesWithoutFormat.size()
          && batchRequest.size() < BATCH_REQUEST_CALL_LIMIT) {
        Message currentMessage = userMessagesWithoutFormat.get(messageIndex);
        gmailService
            .users()
            .messages()
            .get("me", currentMessage.getId())
            .setFormat(MessageFormat.METADATA.formatValue)
            .setMetadataHeaders(metadataHeaders)
            .queue(batchRequest, batchCallback);
        messageIndex++;
      }

      batchRequest.execute();
    }

    return userMessagesWithFormat;
  }

  /** Factory to create a GmailClientImpl instance with given credential */
  public static class Factory implements GmailClientFactory {
    /**
     * Create a GmailClientImpl instance
     *
     * @param credential Google credential object
     * @return GmailClientImpl instance with credential
     */
    @Override
    public GmailClient getGmailClient(Credential credential) {
      return new GmailClientImpl(credential);
    }
  }
}
