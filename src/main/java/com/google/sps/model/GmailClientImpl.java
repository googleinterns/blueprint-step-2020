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
import java.util.stream.Collectors;

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

  @Override
  public List<Message> listUserMessages(String query) throws IOException {
    // Null if no messages present. Convert to empty list for ease
    List<Message> messages =
        gmailService.users().messages().list("me").setQ(query).execute().getMessages();

    return messages != null ? messages : new ArrayList<>();
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
  public List<Message> getUnreadEmailsFromNDays(GmailClient.MessageFormat messageFormat, int nDays)
      throws IOException {
    String ageQuery = GmailClient.emailAgeQuery(nDays, "d");
    String unreadQuery = GmailClient.unreadEmailQuery(true);

    String searchQuery = GmailClient.combineSearchQueries(ageQuery, unreadQuery);
    return listUserMessages(searchQuery).stream()
        .map(
            (message) -> {
              try {
                return getUserMessage(message.getId(), messageFormat);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toList());
  }

  // @Override
  // public int getMessageSize(Message message) throws MessagingException, IOException {
  //   byte[] emailBytes = Base64.decodeBase64(message.getRaw());
  //   Properties props = new Properties();
  //   Session session = Session.getDefaultInstance(props, null);
  //   MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
  //   String emailString = "";
  //   if (email.isMimeType("text/*")) {
  //     emailString = (String) email.getContent();
  //   }
  //   else if (email.isMimeType("multipart/alternative")) {
  //     emailString = getTextFromMultiPartAlternative((Multipart) part.getContent());
  //   }
  //   else if (email.isMimeType("multipart/digest")) {
  //     emailString = getTextFromMultiPartDigest((Multipart) part.getContent());
  //   }
  //   else if (mimeTypeCanBeHandledAsMultiPartMixed(part)) {
  //     emailString = getTextHandledAsMultiPartMixed(part);
  //   }
  //   System.out.println(emailString);
  //   return 3;
  // }

  // @Override
  // private String getTextFromMultiPartAlternative(Multipart multipart) throws IOException,
  // MessagingException {
  //   // search in reverse order as a multipart/alternative should have their most preferred format
  // last
  //   for (int i = multipart.getCount() - 1; i >= 0; i--) {
  //     BodyPart bodyPart = multipart.getBodyPart(i);

  //     if (bodyPart.isMimeType("text/html")) {
  //       return (String) bodyPart.getContent();
  //     } else if (bodyPart.isMimeType("text/plain")) {
  //       // Since we are looking in reverse order, if we did not encounter a text/html first we
  // can return the plain
  //       // text because that is the best preferred format that we understand. If a text/html
  // comes along later it
  //       // means the agent sending the email did not set the html text as preferable or did not
  // set their preferred
  //       // order correctly, and in that case we do not handle that.
  //       return (String) bodyPart.getContent();
  //     } else if (bodyPart.isMimeType("multipart/*") || bodyPart.isMimeType("message/rfc822")) {
  //       String text = getTextFromPart(bodyPart);
  //       if (text != null) {
  //         return text;
  //       }
  //     }
  //   }
  //   // we do not know how to handle the text in the multipart or there is no text
  //   return null;
  // }

  // @Override
  // private String getTextFromMultiPartDigest(Multipart multipart) throws IOException,
  // MessagingException {
  //   StringBuilder textBuilder = new StringBuilder();
  //   for (int i = 0; i < multipart.getCount(); i++) {
  //     BodyPart bodyPart = multipart.getBodyPart(i);
  //     if (bodyPart.isMimeType("message/rfc822")) {
  //       String text = getTextFromPart(bodyPart);
  //       if (text != null) {
  //         textBuilder.append(text);
  //       }
  //     }
  //   }
  //   String text = textBuilder.toString();

  //   if (text.isEmpty()) {
  //     return null;
  //   }

  //   return text;
  // }

  // @Override
  // private boolean mimeTypeCanBeHandledAsMultiPartMixed(Part part) throws MessagingException {
  //   return part.isMimeType("multipart/mixed") || part.isMimeType("multipart/parallel")
  //     || part.isMimeType("message/rfc822")
  //     // as per the RFC2046 specification, other multipart subtypes are recognized as
  // multipart/mixed
  //     || part.isMimeType("multipart/*");
  // }

  // @Override
  // private String getTextHandledAsMultiPartMixed(Part part) throws IOException, MessagingException
  // {
  //   return getTextFromMultiPartMixed((Multipart) part.getContent());
  // }

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
