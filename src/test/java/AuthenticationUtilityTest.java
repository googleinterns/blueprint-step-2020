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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AuthenticationUtilityTest {
  // Not an actual access token
  private static final String STUBBED_ACCESS_TOKEN = "abcdefgh";

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
