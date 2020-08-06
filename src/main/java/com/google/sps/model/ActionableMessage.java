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
public class ActionableMessage implements Comparable<ActionableMessage> {
  private final String id;
  private final String subject;
  private final long internalDate;
  private final MessagePriority priority;
  private final String sender;

  /**
   * Creates an ActionableMessage
   *
   * @param id the message id of the Message in the user's Gmail account
   * @param subject the subject line of the message from the user's Gmail account
   * @param internalDate Timestamp of when the message was received in epoch milliseconds
   * @param priority the priority of the message (HIGH, MEDIUM, or LOW)
   * @param sender the name, (or email address if name is not available), of the sender
   */
  public ActionableMessage(
      String id, String subject, long internalDate, MessagePriority priority, String sender) {
    this.id = id;
    this.subject = subject;
    this.internalDate = internalDate;
    this.priority = priority;
    this.sender = sender;
  }

  public String getId() {
    return id;
  }

  public String getSubject() {
    return subject;
  }

  public long getInternalDate() {
    return internalDate;
  }

  public MessagePriority getPriority() {
    return priority;
  }

  public String getSender() {
    return sender;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(id)
        .append(subject)
        .append(internalDate)
        .append(priority.getPriorityValue())
        .append(sender)
        .toHashCode();
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
        .append(internalDate, actionableMessage.getInternalDate())
        .append(priority.getPriorityValue(), actionableMessage.getPriority().getPriorityValue())
        .append(sender, actionableMessage.getSender())
        .isEquals();
  }

  /**
   * Compares two actionable messages by their priorities. High priority > Medium priority > Low
   * priority. If the assigned priorities are the same, the actionableMessage that was sent later is
   * considered to be higher priority
   *
   * @param actionableMessage the ActionableMessage that this object is being compared against
   * @return A positive integer if the current object has the higher priority, a negative integer if
   *     the passed object has the higher priority, 0 if both objects have the same priority
   */
  @Override
  public int compareTo(ActionableMessage actionableMessage) {
    if (priority.getPriorityValue() == actionableMessage.priority.getPriorityValue()) {
      return (int) (internalDate - actionableMessage.internalDate);
    }

    return priority.getPriorityValue() - actionableMessage.priority.getPriorityValue();
  }

  public enum MessagePriority {
    HIGH(2),
    MEDIUM(1),
    LOW(0);

    private final int priorityValue;

    MessagePriority(int priorityValue) {
      this.priorityValue = priorityValue;
    }

    public int getPriorityValue() {
      return priorityValue;
    }
  }
}
