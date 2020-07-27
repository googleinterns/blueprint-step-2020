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
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Tests the AuthenticatedHttpServlet abstract class */
@RunWith(JUnit4.class)
public class AuthenticatedHttpServletTest extends ServletTestBase {
  // Mock abstract servlet with real constructor and real methods
  private static final AuthenticationVerifier authVerifier =
      Mockito.mock(AuthenticationVerifier.class);
  private static final AuthenticatedHttpServlet servlet =
      Mockito.mock(
          AuthenticatedHttpServlet.class,
          Mockito.withSettings()
              .useConstructor(authVerifier)
              .defaultAnswer(Mockito.CALLS_REAL_METHODS));

  private static final Boolean AUTHENTICATION_VERIFIED = true;
  private static final Boolean AUTHENTICATION_NOT_VERIFIED = false;

  private static final String ID_TOKEN_KEY = "idToken";
  private static final String ID_TOKEN_VALUE = "sampleId";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String ACCESS_TOKEN_VALUE = "sampleAccessToken";

  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);

  private static final Cookie[] noCookies = new Cookie[] {};
  private static final Cookie[] noAccessTokenCookies = new Cookie[] {sampleIdTokenCookie};
  private static final Cookie[] noIdTokenCookies = new Cookie[] {sampleAccessTokenCookie};
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

  @Test
  public void getRequestNoTokens() throws Exception {
    Mockito.when(request.getCookies()).thenReturn(noCookies);
    servlet.doGet(request, response);
    Assert.assertEquals(403, response.getStatus());
  }

  @Test
  public void getRequestNoIdTokens() throws Exception {
    Mockito.when(request.getCookies()).thenReturn(noIdTokenCookies);
    servlet.doGet(request, response);
    Assert.assertEquals(403, response.getStatus());
  }

  @Test(expected = ServletException.class)
  public void getRequestFakeIdToken() throws Exception {
    // If a fake (or empty, which is fake) idToken is present, ServletException should be thrown.
    // This is not an expected path, and indicates malicious behaviour.
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_NOT_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doGet(request, response);
  }

  @Test
  public void getRequestNoAccessToken() throws Exception {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(noAccessTokenCookies);
    servlet.doGet(request, response);
    Assert.assertEquals(403, response.getStatus());
  }

  @Test
  public void getRequestProperAuthentication() throws Exception {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doGet(request, response);
    // doGet's default implementation should return a 400 error to client to indicate request not
    // supported.
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postRequestNoTokens() throws Exception {
    Mockito.when(request.getCookies()).thenReturn(noCookies);
    servlet.doPost(request, response);
    Assert.assertEquals(403, response.getStatus());
  }

  @Test
  public void postRequestNoIdTokens() throws Exception {
    Mockito.when(request.getCookies()).thenReturn(noIdTokenCookies);
    servlet.doPost(request, response);
    Assert.assertEquals(403, response.getStatus());
  }

  @Test(expected = ServletException.class)
  public void postRequestFakeIdToken() throws Exception {
    // If a fake (or empty, which is fake) idToken is present, ServletException should be thrown.
    // This is not an expected path, and indicates malicious behaviour.
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_NOT_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doPost(request, response);
  }

  @Test
  public void postRequestNoAccessToken() throws Exception {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(noAccessTokenCookies);
    servlet.doPost(request, response);
    Assert.assertEquals(403, response.getStatus());
  }

  @Test
  public void postRequestProperAuthentication() throws Exception {
    Mockito.when(authVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
    Mockito.when(request.getCookies()).thenReturn(validCookies);
    servlet.doPost(request, response);
    // doPost's default implementation should return a 400 error to client to indicate request not
    // supported.
    Assert.assertEquals(400, response.getStatus());
  }
}
