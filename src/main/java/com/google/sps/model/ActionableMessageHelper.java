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
   * it is marked medium priority Otherwise, the message is of low priority.
   *
   * @param message Message object to be assigned. Should be of type METADATA (with the "TO" header)
   *     or FULL
   * @param userEmail the email address of the current user. Used to check if the email was actually
   *     sent to them or sent as a part of a mailing list
   * @return The priority of the message (HIGH, MEDIUM, LOW)
   * @throws com.google.sps.exceptions.GmailMessageFormatException if the message does not contain a
   *     "TO" header, which is mandatory, which implies the message is not of the correct format
   */
  ActionableMessage.MessagePriority assignMessagePriority(Message message, String userEmail);
}
