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

package com.google.sps.utility;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/** Utility class to help handle OAuth 2.0 verification. */
public final class AuthenticationUtility {
  // OAuth 2.0 Client ID
  // TODO: Hide Client ID (Issue #72)
  public static final String CLIENT_ID =
      "12440562259-mf97tunvqs179cu1bu7s6pg749gdpked.apps.googleusercontent.com";

  private AuthenticationUtility() {}

  /**
   * Helper function to verify if a request contains a valid user token
   *
   * @param request contains a cookie with a valid user token
   * @return true if a valid user token is present, false if user needs to reauthenticate
   * @throws GeneralSecurityException If something goes wrong with the verifier
   * @throws IOException If something goes wrong with the verifier
   */
  public static boolean verifyUserToken(HttpServletRequest request)
      throws GeneralSecurityException, IOException, IllegalArgumentException {

    // If idToken not present, user needs to reauthenticate.
    Cookie idTokenCookie = ServletUtility.getCookie(request, "idToken");
    if (idTokenCookie == null) {
      return false;
    }

    String idTokenString = idTokenCookie.getValue();

    // Build a verifier used to ensure the passed user ID is legitimate
    HttpTransport transport = UrlFetchTransport.getDefaultInstance();
    JsonFactory factory = JacksonFactory.getDefaultInstance();
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(transport, factory)
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    // If the idToken is not null, the identity is verified and vice versa
    GoogleIdToken idToken;
    idToken = verifier.verify(idTokenString);

    return idToken != null;
  }

  /**
   * Creates a Credential object to work with the Google API Java Client. Will handle verifying
   * userId prior to creating credential. Be sure to do this before creating the credential
   *
   * @param request http request from client. Must contain idToken and accessToken
   * @return a Google credential object that can be used to create an API service instance null if
   *     userId cannot be verified or accessToken cannot be found
   */
  public static Credential getGoogleCredential(HttpServletRequest request) {
    // Return null if userId cannot be verified
    try {
      if (!verifyUserToken(request)) {
        return null;
      }
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
      return null;
    }

    // Return null if accessToken cannot be found
    Cookie accessTokenCookie = ServletUtility.getCookie(request, "accessToken");
    if (accessTokenCookie == null) {
      return null;
    }

    return getGoogleCredential(accessTokenCookie.getValue());
  }

  /**
   * Creates a Credential object to work with the Google API Java Client. Will NOT handle verifying
   * userId prior to creating credential. Be sure to do this before creating the credential.
   * Consider using the overloaded method if you need to do a second verification
   *
   * @param accessToken String representation of the accessToken to authenticate user
   * @return a Google credential object that can be used to create an API service instance. null if
   *     accessToken is empty string
   */
  public static Credential getGoogleCredential(String accessToken) {
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
