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

import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.sps.model.AuthenticationVerifier;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serves the OAuth 2.0 Client ID */
@WebServlet("/client-id")
public class ClientIDServlet extends HttpServlet {
  private String clientId;

  /**
   * Constructor which obtains Client ID from AuthenticationVerifier
   *
   * @throws IOException
   */
  public ClientIDServlet() throws IOException {
    clientId = AuthenticationVerifier.getClientId();
  }

  /**
   * Constructor where the Client ID is input via a parameter.
   *
   * @param clientId String representing a Client ID
   */
  public ClientIDServlet(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Return the OAuth 2.0 Client ID to the client
   *
   * @param request Http request from client
   * @param response Http Response - should contain clientId
   * @throws IOException if an issue arises with response.getWriter
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Cannot use JsonUtility since response needs to be a JsonObject, not just a generic object.
    if (clientId == null) {
      clientId = AuthenticationVerifier.getClientId();
    }
    JsonObject clientIdJson = new JsonObject();
    clientIdJson.addProperty("client-id", clientId);

    response.setContentType("application/json");
    response.getWriter().println(clientIdJson);
  }
}
