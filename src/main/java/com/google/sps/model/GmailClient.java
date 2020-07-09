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

import com.google.api.services.gmail.model.Message;
import java.io.IOException;
import java.util.List;

public interface GmailClient {
  List<Message> listUserMessages(String query) throws IOException;

  Message getUserMessage(String messageId, MessageFormat format) throws IOException;

  /**
   * Encapsulates possible values for the "format" query parameter in the Gmail GET message method
   * FULL: Returns full email message data METADATA: Returns only email message ID, labels, and
   * email headers MINIMAL: Returns only email message ID and labels; does not return the email
   * headers, body, or payload. RAW: Returns the full email message data with body content in the
   * raw field as a base64url encoded string;
   */
  enum MessageFormat {
    FULL("full"),
    METADATA("metadata"),
    MINIMAL("minimal"),
    RAW("raw");

    public final String formatValue;

    MessageFormat(String formatValue) {
      this.formatValue = formatValue;
    }
  }
}
