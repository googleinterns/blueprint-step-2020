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

package com.google.sps.exceptions;

/** Error to indicate that a user's credentials could not be verified */
public class CredentialVerificationException extends RuntimeException {
  /**
   * Creates CredentialVerificationException instance
   *
   * @param errorMessage reason for throwing error
   */
  public CredentialVerificationException(String errorMessage) {
    super(errorMessage);
  }

  /**
   * Creates CredentialVerificationException instance
   * @param errorMessage reason for throwing error
   * @param exceptionCause initial error that caused the exception
   */
  public CredentialVerificationException(String errorMessage, Exception exceptionCause) {
    super(errorMessage, exceptionCause);
  }
}
