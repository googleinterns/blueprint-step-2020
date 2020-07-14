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

import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Tests the AuthenticatedHttpServlet abstract class */
@RunWith(JUnit4.class)
public class AuthenticatedHttpServletTest {
  // Mock abstract servlet with real constructor and real methods
  private static final AuthenticationVerifier authVerifier =
      Mockito.mock(AuthenticationVerifier.class);
  private static final AuthenticatedHttpServlet servlet =
      Mockito.mock(
          AuthenticatedHttpServlet.class,
          Mockito.withSettings()
              .useConstructor(authVerifier)
              .defaultAnswer(Mockito.CALLS_REAL_METHODS));

  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  private static final Boolean AUTHENTICATION_VERIFIED = true;
  private static final Boolean AUTHENTICATION_NOT_VERIFIED = false;

  private static final String ID_TOKEN_KEY = "idToken";
  private static final String ID_TOKEN_VALUE = "sampleId";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String ACCESS_TOKEN_VALUE = "sampleAccessToken";

  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);
  private static final Cookie emptyIdTokenCookie = new Cookie(ID_TOKEN_KEY, "");

  private static final Cookie[] noCookies = new Cookie[] {};
  private static final Cookie[] noAccessTokenCookies = new Cookie[] {sampleIdTokenCookie};
  private static final Cookie[] noIdTokenCookies = new Cookie[] {sampleAccessTokenCookie};
  private static final Cookie[] emptyIdTokenCookies =
      new Cookie[] {emptyIdTokenCookie, sampleAccessTokenCookie};
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

  @Before
  public void init() throws IOException {
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);

    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void clear() {
    // Dump contents after each test
    stringWriter.getBuffer().setLength(0);
  }

  @Test
  public void getRequestNoTokens() throws IOException, ServletException {
    Mockito.when(request.getCookies()).thenReturn(noCookies);
    servlet.doGet(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestNoIdTokens() throws IOException, ServletException {
    Mockito.when(request.getCookies()).thenReturn(noIdTokenCookies);
    servlet.doGet(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestFakeIdToken() throws GeneralSecurityException, IOException, ServletException {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_NOT_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doGet(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestNoAccessToken() throws GeneralSecurityException, IOException, ServletException {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(noAccessTokenCookies);
    servlet.doGet(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestProperAuthentication() throws GeneralSecurityException, IOException, ServletException {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doGet(request, response);
    // doGet's default implementation should return a 400 error to client to indicate request not
    // supported.
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(400), Mockito.anyString());
  }

  @Test
  public void postRequestNoTokens() throws IOException, ServletException {
    Mockito.when(request.getCookies()).thenReturn(noCookies);
    servlet.doPost(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestNoIdTokens() throws IOException, ServletException {
    Mockito.when(request.getCookies()).thenReturn(noIdTokenCookies);
    servlet.doPost(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestFakeIdToken() throws GeneralSecurityException, IOException, ServletException {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_NOT_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doPost(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestNoAccessToken() throws GeneralSecurityException, IOException, ServletException {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(noAccessTokenCookies);
    servlet.doPost(request, response);
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestProperAuthentication() throws GeneralSecurityException, IOException, ServletException {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doPost(request, response);
    // doPost's default implementation should return a 400 error to client to indicate request not
    // supported.
    Mockito.verify(response, Mockito.times(1)).sendError(Mockito.eq(400), Mockito.anyString());
  }
}
