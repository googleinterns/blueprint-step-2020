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

package com.google.sps.utility;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.sps.exceptions.GmailMessageFormatException;
import java.util.List;

/** Contains helper methods for Gmail Message objects */
public final class GmailUtility {
  private GmailUtility() {}

  /**
   * Given a list of message headers, extract a single header with the specified name
   *
   * @param message a Gmail message object. Must be METADATA or FULL format
   * @param headerName name of header that should be extracted
   * @return first header in list with name headerName (this method does not handle duplicate
   *     headers)
   * @throws GmailMessageFormatException if the message does not contain the specified header (and
   *     is thus the wrong format)
   */
  public static MessagePartHeader extractHeader(Message message, String headerName) {
    MessagePart payload = message.getPayload();
    if (payload == null) {
      throw new GmailMessageFormatException("Payload not present in message! Check message format");
    }
    List<MessagePartHeader> headers = payload.getHeaders();
    if (headers == null || headers.isEmpty()) {
      throw new GmailMessageFormatException("No headers present in payload! Check message format");
    }

    return headers.stream()
        .filter((header) -> header.getName().equals(headerName))
        .findFirst()
        .orElseThrow(
            () ->
                new GmailMessageFormatException(
                    String.format("%s Header not present!", headerName)));
  }

  /**
   * Gets a sender's contact name or email address.
   *
   * <p>"From" header values have two possible formats: "<sampleemail@sample.com>" (if name is not
   * available) OR "Sample Name <sampleemail@sample.com>" If a name is available, this is extracted.
   * Otherwise, the email is extracted
   *
   * @param fromHeaderValue the value of a "From" header from which a contact name / email should be
   *     extracted
   * @return A contact name if available, or an email if it is not
   */
  public static String parseNameInFromHeader(String fromHeaderValue) {
    return fromHeaderValue.charAt(0) == '<'
        ? fromHeaderValue.substring(1, fromHeaderValue.length() - 1)
        : fromHeaderValue.split("<")[0].trim();
  }
}
