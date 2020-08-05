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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Encapsulates a response for the GmailActionableEmailsServlet. Contains just the bare minimum
 * information needed by the client for actionable emails
 */
public class ActionableMessage {
  private final String id;
  private final String subject;
  private final String sender;

  /**
   * Creates an ActionableMessage
   *
   * @param id the message id of the Message in the user's Gmail account
   * @param subject the subject line of the message from the user's Gmail account
   * @param sender the name, (or email address if name is not available), of the sender
   */
  public ActionableMessage(String id, String subject, String sender) {
    this.id = id;
    this.subject = subject;
    this.sender = sender;
  }

  public String getId() {
    return id;
  }

  public String getSubject() {
    return subject;
  }

  public String getSender() {
    return sender;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(7, 17).append(id).append(subject).append(sender).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ActionableMessage)) {
      return false;
    } else if (obj == this) {
      return true;
    }

    ActionableMessage actionableMessage = (ActionableMessage) obj;
    return new EqualsBuilder()
        .append(id, actionableMessage.getId())
        .append(subject, actionableMessage.getSubject())
        .append(sender, actionableMessage.getSender())
        .isEquals();
  }
}
