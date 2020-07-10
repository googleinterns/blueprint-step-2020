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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.sps.model.AuthenticatedHttpServlet;
import com.google.sps.model.AuthenticationVerifier;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class AuthenticatedHttpServletTest {
  private static final AuthenticationVerifier authVerifier =
      Mockito.mock(AuthenticationVerifier.class);
  private static final AuthenticatedHttpServlet servlet =
      Mockito.mock(
          AuthenticatedHttpServlet.class,
          Mockito.withSettings()
              .useConstructor(authVerifier)
              .defaultAnswer(Mockito.CALLS_REAL_METHODS));

  private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
  private final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
  private static StringWriter stringWriter;
  private static PrintWriter printWriter;

  private static final Boolean AUTHENTICATION_VERIFIED = true;
  private static final Boolean AUTHENTICATION_NOT_VERIFIED = false;

  private static final String ID_TOKEN_KEY = "idToken";
  private static final String ID_TOKEN_VALUE = "sampleId";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String ACCESS_TOKEN_VALUE = "sampleAccessToken";

  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);
  private static final Cookie emptyIdToken = new Cookie(ID_TOKEN_KEY, "");
  private static final Cookie emptyAccessToken = new Cookie(ACCESS_TOKEN_KEY, "");

  private static final Cookie[] noCookies = new Cookie[] {};
  private static final Cookie[] noAccessTokenCookies = new Cookie[] {sampleIdTokenCookie};
  private static final Cookie[] noIdTokenCookies = new Cookie[] {sampleAccessTokenCookie};
  private static final Cookie[] emptyIdTokenCookies =
      new Cookie[] {emptyIdToken, sampleAccessTokenCookie};
  private static final Cookie[] emptyAccessTokenCookies =
      new Cookie[] {sampleIdTokenCookie, emptyAccessToken};
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

  @Before
  public void init() throws IOException {
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void getRequestNoTokens() throws IOException {
    when(request.getCookies()).thenReturn(noCookies);
    servlet.doGet(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestNoIdTokens() throws IOException {
    when(request.getCookies()).thenReturn(noIdTokenCookies);
    servlet.doGet(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestEmptyIdToken() throws IOException {
    when(request.getCookies()).thenReturn(emptyIdTokenCookies);
    servlet.doGet(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestFakeIdToken() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_NOT_VERIFIED);
    when(request.getCookies()).thenReturn(validCookies);
    servlet.doGet(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestNoAccessToken() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_VERIFIED);
    when(request.getCookies()).thenReturn(noAccessTokenCookies);
    servlet.doGet(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestEmptyAccessToken() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_VERIFIED);
    when(request.getCookies()).thenReturn(emptyAccessTokenCookies);
    servlet.doGet(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void getRequestProperAuthentication() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_VERIFIED);
    when(request.getCookies()).thenReturn(validCookies);
    servlet.doGet(request, response);
    verify(response, Mockito.times(0)).sendError(Mockito.anyInt(), Mockito.anyString());
  }

  @Test
  public void postRequestNoTokens() throws IOException {
    when(request.getCookies()).thenReturn(noCookies);
    servlet.doPost(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestNoIdTokens() throws IOException {
    when(request.getCookies()).thenReturn(noIdTokenCookies);
    servlet.doPost(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestEmptyIdToken() throws IOException {
    when(request.getCookies()).thenReturn(emptyIdTokenCookies);
    servlet.doPost(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestFakeIdToken() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_NOT_VERIFIED);
    when(request.getCookies()).thenReturn(validCookies);
    servlet.doPost(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestNoAccessToken() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_VERIFIED);
    when(request.getCookies()).thenReturn(noAccessTokenCookies);
    servlet.doPost(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestEmptyAccessToken() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_VERIFIED);
    when(request.getCookies()).thenReturn(emptyAccessTokenCookies);
    servlet.doPost(request, response);
    verify(response, Mockito.times(1)).sendError(Mockito.eq(403), Mockito.anyString());
  }

  @Test
  public void postRequestProperAuthentication() throws GeneralSecurityException, IOException {
    when(authVerifier.verifyUserToken(Mockito.anyString())).thenReturn(AUTHENTICATION_VERIFIED);
    when(request.getCookies()).thenReturn(validCookies);
    servlet.doPost(request, response);
    verify(response, Mockito.times(0)).sendError(Mockito.anyInt(), Mockito.anyString());
  }
}
