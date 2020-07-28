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

import com.google.sps.servlets.ClientIDServlet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Client ID Servlet to ensure response contains a defined OAuth 2.0 client ID */
@RunWith(JUnit4.class)
public final class ClientIDServletTest extends ServletTestBase {

  private static final String CLIENT_ID = "sampleValue";
  private static final ClientIDServlet servlet = new ClientIDServlet(CLIENT_ID);

  @Test
  public void responseContainsClientId() throws Exception {
    servlet.doGet(request, response);
    Assert.assertTrue(stringWriter.toString().contains(CLIENT_ID));
  }
}
