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

import com.google.api.client.auth.oauth2.Credential;
import com.google.sps.utility.AuthenticationUtility;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Test Authentication Utility functions TODO: Mock the Google verification service (Issue #5) */
@RunWith(JUnit4.class)
public final class AuthenticationUtilityTest {

  // Will mock an HttpServletRequest to be passed to utilities
  private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  private final Cookie[] correctCookies =
      new Cookie[] {
        new Cookie("junk", "junk_value"),
        new Cookie("idToken", "sample_id_token"),
        new Cookie("accessToken", "sample_access_token")
      };

  private final Cookie[] emptyCookies = new Cookie[] {};

  private final Cookie[] missingAuthCookies = new Cookie[] {new Cookie("junk", "junk_value")};

  private final Cookie[] duplicateCookies =
      new Cookie[] {
        new Cookie("junk", "junk_value"),
        new Cookie("idToken", "sample_id_token"),
        new Cookie("idToken", "sample_id_token"),
        new Cookie("accessToken", "sample_access_token"),
        new Cookie("accessToken", "sample_access_token")
      };

  // Not an actual access token
  private static final String STUBBED_ACCESS_TOKEN = "abcdefgh";

  @Test
  public void getCookie() {
    // A cookie is requested and is present in the list. Should return cookie object
    Mockito.when(request.getCookies()).thenReturn(correctCookies);

    Cookie retrievedCookie = AuthenticationUtility.getCookie(request, "idToken");
    Assert.assertEquals(retrievedCookie.getName(), "idToken");
    Assert.assertEquals(retrievedCookie.getValue(), "sample_id_token");
  }

  @Test
  public void getCookieEmptyCookies() {
    // A cookie is requested from an empty list. Should return null.
    Mockito.when(request.getCookies()).thenReturn(emptyCookies);

    Cookie retrievedCookie = AuthenticationUtility.getCookie(request, "idToken");
    Assert.assertNull(retrievedCookie);
  }

  @Test
  public void getCookieNameNotFound() {
    // A cookie is requested and it is not in the list. Should return null
    Mockito.when(request.getCookies()).thenReturn(missingAuthCookies);

    Cookie retrievedCookie = AuthenticationUtility.getCookie(request, "idToken");
    Assert.assertNull(retrievedCookie);
  }

  @Test
  public void getCookieFromDuplicates() {
    // A cookie is requested but duplicates present. Should return null
    Mockito.when(request.getCookies()).thenReturn(duplicateCookies);

    Cookie retrievedCookie = AuthenticationUtility.getCookie(request, "idToken");
    Assert.assertNull(retrievedCookie);
  }

  @Test
  public void getAuthHeader() {
    // An authentication header is requested and the access token is present.
    // Should return "Bearer <access-token>"
    Mockito.when(request.getCookies()).thenReturn(correctCookies);

    String header = AuthenticationUtility.generateAuthorizationHeader(request);
    Assert.assertEquals(header, "Bearer sample_access_token");
  }

  @Test
  public void getAuthHeaderEmptyCookies() {
    // An authentication header is requested but no cookies are present.
    // Should return null
    Mockito.when(request.getCookies()).thenReturn(emptyCookies);

    String header = AuthenticationUtility.generateAuthorizationHeader(request);
    Assert.assertNull(header);
  }

  @Test
  public void getAuthHeaderMissingAuthCookies() {
    // An authentication header is requested but no access token is present.
    // Should return null
    Mockito.when(request.getCookies()).thenReturn(missingAuthCookies);

    String header = AuthenticationUtility.generateAuthorizationHeader(request);
    Assert.assertNull(header);
  }

  @Test
  public void getAuthHeaderDuplicateTokens() {
    // An authentication header is requested but duplicate access tokens present
    // Should return null
    Mockito.when(request.getCookies()).thenReturn(duplicateCookies);

    String header = AuthenticationUtility.generateAuthorizationHeader(request);
    Assert.assertNull(header);
  }

  @Test
  public void getValidCredential() {
    // Should create a valid Google credential object with accessToken stored
    Credential googleCredential = AuthenticationUtility.getGoogleCredential(STUBBED_ACCESS_TOKEN);
    Assert.assertEquals(googleCredential.getAccessToken(), STUBBED_ACCESS_TOKEN);
  }

  @Test
  public void getInvalidCredential() {
    // If a credential is made with "" as the accessToken, a null object should be returned
    // The makeCredential method is not expected to test for any other forms of invalidity
    // i.e. invalid accessKey, erroneous input, etc.
    Assert.assertNull(AuthenticationUtility.getGoogleCredential(""));
  }
}
