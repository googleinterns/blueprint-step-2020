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

import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.servlets.ClientIDServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Test Client ID Servlet to ensure response contains a defined OAuth 2.0 client ID */
@RunWith(JUnit4.class)
public final class ClientIDServletTest extends ServletTestBase {
  private static final ClientIDServlet servlet = new ClientIDServlet();

  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  @Before
  public void setUp() throws IOException {
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);

    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void responseContainsClientId() throws Exception {
    servlet.doGet(request, response);
    printWriter.flush();
    System.out.println("PWD " + System.getProperty("user.dir"));
    Assert.assertTrue(stringWriter.toString().contains(AuthenticationVerifier.getClientId()));
  }
}
