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
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** HttpServlet that enforces the verification of user tokens */
public abstract class AuthenticatedHttpServlet extends HttpServlet {
  // Error message if user is not authenticated
  protected static final String ERROR_403 = "Authentication tokens not present / invalid";

  protected static final String ERROR_500 = "Oops! Something unexpected happened";

  private final AuthenticationVerifier authenticationVerifier;

  /** Create AuthenticatedHttpServlet with default implementations of the AuthenticationVerifier */
  public AuthenticatedHttpServlet() {
    authenticationVerifier = new AuthenticationVerifierImpl();
  }

  /**
   * Create AuthenticatedHttpServlet with an explicit implementation of the AuthenticationVerifier
   *
   * @param authenticationVerifier implementation of the AuthenticationVerifier
   */
  public AuthenticatedHttpServlet(AuthenticationVerifier authenticationVerifier) {
    this.authenticationVerifier = authenticationVerifier;
  }

  /**
   * Verifies user credentials on GET (sending a 403 error in the case that the user is not properly
   * authenticated, or 500 error if verification service fails). Public for testing purposes
   *
   * @param request Http request sent from client
   * @param response Http response to be sent back to the client
   * @throws IOException if a read/write issue arises while processing the request
   * @throws ServletException if the request cannot be handled due to unexpected errors
   */
  @Override
  public final void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    try {
      Credential googleCredential = getGoogleCredential(request);
      if (googleCredential == null) {
        response.sendError(403, ERROR_403);
        return;
      }
      doGet(request, response, googleCredential);
    } catch (TokenVerificationError e) {
      response.sendError(403, ERROR_403);
    } catch (GeneralSecurityException e) {
      throw new ServletException(ERROR_500);
    }
  }

  /**
   * Verifies user credentials on POST (sending a 403 error in the case that the user is not
   * properly authenticated, or 500 error if verification service fails). Public for testing
   * purposes
   *
   * @param request Http request sent from client
   * @param response Http response to be sent back to the client
   * @throws IOException if there is an issue processing the request
   * @throws ServletException if the request cannot be handled due to unexpected errors
   */
  @Override
  public final void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    try {
      Credential googleCredential = getGoogleCredential(request);
      if (googleCredential == null) {
        response.sendError(403, ERROR_403);
        return;
      }
      doPost(request, response, googleCredential);
    } catch (TokenVerificationError e) {
      response.sendError(403, ERROR_403);
    } catch (GeneralSecurityException e) {
      throw new ServletException(ERROR_500);
    }
  }

  /**
   * Handle GET request. Public for testing purposes
   *
   * @param request HTTP request from client
   * @param response Http response to be sent to client
   * @param googleCredential valid, verified google credential object
   * @throws IOException is there is an issue processing the request
   */
  public void doGet(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    response.sendError(400, "GET is not supported");
  }

  /**
   * Handle POST request. Public for testing purposes
   *
   * @param request HTTP request from client
   * @param response Http response to be sent to client
   * @param googleCredential valid, verified google credential object
   * @throws IOException is there is an issue processing the request
   */
  public void doPost(
      HttpServletRequest request, HttpServletResponse response, Credential googleCredential)
      throws IOException {
    response.sendError(400, "POST is not supported");
  }

  /**
   * Get a Google Credential object from the authentication cookies (idToken & accessToken) in the
   * HTTP Request
   *
   * @param request Http Request sent from client
   * @return Credential object with accessToken. null if tokens not present / are empty
   * @throws TokenVerificationError if the passed idToken is confirmed to be false / is unverifiable
   * @throws GeneralSecurityException if an issue occurs with Google's verification service
   * @throws IOException if an issue occurs with Google's verification service
   */
  private Credential getGoogleCredential(HttpServletRequest request)
      throws TokenVerificationError, GeneralSecurityException, IOException {
    // If cookies not present, return null (user's session likely expired)
    Cookie idTokenCookie = ServletUtility.getCookie(request, "idToken");
    Cookie accessTokenCookie = ServletUtility.getCookie(request, "accessToken");
    if (idTokenCookie == null || accessTokenCookie == null) {
      return null;
    }
    String idToken = idTokenCookie.getValue();
    String accessToken = accessTokenCookie.getValue();

    try {
      if (!authenticationVerifier.verifyUserToken(idToken)) {
        throw new TokenVerificationError(String.format("idToken (value=%s) is invalid!", idToken));
      }
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
      throw e;
    }

    // Build Google credential with verified authentication information
    Credential.AccessMethod accessMethod = BearerToken.authorizationHeaderAccessMethod();
    Credential.Builder credentialBuilder = new Credential.Builder(accessMethod);
    Credential credential = credentialBuilder.build();
    credential.setAccessToken(accessToken);

    return credential;
  }
}
