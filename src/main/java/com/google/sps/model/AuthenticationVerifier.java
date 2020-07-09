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

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthenticationVerifier {
  String CLIENT_ID = "12440562259-mf97tunvqs179cu1bu7s6pg749gdpked.apps.googleusercontent.com";
  boolean verifyUserToken(String userToken) throws GeneralSecurityException, IOException;
}
