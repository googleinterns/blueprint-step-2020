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

package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.utility.AuthenticationUtility;
import com.google.sps.utility.GmailUtility;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves the OAuth 2.0 Client ID */
@WebServlet("/gmail")
public class GmailServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Credential googleCredential = AuthenticationUtility.getGoogleCredential(request);
    if (googleCredential == null) {
      response.sendError(403, "Authentication tokens not present / invalid");
      return;
    }

    Gmail gmailService = GmailUtility.getGmailService(googleCredential);
    List<Message> messages = GmailUtility.listUserMessages(gmailService, "");

    Gson gson = new Gson();
    String messageJson = gson.toJson(messages);

    response.setContentType("application/json");
    response.getWriter().println(messageJson);
  }
}
