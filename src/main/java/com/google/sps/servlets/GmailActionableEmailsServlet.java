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
import com.google.sps.exceptions.GmailMessageFormatException;
import com.google.sps.model.ActionableMessage;
import com.google.sps.model.ActionableMessageHelper;
import com.google.sps.model.ActionableMessageHelperImpl;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailClientImpl;
import com.google.sps.utility.GmailUtility;
import com.google.sps.utility.JsonUtility;
import com.google.sps.utility.ServletUtility;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to get actionable emails from user's gmail account, as defined by specific words in an
 * email's subject line. Used by "Assign" panel on client.
 */
@WebServlet("/gmail-actionable-emails")
public class GmailActionableEmailsServlet extends AuthenticatedHttpServlet {
  private final GmailClientFactory gmailClientFactory;
  private final ActionableMessageHelper actionableMessageHelper;

  /** Create new servlet instance (called by java during HttpRequest handling */
  public GmailActionableEmailsServlet() {
    gmailClientFactory = new GmailClientImpl.Factory();
    actionableMessageHelper = new ActionableMessageHelperImpl();
  }

  private static final List<String> METADATA_HEADERS = ImmutableList.of("Subject");

  /**
   * Create new servlet instance (used for testing)
   *
   * @param authenticationVerifier implementation of AuthenticationVerifier interface
   * @param gmailClientFactory implementation of GmailClientFactory interface
   * @param actionableMessageHelper implementation of ActionableMessageHelper interface
   */
  public GmailActionableEmailsServlet(
      AuthenticationVerifier authenticationVerifier,
      GmailClientFactory gmailClientFactory,
      ActionableMessageHelper actionableMessageHelper) {
    super(authenticationVerifier);
    this.gmailClientFactory = gmailClientFactory;
    this.actionableMessageHelper = actionableMessageHelper;
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
   * @param userEmail the email address of the user
   * @throws IOException If a read/write issue arises while processing the request
   */
  @Override
  public void doGet(
      HttpServletRequest request,
      HttpServletResponse response,
      Credential googleCredential,
      String userEmail)
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

    List<ActionableMessage> actionableEmails =
        gmailClient.getActionableEmails(subjectLinePhrases, unreadOnly, nDays, METADATA_HEADERS)
            .stream()
            .map((message) -> createActionableMessage(message, userEmail))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
    JsonUtility.sendJson(response, actionableEmails);
  }

  /**
   * Creates an ActionableMessage object from a Gmail Message
   *
   * @param message Message object from user's Gmail account
   * @param userEmail the email address of the current user. Used for assigning priority
   * @return ActionableMessage object with information from Gmail Message object
   * @throws GmailMessageFormatException if the "Subject" header is not present (despite filtering
   *     for this in the search query).
   */
  private ActionableMessage createActionableMessage(Message message, String userEmail)
      throws GmailMessageFormatException {
    String messageId = message.getId();
    String subject = GmailUtility.extractHeader(message, "Subject").getValue();
    long internalDate = message.getInternalDate();
    ActionableMessage.MessagePriority priority =
        actionableMessageHelper.assignMessagePriority(message, userEmail);
    return new ActionableMessage(messageId, subject, internalDate, priority);
  }
}
