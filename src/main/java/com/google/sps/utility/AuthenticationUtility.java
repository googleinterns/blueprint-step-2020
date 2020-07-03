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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/** Utility class to help handle OAuth 2.0 verification. */
public final class AuthenticationUtility {
  // OAuth 2.0 Client ID
  public static final String CLIENT_ID =
      "12440562259-mf97tunvqs179cu1bu7s6pg749gdpked.apps.googleusercontent.com";

  // Application Name
  public static final String APPLICATION_NAME = "PUT NAME HERE";

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
    Cookie idTokenCookie = getCookie(request, "idToken");
    if (idTokenCookie == null) {
      return false;
    }

    String idTokenString = idTokenCookie.getValue();

    // Build a verifier used to ensure the passed user ID is legitimate
    JsonFactory jsonFactory = getJsonFactory();
    HttpTransport transport = getAppEngineTransport();
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    // If the idToken is not null, the identity is verified and vice versa
    GoogleIdToken idToken;
    idToken = verifier.verify(idTokenString);

    return idToken != null;
  }

  /**
   * Helper function to create an authorization header for an outgoing HTTP request. Does NOT check
   * if the access token provides correct permissions for the request Does NOT check if the
   * userToken was valid. Use verifyUserToken to verify userId validity
   *
   * @deprecated Use getGoogleCredential and the Google API Java Client if possible.
   * @param request contains cookies for a userToken and accessToken
   * @return Value of the "Authorization" header. Null if authentication is invald or accessToken
   *     was not found
   */
  @Deprecated
  public static String generateAuthorizationHeader(HttpServletRequest request) {
    // If accessToken cannot be found, return null
    Cookie authCookie = getCookie(request, "accessToken");
    if (authCookie == null) {
      return null;
    }

    // Otherwise, return the accessToken as a Bearer token for the Authorization Header
    return "Bearer " + authCookie.getValue();
  }

  /**
   * Creates a Credential object to work with the Google API Java Client. Will handle verifying
   * userId prior to creating credential. Be sure to do this before creating the credential
   *
   * @param request http request from client. Must contain idToken and accessToken
   * @return a Google credential object that can be used to create an API service instance
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
    Cookie accessTokenCookie = getCookie(request, "accessToken");
    String accessToken = "";
    if (accessTokenCookie != null) {
      accessToken = accessTokenCookie.getValue();
    } else {
      return null;
    }

    return getGoogleCredential(accessToken);
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
    if (accessToken.equals("")) {
      return null;
    }

    // Build credential object with accessToken
    Credential.AccessMethod accessMethod = BearerToken.authorizationHeaderAccessMethod();
    Credential.Builder credentialBuilder = new Credential.Builder(accessMethod);
    Credential credential = credentialBuilder.build();
    credential.setAccessToken(accessToken);

    return credential;
  }

  /**
   * Get HTTPTransport appropriate for App Engine environment
   * https://googleapis.dev/java/google-http-client/latest/com/google/api/client/extensions/appengine/http/UrlFetchTransport.html
   *
   * @return UrlFetchTransport instance
   */
  public static HttpTransport getAppEngineTransport() {
    return new UrlFetchTransport();
  }

  /**
   * Get JsonFactory instance. Use this method to get JsonFactory instances for consistency purposes
   *
   * @return JacksonFactory instance
   */
  public static JsonFactory getJsonFactory() {
    return new JacksonFactory();
  }

  /**
   * Helper method to get a cookie from an HttpServletRequest
   *
   * @param request HttpServletRequest that contains desired cookie
   * @param cookieName name of desired cookie. Case sensitive
   * @return Cookie if found, null if not found or if duplicates present
   */
  public static Cookie getCookie(HttpServletRequest request, String cookieName) {
    List<Cookie> cookies =
        Arrays.stream(request.getCookies())
            .filter((Cookie c) -> c.getName().equals(cookieName))
            .collect(Collectors.toList());

    if (cookies.isEmpty()) {
      System.out.println("Cookie not found");
      return null;
    }

    // If more than one cookies are found, it is ambiguous as to which one to return.
    // This is unexpected - duplicate cookies are usually blocked by the browser (especially
    // when they are user set).
    if (cookies.size() > 1) {
      System.out.println("Duplicate cookie");
      return null;
    }

    return cookies.get(0);
  }
}
