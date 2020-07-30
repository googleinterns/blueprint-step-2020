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

import com.google.sps.utility.KeyProvider;
import java.io.IOException;
import java.security.GeneralSecurityException;

/** Contract for verifying user with Google Sign in API */
public interface AuthenticationVerifier {
  /**
   * Retrieves hidden Client ID from Secret Manager
   *
   * @return String representing Client ID value defined in Secret Manager
   */
  static String getClientId() throws IOException {
    return KeyProvider.getKey("clientId");
  }
  /**
   * Verifies that Google User ID Token is legitimate
   *
   * @param idToken idToken from HTTP Request
   * @return true if token is valid, false otherwise
   * @throws GeneralSecurityException If an issue occurs verifying the user token with Google
   * @throws IOException If an issue occurs verifying the user token with Google
   */
  boolean verifyUserToken(String idToken) throws GeneralSecurityException, IOException;
}
