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
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailClientImpl;
import com.google.sps.utility.JsonUtility;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
   * @param request HTTP request from client
   * @param response Http response to be sent to client
   * @param googleCredential valid, verified google credential object
   * @throws IOException If a read/write issue arises while processing the request
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    GmailClient gmailClient = gmailClientFactory.getGmailClient(googleCredential);

    List<String> subjectLineWords;
    try {
      subjectLineWords = getListFromQueryString(request, "subjectLineWords");
    } catch (IllegalArgumentException e) {
      response.sendError(400, "subjectLineWords must be present in request");
      return;
    }
    if (subjectLineWords.isEmpty()) {
      response.sendError(400, "subjectLineWords must be non-empty");
      return;
    }

    boolean unreadOnly;
    String unreadOnlyParameter = request.getParameter("unreadOnly");
    if (unreadOnlyParameter == null) {
      response.sendError(400, "unreadOnly must be present in request");
      return;
    }
    if (!unreadOnlyParameter.equals("false") && !unreadOnlyParameter.equals("true")) {
      response.sendError(400, "unreadOnly must be a boolean");
    }
    unreadOnly = Boolean.parseBoolean(unreadOnlyParameter);

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
        gmailClient.getActionableEmails(
            GmailClient.MessageFormat.FULL, subjectLineWords, unreadOnly, nDays);
    JsonUtility.sendJson(response, actionableEmails);
  }

  /**
   * Parses a list of values (contained as a single parameter) from a request. Requested parameter
   * should have format: ..."parameter=value1,value2,value3"...
   *
   * @param request HttpServletRequest containing parameter with above format
   * @param parameter name of the request parameter
   * @return parsed list of request parameter values. Empty list if parameter is empty
   */
  private List<String> getListFromQueryString(HttpServletRequest request, String parameter)
      throws IllegalArgumentException {
    String listAsString = request.getParameter(parameter);
    if (listAsString == null) {
      throw new IllegalArgumentException(parameter + " parameter is not present in request");
    }

    return !listAsString.isEmpty()
        ? Arrays.asList(listAsString.split(","))
        : Collections.emptyList();
  }
}
