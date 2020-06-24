import com.google.sps.utility.AuthenticationUtility;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.mockito.Mockito;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Class to Test the Authentication Utility functions
 * TODO: Find a way to mock the Google verification service to create unit tests for
 *     AuthenticationUtility.verifyUserToken()
 */
@RunWith(JUnit4.class)
public final class AuthenticationUtilityTest {

  // Will mock an HttpServletRequest to be passed to utilities
  private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  private final Cookie[] correctCookies = new Cookie[] {
      new Cookie("Junk", "Junk_Value"),
      new Cookie("idToken", "sample_id_token"),
      new Cookie("accessToken", "sample_access_token")
  };

  private final Cookie[] emptyCookies = new Cookie[] {};

  private final Cookie[] missingAuthCookies = new Cookie[] {
      new Cookie("Junk", "Junk_Value")
  };

  private final Cookie[] duplicateCookies = new Cookie[] {
      new Cookie("Junk", "Junk_Value"),
      new Cookie("idToken", "sample_id_token"),
      new Cookie("idToken", "sample_id_token"),
      new Cookie("accessToken", "sample_access_token"),
      new Cookie("accessToken", "sample_access_token")
  };

  @Test
  public void getCookie() {
    // A cookie is requested and is present in the list. Should return cookie object
    Mockito.when(request.getCookies()).thenReturn(correctCookies);

    Cookie retrievedCookie = AuthenticationUtility.getCookie(request, "idToken");
    Assert.assertNotNull(retrievedCookie);
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
}

