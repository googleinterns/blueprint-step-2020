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
import com.google.api.services.gmail.model.MessagePartHeader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Contains the summary gmail information that should be passed to the client, as well as the
 * methods to generate these statistics
 */
public class GmailResponse {
  private int nDays;
  private int unreadEmailsFromNDays;
  private int unreadEmailsFrom3Hours;
  private int unreadImportantEmailsFromNDays;
  private String senderOfUnreadEmailsFromNDays;

  public GmailResponse(GmailClient gmailClient, int nDays) {
    this.nDays = nDays;
    //populateNDaysMethods(nDays, gmailClient);
    setUnreadEmailsFrom3Hours(gmailClient);
    setUnreadEmailsFromNDays(nDays, gmailClient);
    setUnreadImportantEmailsFromNDays(nDays, gmailClient);
    setSenderOfUnreadEmailsFromNDays(nDays, gmailClient);
  }

  public int getNDays() {
    return nDays;
  }

  public String getSenderOfUnreadEmailsFromNDays() {
    return senderOfUnreadEmailsFromNDays;
  }

  public int getUnreadEmailsFrom3Hours() {
    return unreadEmailsFrom3Hours;
  }

  public int getUnreadEmailsFromNDays() {
    return unreadEmailsFromNDays;
  }

  public int getUnreadImportantEmailsFromNDays() {
    return unreadImportantEmailsFromNDays;
  }

  private void setUnreadEmailsFromNDays(int nDays, GmailClient gmailClient) {
    // Uses the stored n days
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, false, "");
    try {
      unreadEmailsFromNDays = gmailClient.listUserMessages(searchQuery).size();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setUnreadEmailsFrom3Hours(GmailClient gmailClient) {
    String searchQuery = GmailClient.emailQueryString(3, "h", true, false, "");
    try {
      unreadEmailsFrom3Hours = gmailClient.listUserMessages(searchQuery).size();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setUnreadImportantEmailsFromNDays(int nDays, GmailClient gmailClient) {
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, true, "");
    try {
      unreadImportantEmailsFromNDays = gmailClient.listUserMessages(searchQuery).size();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setSenderOfUnreadEmailsFromNDays(int nDays, GmailClient gmailClient) {
    String searchQuery = GmailClient.emailQueryString(nDays, "d", true, false, "");
    GmailClient.MessageFormat messageFormat = GmailClient.MessageFormat.METADATA;

    List<Message> unreadEmails;

    try {
      unreadEmails =
          gmailClient.listUserMessages(searchQuery).stream()
              .map(
                  (Message m) -> {
                    try {
                      return gmailClient.getUserMessage(m.getId(), messageFormat);
                    } catch (IOException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    HashMap<String, Integer> senders = new HashMap<>();

    unreadEmails.forEach(
        (Message m) -> {
          List<MessagePartHeader> senderList = m.getPayload().getHeaders();

          System.out.println("Here");

          senderList.stream()
              .filter((MessagePartHeader header) -> header.getName().equals("From"))
              .collect(Collectors.toList());

          System.out.println(senderList.size());

          String sender = senderList.get(0).getValue();

          senders.put(sender, senders.get(sender) != null ? senders.get(sender) + 1 : 1);
        });

    Map.Entry<String, Integer> entry =
        senders.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);

    senderOfUnreadEmailsFromNDays = entry != null ? entry.getKey() : "";

    System.out.println(senderOfUnreadEmailsFromNDays);
  }

  private void populateNDaysMethods(int nDays, GmailClient gmailClient) {
    List<CompletableFuture<Void>> threads = new ArrayList<>();
    threads.add(CompletableFuture.runAsync(() -> setUnreadEmailsFromNDays(nDays, gmailClient)));
    threads.add(
        CompletableFuture.runAsync(() -> setUnreadImportantEmailsFromNDays(nDays, gmailClient)));
    threads.add(
        CompletableFuture.runAsync(() -> setSenderOfUnreadEmailsFromNDays(nDays, gmailClient)));
    threads.add(CompletableFuture.runAsync(() -> setUnreadEmailsFrom3Hours(gmailClient)));

    threads.forEach(CompletableFuture::join);
  }
}
