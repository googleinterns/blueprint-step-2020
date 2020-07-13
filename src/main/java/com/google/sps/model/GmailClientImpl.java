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
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.sps.utility.ServletUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Handles GET requests from Gmail API */
public class GmailClientImpl implements GmailClient {
  private Gmail gmailService;

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
   * Get all of the message IDs from a user's Gmail account
   *
   * @param query search query to filter which results are returned
   * @return a list of messages with IDs and Thread IDs
   * @throws IOException if an issue occurs with the Gmail Service
   */
  @Override
  public List<Message> listUserMessages(String query) throws IOException {
    // Null if no messages present. Convert to empty list for ease
    List<Message> messages =
        gmailService.users().messages().list("me").setQ(query).execute().getMessages();

    return messages != null ? messages : new ArrayList<>();
  }

  /**
   * Get a message from a user's Gmail account
   *
   * @param messageId the messageID (retrieved from listUserMessages) of the desired Message
   * @param format MessageFormat enum that defines how much of the Message object is populated
   * @return a Message object with the requested information
   * @throws IOException if an issue occurs with the Gmail service
   */
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
