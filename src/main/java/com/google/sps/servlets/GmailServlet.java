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
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailClientImpl;
import com.google.sps.model.GmailResponse;
import java.io.IOException;
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

    if (mHours > nDays * 24) {
      response.sendError(400, "mHours must represent less time than nDays");
      return;
    }

    GmailResponse gmailResponse = new GmailResponse(nDays, mHours, gmailClient);

    Gson gson = new Gson();
    String messageJson = gson.toJson(gmailResponse);
    response.setContentType("application/json");
    response.getWriter().println(messageJson);
  }
}
