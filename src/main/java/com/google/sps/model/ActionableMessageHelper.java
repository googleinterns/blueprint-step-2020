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

/** Contains methods that help create the non-trivial fields in an ActionableMessage */
public interface ActionableMessageHelper {
  /**
   * Priority is determined by a series of arbitrary factors. If a message is starred by a user, it
   * is considered high priority. Then, if a message is not from a mailing list or marked important,
   * it is marked medium priority Otherwise, the message is of low priority. higher priority than a
   * message where both of these are false. Finally, if a message was sent later than another
   * message, it is considered to be of higher priority.
   *
   * @param message Message object to be assigned. Should be of type METADATA (with the "TO" header)
   *     or FULL
   * @param userEmail the email address of the current user. Used to check if the email was actually
   *     sent to them or sent as a part of a mailing list
   * @return 1 if messageOne has the higher priority, -1 is messageTwo has the higher priority 0 if
   *     they have the same priority (very very rare - needs same internal date which is measured in
   *     milliseconds)
   * @throws com.google.sps.exceptions.GmailMessageFormatException if the message does not contain a
   *     "TO" header, which is mandatory, which implies the message is not of the correct format
   */
  ActionableMessage.MessagePriority assignMessagePriority(Message message, String userEmail);
}
