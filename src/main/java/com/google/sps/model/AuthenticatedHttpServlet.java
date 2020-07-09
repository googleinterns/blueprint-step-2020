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

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.sps.utility.ServletUtility;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** HttpServlet that enforces the verification of user tokens */
public abstract class AuthenticatedHttpServlet extends HttpServlet {
  // Error message if user is not authenticated
  protected static final String ERROR_403 = "Authentication tokens not present / invalid";

  protected Credential googleCredential;

  private final AuthenticationVerifier authenticationVerifier;

  public AuthenticatedHttpServlet() {
    super();
    authenticationVerifier = new AuthenticationVerifierImpl();
  }

  public AuthenticatedHttpServlet(AuthenticationVerifier authenticationVerifier) {
    super();
    this.authenticationVerifier = authenticationVerifier;
  }

  /**
   * Handles a GET request and sending a 403 error in the case that the user is not properly
   * authenticated
   *
   * @param request Http request sent from client
   * @param response Http response to be sent back to the client
   * @throws IOException if there is an issue processing the request
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    googleCredential = getGoogleCredential(request);
    if (googleCredential == null) {
      response.sendError(403, ERROR_403);
    }
  }

  /**
   * Handles a POST request and sending a 403 error in the case that the user is not properly
   * authenticated
   *
   * @param request Http request sent from client
   * @param response Http response to be sent back to the client
   * @throws IOException if there is an issue processing the request
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    googleCredential = getGoogleCredential(request);
    if (googleCredential == null) {
      response.sendError(403, ERROR_403);
    }
  }

  private Credential getGoogleCredential(HttpServletRequest request) {
    Cookie userTokenCookie = ServletUtility.getCookie(request, "idToken");
    if (userTokenCookie == null) {
      return null;
    }

    try {
      if (!authenticationVerifier.verifyUserToken(userTokenCookie.getValue())) {
        return null;
      }
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
      return null;
    }

    Cookie accessTokenCookie = ServletUtility.getCookie(request, "accessToken");
    if (accessTokenCookie == null) {
      return null;
    }

    String accessToken = accessTokenCookie.getValue();

    if (accessToken.isEmpty()) {
      return null;
    }

    // Build credential object with accessToken
    Credential.AccessMethod accessMethod = BearerToken.authorizationHeaderAccessMethod();
    Credential.Builder credentialBuilder = new Credential.Builder(accessMethod);
    Credential credential = credentialBuilder.build();
    credential.setAccessToken(accessToken);

    return credential;
  }
}