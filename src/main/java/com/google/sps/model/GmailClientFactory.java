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

import com.google.api.client.auth.oauth2.Credential;

/** Contract for creating a GmailClient with a given credential */
public interface GmailClientFactory {
  /**
   * Creates a GmailClient instance
   *
   * @param credential a valid Google credential object
   * @return a GmailClient instance that contains the user's credentials
   */
  GmailClient getGmailClient(Credential credential);
}
