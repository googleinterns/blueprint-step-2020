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

import com.google.sps.utility.ServletUtility;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Test Authentication Utility functions */
@RunWith(JUnit4.class)
public final class ServletUtilityTest {

  private HttpServletRequest request;

  private static final Cookie[] correctCookies =
      new Cookie[] {
        new Cookie("junk", "junk_value"),
        new Cookie("idToken", "sample_id_token"),
        new Cookie("accessToken", "sample_access_token")
      };

  private static final Cookie[] emptyCookies = new Cookie[] {};

  private static final Cookie[] missingAuthCookies =
      new Cookie[] {new Cookie("junk", "junk_value")};

  private static final Cookie[] duplicateCookies =
      new Cookie[] {
        new Cookie("junk", "junk_value"),
        new Cookie("idToken", "sample_id_token"),
        new Cookie("idToken", "sample_id_token"),
        new Cookie("accessToken", "sample_access_token"),
        new Cookie("accessToken", "sample_access_token")
      };

  @Before
  public void init() {
    request = Mockito.mock(HttpServletRequest.class);
  }

  @Test
  public void getCookie() throws IOException {
    // A cookie is requested and is present in the list. Should return cookie object
    Mockito.when(request.getCookies()).thenReturn(correctCookies);

    Cookie retrievedCookie = ServletUtility.getCookie(request, "idToken");
    Assert.assertEquals(retrievedCookie.getName(), "idToken");
    Assert.assertEquals(retrievedCookie.getValue(), "sample_id_token");
  }

  @Test
  public void getCookieEmptyCookies() throws IOException {
    // A cookie is requested from an empty list. Should return null.
    Mockito.when(request.getCookies()).thenReturn(emptyCookies);

    Cookie retrievedCookie = ServletUtility.getCookie(request, "idToken");
    Assert.assertNull(retrievedCookie);
  }

  @Test
  public void getCookieNameNotFound() throws IOException {
    // A cookie is requested and it is not in the list. Should return null
    Mockito.when(request.getCookies()).thenReturn(missingAuthCookies);

    Cookie retrievedCookie = ServletUtility.getCookie(request, "idToken");
    Assert.assertNull(retrievedCookie);
  }

  @Test(expected = IOException.class)
  public void getCookieFromDuplicates() throws IOException {
    // A cookie is requested but duplicates present.
    // Should throw IOException
    Mockito.when(request.getCookies()).thenReturn(duplicateCookies);

    ServletUtility.getCookie(request, "idToken");
  }

  @Test
  public void getAuthHeader() throws IOException {
    // An authentication header is requested and the access token is present.
    // Should return "Bearer <access-token>"
    Mockito.when(request.getCookies()).thenReturn(correctCookies);

    String header = ServletUtility.generateAuthorizationHeader(request);
    Assert.assertEquals(header, "Bearer sample_access_token");
  }

  @Test
  public void getAuthHeaderEmptyCookies() throws IOException {
    // An authentication header is requested but no cookies are present.
    // Should return null
    Mockito.when(request.getCookies()).thenReturn(emptyCookies);

    String header = ServletUtility.generateAuthorizationHeader(request);
    Assert.assertNull(header);
  }

  @Test
  public void getAuthHeaderMissingAuthCookies() throws IOException {
    // An authentication header is requested but no access token is present.
    // Should return null
    Mockito.when(request.getCookies()).thenReturn(missingAuthCookies);

    String header = ServletUtility.generateAuthorizationHeader(request);
    Assert.assertNull(header);
  }

  @Test(expected = IOException.class)
  public void getAuthHeaderDuplicateTokens() throws IOException {
    // An authentication header is requested but duplicate access tokens present
    // Should throw IOException
    Mockito.when(request.getCookies()).thenReturn(duplicateCookies);

    ServletUtility.generateAuthorizationHeader(request);
  }
}
