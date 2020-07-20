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

package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailClientImpl;
import com.google.sps.model.GmailResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves selected information from the User's Gmail Account. */
@WebServlet("/gmail")
public class GmailServlet extends AuthenticatedHttpServlet {
  private final GmailClientFactory gmailClientFactory;

  /** Create servlet with default GmailClient and Authentication Verifier implementations */
  public GmailServlet() {
    super();
    gmailClientFactory = new GmailClientImpl.Factory();
  }

  /**
   * Create servlet with explicit implementations of GmailClient and AuthenticationVerifier
   *
   * @param authenticationVerifier implementation of AuthenticationVerifier
   * @param gmailClientFactory implementation of GmailClientFactory
   */
  public GmailServlet(
      AuthenticationVerifier authenticationVerifier, GmailClientFactory gmailClientFactory) {
    super(authenticationVerifier);
    this.gmailClientFactory = gmailClientFactory;
  }

  /**
   * Returns selected statistics from a user's gmail account. Statistics include number of unread
   * emails from last n days, number of important unread emails from last n days, number of unread
   * emails from last m hours, and the most frequent sender of unread emails in the last n days
   *
   * @param request Http request from the client. Should contain idToken and accessToken, as well as
   *     integer values for nDays and mHours (both >0)
   * @param response 403 if user is not authenticated, list of messageIds otherwise
   * @param googleCredential valid google credential object (already verified)
   * @throws IOException if an issue arises while processing the request
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    assert googleCredential != null
        : "Null credentials (i.e. unauthenticated requests) should already be handled";

    GmailClient gmailClient = gmailClientFactory.getGmailClient(googleCredential);

    int nDays;
    int mHours;
    try {
      nDays = Integer.parseInt(request.getParameter("nDays"));
      mHours = Integer.parseInt(request.getParameter("mHours"));
    } catch (NumberFormatException e) {
      response.sendError(400, "nDays and mHours must be integers");
      return;
    }

    if (nDays < 0 || mHours < 0) {
      response.sendError(400, "nDays and mHours must be positive");
      return;
    }

    // TODO: Perform these operations faster (use multithreading, batching, etc) (Issue #84)
    int unreadEmails = getUnreadEmailCountDays(nDays, gmailClient);
    int recentUnreadEmails = getUnreadEmailCountHours(mHours, gmailClient);
    int unreadImportantEmails = getImportantUnreadEmailCount(nDays, gmailClient);
    String sender = getMostFrequentSender(nDays, gmailClient);
    GmailResponse gmailResponse =
        new GmailResponse(
            nDays, mHours, unreadEmails, recentUnreadEmails, unreadImportantEmails, sender);

    Gson gson = new Gson();
    String messageJson = gson.toJson(gmailResponse);
    response.setContentType("application/json");
    response.getWriter().println(messageJson);
  }

  private static class EmailFrequencyWithDate {
    private int timesSent;
    private long mostRecentEmailTimestamp;

    public EmailFrequencyWithDate(long timestamp) {
      timesSent = 1;
      this.mostRecentEmailTimestamp = timestamp;
    }

    public int getTimesSent() {
      return timesSent;
    }

    public long getMostRecentEmailTimestamp() {
      return mostRecentEmailTimestamp;
    }

    public void updateMostRecentEmailTimestamp(long timestamp) {
      if (timestamp > mostRecentEmailTimestamp) {
        this.mostRecentEmailTimestamp = timestamp;
      }
    }

    public void incrementTimesSent() {
      timesSent++;
    }
  }

  /**
   * Get the amount of unread emails from the last n days in a user's Gmail account
   *
   * @param nDays number of days to check the user's account for
   * @param gmailClient Gmail service with valid google credential
   * @return number of unread emails from last nDays days
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private int getUnreadEmailCountDays(int nDays, GmailClient gmailClient) throws IOException {
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, false, "");
    return gmailClient.listUserMessages(searchQuery).size();
  }

  /**
   * Get the amount of unread emails from the last m hours in a user's Gmail account
   *
   * @param mHours number of hours to check the user's account for
   * @param gmailClient Gmail service with valid google credential
   * @return number of unread emails from last mHours hours
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private int getUnreadEmailCountHours(int mHours, GmailClient gmailClient) throws IOException {
    String searchQuery = GmailClient.emailQueryString(mHours, "h", true, false, "");
    return gmailClient.listUserMessages(searchQuery).size();
  }

  /**
   * Get the amount of unread, important emails from the last n days in a user's Gmail account
   *
   * @param nDays number of days to check the user's account for
   * @param gmailClient Gmail service with valid google credential
   * @return number of unread, important emails from last nDays days
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private int getImportantUnreadEmailCount(int nDays, GmailClient gmailClient) throws IOException {
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, true, "");
    return gmailClient.listUserMessages(searchQuery).size();
  }

  /**
   * Get the sender of the most unread emails from the last n days in a user's Gmail account
   *
   * @param nDays number of days to check the user's account for
   * @param gmailClient Gmail service with valid google credential
   * @return sender of the most unread emails from the last nDays days
   * @throws IOException if an issue occurs with the Gmail Service
   */
  private String getMostFrequentSender(int nDays, GmailClient gmailClient) throws IOException {
    // Get user's messages - if none present, return "" as there is no sender to extract
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, false, "");
    GmailClient.MessageFormat messageFormat = GmailClient.MessageFormat.METADATA;
    List<Message> unreadEmails;
    unreadEmails =
        gmailClient.listUserMessages(searchQuery).stream()
            .map(
                (Message m) -> {
                  try {
                    return gmailClient.getUserMessage(m.getId(), messageFormat);
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toList());
    if (unreadEmails.isEmpty()) {
      return "";
    }

    // Find out which sender sent the most emails
    // Extracted from headers within messages.
    // Headers have the name "From" and two types of values:
    // <sampleemail@sample.com> (if name is not available)
    // OR
    // Sample Sender <sampleemail@sample.com>
    // If a name is available, this is extracted. Otherwise, the email is extracted
//    HashMap<String, Integer> senders = new HashMap<>();
//    unreadEmails.forEach(
//        (Message m) -> {
//          List<MessagePartHeader> senderList = m.getPayload().getHeaders();
//          senderList =
//              senderList.stream()
//                  .filter((MessagePartHeader header) -> header.getName().equals("From"))
//                  .collect(Collectors.toList());
//
//          String sender = senderList.get(0).getValue();
//
//          senders.put(sender, senders.get(sender) != null ? senders.get(sender) + 1 : 1);
//        });
//    String headerValue =
//        senders.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();

    // stores the senders paired with 1) how often they sent emails in the last nDays and
    // 2) what the timestamp was of their most recent email.
    HashMap<String, EmailFrequencyWithDate> senders = new HashMap<>();
    unreadEmails.forEach(
        (Message m) -> {
          List<MessagePartHeader> senderList = m.getPayload().getHeaders();
          senderList =
              senderList.stream()
                  .filter((MessagePartHeader header) -> header.getName().equals("From"))
                  .collect(Collectors.toList());

          String sender = senderList.get(0).getValue();

          long timestamp = m.getInternalDate();
          if (senders.get(sender) == null) {
            EmailFrequencyWithDate newEmailFrequencyWithDate = new EmailFrequencyWithDate(timestamp);
            senders.put(sender, newEmailFrequencyWithDate);
          } else {
            EmailFrequencyWithDate updatedEmailFrequencyWithDate = senders.get(sender);
            updatedEmailFrequencyWithDate.incrementTimesSent();
            updatedEmailFrequencyWithDate.updateMostRecentEmailTimestamp(timestamp);
            senders.replace(sender, updatedEmailFrequencyWithDate);
          }
        });

    String headerValue = senders.entrySet().stream().reduce((a, b) -> {
      if (a.getValue().getTimesSent() > b.getValue().getTimesSent()) {
        return a;
      } else if (b.getValue().getTimesSent() > a.getValue().getTimesSent()) {
        return b;
      } else {
        if (a.getValue().getMostRecentEmailTimestamp() > b.getValue().getMostRecentEmailTimestamp()) {
          return a;
        }
        return b;
      }
    }).get().getKey();

    if (headerValue.charAt(0) == '<') {
      return headerValue.substring(1, headerValue.length() - 1);
    } else {
      return headerValue.split("<")[0].trim();
    }
  }
}