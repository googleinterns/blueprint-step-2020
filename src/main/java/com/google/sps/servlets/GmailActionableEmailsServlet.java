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
import com.google.common.collect.ImmutableList;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailClientImpl;
import com.google.sps.utility.JsonUtility;
import com.google.sps.utility.ServletUtility;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to get actionable emails from user's gmail account, as defined by specific words in an
 * email's subject line. Used by "Assign" panel on client.
 */
@WebServlet("/gmail-actionable-emails")
public class GmailActionableEmailsServlet extends AuthenticatedHttpServlet {
  private GmailClientFactory gmailClientFactory;

  /** Create new servlet instance (called by java during HttpRequest handling */
  public GmailActionableEmailsServlet() {
    gmailClientFactory = new GmailClientImpl.Factory();
  }

  private static final List<String> METADATA_HEADERS = ImmutableList.of("Subject");

  /**
   * Create new servlet instance (used for testing)
   *
   * @param authenticationVerifier implementation of AuthenticationVerifier interface
   * @param gmailClientFactory implementation of GmailClientFactory interface
   */
  public GmailActionableEmailsServlet(
      AuthenticationVerifier authenticationVerifier, GmailClientFactory gmailClientFactory) {
    super(authenticationVerifier);
    this.gmailClientFactory = gmailClientFactory;
  }

  /**
   * Retrieve the actionable emails from a user's gmail account, given a list of words/phrases to
   * search for in the subject line.
   *
   * @param request HTTP request from client. Should include at least two parameters: 1)
   *     subjectLinePhrases, which is a list (comma-separated) of phrases to search for. Gmail
   *     doesn't recognize special search characters like [], (), currency symbols, &, #, *, or
   *     commas. As such, these phrases should not contain any of the above punctuation. Moreover,
   *     phrases with commas will be 2) nDays, which specifies that emails from the last nDays days
   *     will be queried. unreadOnly is an optional parameter, assumed to be false. If true, only
   *     unread emails will be queried.
   * @param response Http response to be sent to client. Will contain a list of messages that are
   *     deemed actionable by the above criteria.
   * @param googleCredential valid, verified google credential object
   * @throws IOException If a read/write issue arises while processing the request
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    GmailClient gmailClient = gmailClientFactory.getGmailClient(googleCredential);

    List<String> subjectLinePhrases;
    try {
      subjectLinePhrases = ServletUtility.getListFromQueryString(request, "subjectLinePhrases");
    } catch (IllegalArgumentException e) {
      response.sendError(400, "subjectLinePhrases must be present in request");
      return;
    }
    if (subjectLinePhrases.isEmpty()) {
      response.sendError(400, "subjectLinePhrases must be non-empty");
      return;
    }

    String unreadOnlyParameter = request.getParameter("unreadOnly");
    boolean unreadOnly = Boolean.parseBoolean(unreadOnlyParameter);

    int nDays;
    try {
      nDays = Integer.parseInt(request.getParameter("nDays"));
    } catch (NumberFormatException e) {
      response.sendError(400, "nDays must be present in request");
      return;
    }
    if (nDays <= 0) {
      response.sendError(400, "nDays must be positive");
      return;
    }

    List<Message> actionableEmails =
        gmailClient.getActionableEmails(subjectLinePhrases, unreadOnly, nDays, METADATA_HEADERS);
    JsonUtility.sendJson(response, actionableEmails);
  }
}
