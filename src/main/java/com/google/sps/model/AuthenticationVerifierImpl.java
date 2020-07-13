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

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/** Verifies authentication information with Google */
public class AuthenticationVerifierImpl implements AuthenticationVerifier {
  @Override
  public boolean verifyUserToken(String idToken) throws GeneralSecurityException, IOException {
    // Build a verifier used to ensure the passed user ID is legitimate
    HttpTransport transport = UrlFetchTransport.getDefaultInstance();
    JsonFactory factory = JacksonFactory.getDefaultInstance();
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(transport, factory)
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    // If the userToken is not null, the identity is verified and vice versa
    GoogleIdToken userToken = verifier.verify(idToken);

    return userToken != null;
  }
}
