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
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailClient;
import com.google.sps.model.GmailClientFactory;
import com.google.sps.model.GmailClientImpl;
import java.io.IOException;
import java.util.List;
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
   * Returns messageIds from the user's Gmail account
   *
   * @param request Http request from the client. Should contain idToken and accessToken
   * @param response 403 if user is not authenticated, list of messageIds otherwise
   * @param googleCredential valid google credential object (already verified)
   * @throws IOException if an issue arises while processing the request
   */
  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    // Get messageIds from Gmail
    GmailClient gmailClient = gmailClientFactory.getGmailClient(googleCredential);
    List<Message> messages = gmailClient.listUserMessages("");

    // convert messageIds to JSON object and print to response
    Gson gson = new Gson();
    String messageJson = gson.toJson(messages);

    response.setContentType("application/json");
    response.getWriter().println(messageJson);
  }

  /**
   * Post is not supported by GmailServlet.
   *
   * @param request HTTP request from client
   * @param response Response which will contain 400 error
   * @param googleCredential valid google credential object (already verified)
   * @throws IOException if an issue arises while processing the request
   */
  @Override
  public void doPost(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    response.sendError(400, "Post is not supported");
  }
}
